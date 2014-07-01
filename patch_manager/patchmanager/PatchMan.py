import StringIO
from collections import deque
from datetime import *
import email
import logging
import os
import re
import string
import subprocess
import sys
import tempfile

import smtplib
from email.MIMEMultipart import MIMEMultipart
from email.MIMEBase import MIMEBase
from email.MIMEText import MIMEText
from email import Encoders

from trac.attachment import Attachment
from trac.attachment import AttachmentModule
from trac.config import PathOption
from trac.core import *
from trac.util.html import html
from trac.web import IRequestHandler
from trac.web.chrome import INavigationContributor
from trac.web.chrome import ITemplateProvider
from tracext.git import PyGIT
from jenkinsapi import JenkinsApi

class PatchMan(Component):
    implements(ITemplateProvider)

    _git_bin = PathOption('git', 'git_bin', '/usr/bin/git', "path to git executable")
    APPLIED = "APPLIED", "green"
    PENDING = "PENDING", "orange"
    REJECTED = "REJECTED", "red"
    GMAIL_USER = "rasdaman.community@gmail.com"
    GMAIL_PWD = "TODO"
    RECIPIENTS = ["p.baumann@jacobs-university.de", "d.misev@jacobs-university.de"]

    def addNewPatch(self, FromAddr, Subject, Branch, Date, FileContent):
        db = self.env.get_db_cnx()
        cursor = db.cursor()
        cursor.execute("INSERT INTO Patches (email, subject, branch, commit_time, submit_time, rejected)\
            VALUES (%s, %s, %s, %s, %s, %s) ", (FromAddr, Subject, Branch, Date, datetime.today(), "0"))
        id = db.get_last_id(cursor, "Patches");

        file = Attachment(self.env, 'PatchManager', '0.2')
        filename = "patch" + str(id)
        FileContent.file.seek(0, 2)             # seek to end of file
        size = FileContent.file.tell()
        FileContent.file.seek(0);
        file.insert(filename, FileContent.file, size)
        db.commit()

        jenkins_url = self.config.get('jenkins', 'url');
        jenkins_user = self.config.get('jenkins', 'user');
        jenkins_passwd = self.config.get('jenkins', 'passwd');
        job_name = self.config.get('jenkins', 'patch_test_job_name');
        jenkins = JenkinsApi(jenkins_url, jenkins_user, jenkins_passwd, self.env.log);

        jenkins.test_patch(id, job_name)

        #TODO REMOVE
        return

        for r in self.RECIPIENTS:
            self.sendMail(r, "[PATCH] <%s> %s" % (self.cutString(FromAddr, 15), self.cutString(Subject, 40)),
                "New patch submitted to rasdaman.org:\n\nSubject\t %s\nFrom\t %s\nDate\t %s" % (Subject, FromAddr, Date),
                None)
        return

    def cutString(self, s, n):
        if len(s) > n:
            return s[:n] + "..."
        else:
            return s

    # http://kutuma.blogspot.com/2007/08/sending-emails-via-gmail-with-python.html
    def sendMail(self, to, subject, text, attach):
        msg = MIMEMultipart()
        msg['From'] = self.GMAIL_USER
        msg['To'] = to
        msg['Subject'] = subject
        msg.attach(MIMEText(text))

        if attach != None:
           part = MIMEBase('application', 'octet-stream')
           part.set_payload(open(attach, 'rb').read())
           Encoders.encode_base64(part)
           part.add_header('Content-Disposition',
                   'attachment; filename="%s"' % os.path.basename(attach))
           msg.attach(part)

        try:
           mailServer = smtplib.SMTP("smtp.gmail.com", 587)
           mailServer.ehlo()
           mailServer.starttls()
           mailServer.ehlo()
           mailServer.login(self.GMAIL_USER, self.GMAIL_PWD)
           mailServer.sendmail(self.GMAIL_USER, to, msg.as_string())
           # Should be mailServer.quit(), but that crashes...
           mailServer.close()
        except:
           self.env.log.debug("failed sending email to " + to)

    def sendPatchStatusMail(self, id, patchStatus):
        """Send an email to the submitter of a patch with the given id. The
        subject is of the form 'rasdaman.org: PATCH patchStatus'"""
        db = self.env.get_db_cnx()
        cursor = db.cursor()
        cursor.execute("SELECT email, subject, submit_time FROM Patches WHERE id='" + str(id) + "'")
        for email, subject, submit_time in cursor:
            #self.env.log.debug("in sendPatchStatusMail: " + email + ' ' + subject + ' ' + submit_time)
            if email and subject and submit_time:
                self.sendMail(email, "rasdaman.org: PATCH " + patchStatus,
                "Your patch submitted to rasdaman.org on " + str(submit_time) +
                " has been " + patchStatus + "\n\nPatch description:\n" + str(subject),
                None)

    def checkPatchStatus(self, repo_patches, email, subject, branch):
        if email is None or subject is None:
            return self.PENDING
        r = re.compile(r'\[PATCH[^\]]*\]( *\[[^\]]*\])* *(.*)', re.DOTALL)
        match = r.match(subject)
        if not match is None:
            subject = match.group(2)
        key = self.getPatchUID(email, subject, branch)
        if (not repo_patches.has_key(key)):
            key = self.getPatchUID(email, subject, 'master')

        if (repo_patches.has_key(key)):
            return self.APPLIED
        else:
            return self.PENDING


    def getPatchUID(self, author, message, branch):
        return self._deleteExtraWhiteSpace(author + ":" + message + " (" + branch + ")").strip()

    def listBranches(self):
        results = deque()

        repDir = self.config.get('trac', 'repository_dir')
        gitBin = self.config.get('git', 'git_bin')

        #REPO should be repDir if everything is fine with trac.ini
        REPO = repDir
        output = self.getSystemOutput('cd ' + REPO + '; git branch')

        res = ""
        results.append({'branch':'master'})
        for branch in output:
            temp = branch[2:-1]
            if temp != 'master':
				results.append({'branch':temp})

        return results

    def listPatches(self, req, applied, pending, rejected, offset=0, count=10):
        result = deque();
        repo_patches = dict();
        subject_patches = dict()

        repo = self.env.get_repository();

        rev = repo.get_youngest_rev();

        while (not (rev is None)):
            t = repo.get_changeset(rev);
            properties = t.get_properties().get("git-author");
            branch = ""
            for tmp in t.get_branches():
                branch, head = tmp
                break
            msg = t.message
            end = msg.find("\n")
            if end != -1:
                msg = msg[:end]
            if (properties is None):
                key = self.getPatchUID(t.author, msg, branch);
            else:
                key = self.getPatchUID(properties[0], msg, branch);
            rev = repo.previous_rev(rev)
            repo_patches.update([(key, True)]);
            subject_patches.update([(key, t.message)])

        db = self.env.get_db_cnx()
        cursor = db.cursor()
        cursor.execute("SELECT id, email, subject, branch, commit_time, submit_time, rejected, test_status FROM Patches ORDER BY submit_time DESC LIMIT %s OFFSET %s", (count, offset))

        jenkins_url = self.config.get('jenkins', 'url');
        jenkins_user = self.config.get('jenkins', 'user');
        jenkins_passwd = self.config.get('jenkins', 'passwd');
        jenkins = JenkinsApi(jenkins_url, jenkins_user, jenkins_passwd, self.env.log);
        job_name = self.config.get('jenkins', 'patch_test_job_name');
        test_status = jenkins.get_test_patch_map(job_name)
        self.env.log.debug(test_status)

        update_cursor = db.cursor()
        refreshPage = False

        for id, email, subject, branch, commit_time, submit_time, rejected_att, test in cursor:
            if not branch:
                branch = 'master'
            if rejected_att == 1:
                status = self.REJECTED
            else:
                status = self.checkPatchStatus(repo_patches, email, subject, branch)
            if not applied is None and status != self.APPLIED:
                continue
            if not pending is None and status != self.PENDING:
                continue
            if not rejected is None and status != self.REJECTED:
                continue

            ind = submit_time.find(".")
            if ind != -1:
                submit_time = submit_time[:ind]

            r = re.compile(r'\[PATCH[^\]]*\] +(.*)', re.DOTALL)
            match = r.match(subject)
            if not match is None:
                tmp = match.group(1)
            else:
                tmp = subject
            key = self.getPatchUID(email, tmp, branch)
            if (not subject_patches.has_key(key)):
                key = self.getPatchUID(email, tmp, 'master')

            if (subject_patches.has_key(key)):
                subj = subject_patches[key]
            else:
                subj = subject

            if (status == self.PENDING) and (id in test_status):
                refreshPage |= bool(test_status[id]['building'])
                if (not bool(test_status[id]['building'])) and (test is None):
                    test = test_status[id]['buildStatus']
                    update_cursor.execute("UPDATE Patches SET test=%s WHERE id=%s", (test, id))

            result.append({'id':id, 'email':self.encodeEmail(email, req),
                          'subject':subj, 'branch':branch, 'commit_time':commit_time,
                          'submit_time':submit_time, 'status':status[0], 'status_color':status[1], 'test' : test});
        db.commit()
        return result, refreshPage

    def encodeEmail(self, email, req):
        result = "";
        if email is None:
            return result
        if 'anonymous' == req.perm.username:
            ind = email.find("<")
            if ind != -1:
                email = email[:ind]
        for c in email:
            result += chr(ord(c) + 1);
        return result

    def patchCount(self):
        db = self.env.get_db_cnx()
        cursor = db.cursor()
        cursor.execute("SELECT count(id) as cnt FROM Patches")
        result = cursor.fetchone()[0]
        return result

    def _getAgreementText(self):
        path = self.get_htdocs_dirs();
        f = open(path[0][1] + '/agreement.txt');
        result = f.read();
        f.close();
        return result;

    # ITemplateProvider methods
    def get_templates_dirs(self):
        from pkg_resources import resource_filename
        return [resource_filename(__name__, 'templates')]

    def get_htdocs_dirs(self):
        from pkg_resources import resource_filename
        return [('patchmanager', resource_filename(__name__, 'htdocs'))]

    def _getPatchDetails(self, emailContent):
        if (type(emailContent) == str):
            msg = email.message_from_string(emailContent)
        else:
            msg = email.message_from_file(emailContent.file)
        return {'From':msg.get("From"), 'Subject':msg.get("Subject"), 'Date':msg.get("Date")};

    def _getBundleDetails(self, bundle, selectbranch):

        _bundle_file = bundle.file
        _bundle_file.seek(0)
        _rep_dir = self.config.get('trac', 'repository_dir')
        _git_bin = self.config.get('git', 'git_bin')
        _cmd = ""

        # Temporarily store the bundle file
        _temp_bundle_file = tempfile.NamedTemporaryFile()
        _temp_bundle_file.write(_bundle_file.read())
        _temp_bundle_file.seek(0)
        _bundle_path = _temp_bundle_file.name
        # DEBUG
        #_bundle_file.seek(0)
        #self.env.log.debug("BUNDLE FILE: " + _bundle_file.read(500))
        #self.env.log.debug("TEMP BUNDLE FILE: " + _temp_bundle_file.read(500))

        # Clone the repo for testing
        _temp_dir = tempfile.mkdtemp();
        _cmd = "cd " + _temp_dir + "; " + _git_bin + " clone -b " + selectbranch + " " + _rep_dir + " " + _temp_dir
        self.env.log.debug("terminal call: " + _cmd)
        os.system( _cmd )

        # Check there is a single commit inside the bundle
        _cmd = "cd " + _temp_dir + "; test $( " + _git_bin + " bundle list-heads " + _bundle_path + " | wc -l ) = 1"
        self.env.log.debug("terminal call:" + _cmd)
        if os.system( _cmd ) != os.EX_OK:
            raise TracError("The uploaded bundle contains more than one commit.")
        #_temp_bundle_file.seek(0)

        # Need to apply the bundle and fetch the details there (From, To, Date, etc.)
        # checkout to a tmp branch (otherwise git-fetch won't apply)
        _cmd = "cd " + _temp_dir + "; " + _git_bin + " checkout -b tmp "
        self.env.log.debug(("terminal call: " + _cmd + " 2>&1"))
        os.system( _cmd )
        # apply the bundle
        _cmd = "cd " + _temp_dir + "; " + _git_bin + " fetch " + _bundle_path + " " + selectbranch + ":" + selectbranch
        self.env.log.debug(("terminal call: " + _cmd + " 2>&1"))
        self.env.log.debug(self.toString(self.getSystemOutput( _cmd + " 2>&1" )))

        # patch the bundle's commit and extract metadata
        _cmd = "cd " + _temp_dir + "; " + _git_bin + " show " + selectbranch + " HEAD --pretty=email"
        self.env.log.debug(("terminal call: " + _cmd + " 2>&1"))
        _bundle_patch = self.toString(self.getSystemOutput(_cmd + " 2>&1"))
        #self.env.log.debug("Bundle diffs: " + _bundle_patch)

        # cleaning
        _temp_bundle_file.close()
        os.system("rm -r " + _temp_dir)

        return self._getPatchDetails(_bundle_patch);

    def _deleteExtraWhiteSpace(self, string1):
        result = StringIO.StringIO();
        lastSpace = False;
        for i in range(len(string1)):
            if (not(string1[i] in string.whitespace)):
                result.write(string1[i]);
            elif (not lastSpace):
                result.write(" ");
            lastSpace = string1[i] in string.whitespace;
        return result.getvalue();

    def getAttachment(self, id):
        try:
            return Attachment(self.env, 'PatchManager', '0.2', "patch" + str(id))
        except:
            pass
        try:
            return Attachment(self.env, 'PatchManager', '0.1', "patch" + str(id))
        except:
            pass
        return None

    def processUploadPatch(self, req):
        if (not 'agree' in req.args or req.args['agree'] != 'on'):
            raise TracError('Please accept the license agreement')

        if (not 'agreement' in req.args):
            raise TracError('Please accept the license agreement')

        uploadedAgreement = self._deleteExtraWhiteSpace(req.args['agreement']);
        localAgreement = self._deleteExtraWhiteSpace(self._getAgreementText());

        if (uploadedAgreement != localAgreement):
            raise TracError('Uploaded agreement differs from our local agreement. Please contact admin!')

        file_name = self.getFileName(req.args['patchfile'])
        self.env.log.debug("Name of the uploaded file: " + file_name)
        selectbranch = req.args["selectbranch"]

        data = {};
        if re.match(r'.*\.patch', file_name):
            data.update(self._getPatchDetails(req.args["patchfile"]));
        elif re.match(r'.*\.bundle', file_name):
            data.update(self._getBundleDetails(req.args["patchfile"], selectbranch));
        else:
            raise TracError("Please upload a file with either .patch or .bundle extension.")

        if data['From'] is None:
            raise TracError("The patch doesn't have any author credentials")
        if data['Subject'] is None:
            raise TracError("The patch doesn't have any description")

        subject = data['Subject']
        r = re.compile(r'\[PATCH[^\]]*\] +ticket:[0-9]+ .+', re.DOTALL)
        match = r.match(subject)
        if match is None and selectbranch == 'master':
            raise TracError("The subject of the patch is invalid; Please edit it so that the subject starts with ticket:NUMBER, where NUMBER is a valid ticket number on the rasdaman.org tracker.")

        self.env.log.debug("A new patch was submitted! Getting details...");
        self.addNewPatch(data['From'], data['Subject'], req.args["selectbranch"], data['Date'], req.args["patchfile"])
        return {'page':'addpatchdetails.html', 'data':data}

    def automatic_test_results(self):
        jenkins_url = self.config.get('jenkins', 'url');
        jenkins_user = self.config.get('jenkins', 'user');
        jenkins_passwd = self.config.get('jenkins', 'passwd');
        job_name = self.config.get('jenkins', 'automatic_test_job_name');
        jenkins = JenkinsApi(jenkins_url, jenkins_user, jenkins_passwd, self.env.log);

        tests = jenkins.get_automatic_build_map(job_name)

        keys = sorted(tests.keys(), reverse=True)
        result = list()
        for k in keys:
            result.append(tests[k])

        return result

    def processDeletePatch(self, req):
        if not 'TRAC_ADMIN' in req.perm:
            raise TracError('You do not have enough priviledges to delete a patch!')
        db = self.env.get_db_cnx()
        cursor = db.cursor()
        for id in self.getIDs(req.args['select']):
            self.sendPatchStatusMail(id, "DELETED")
            cursor.execute("DELETE FROM Patches WHERE id=" + str(id))
            file = self.getAttachment(id)
            file.delete()
        db.commit()

    def processDownloadPatch(self, req):
        tempDir = tempfile.mkdtemp("patch");
        path = os.path.join(self.env.path, 'attachments', 'PatchManager', '0.2')
        cmd = "cd " + tempDir + "; tar -czf archive.tgz -C " + path + " "

        for id in self.getIDs(req.args['select']):
            cmd += "patch" + str(id) + " "
        self.env.log.debug("executing " + cmd);
        os.system(cmd);
        req.send_header('Content-Disposition', 'attachment; filename="patches.tgz"');
        req.send_file(tempDir + "/archive.tgz", 'application/x-tar-gz')


    def getIDs(self, args):
        if type(args) == type("ab") or type(args) == type(u"ab"):
            return [args];
        else:
            return args;

    def getFileName(self, arg):
        if arg.filename:
            return arg.filename
        else:
            raise TracError("This resource has no filename.")

    def getSystemOutput(self, cmd):
        Messages = []
        proc = os.popen(cmd, "r");
        for line in proc:
            Messages.append(line);
        status = proc.close();
        return Messages


    def processApplyPatch(self, req):
        if not 'TRAC_ADMIN' in req.perm:
            raise TracError('You do not have enough priviledges to apply a patch!')

        tempDir = tempfile.mkdtemp("patch");
        os.removedirs(tempDir);

        repDir = self.config.get('trac', 'repository_dir')
        gitBin = self.config.get('git', 'git_bin')
        # git commands
        gitAm     = gitBin + ' am -3 --ignore-whitespace --whitespace=fix ' # see LSIS ticket #45
        gitBranch = gitBin + ' branch '
        gitClone  = gitBin + ' clone '
        gitCo     = gitBin + ' checkout '
        gitFetch  = gitBin + ' fetch '
        gitLog    = gitBin + ' log '
        gitPush   = gitBin + ' push '

        log = ""

        for id in self.getIDs(req.args['select']):
            try:
                file = self.getAttachment(id)
                patch_path = file._get_path('PatchManager', '0.2', "patch" + str(id))
                #log += self.toString(self.getSystemOutput("cd " + tempDir + "; " + gitAm + patch_path + " 2>&1"))
            except RuntimeError:
                print ""

            try:
                db = self.env.get_db_cnx()
                cursor = db.cursor()
                cursor.execute("SELECT email, subject, branch, submit_time FROM Patches WHERE id='" + str(id) + "'")
                for email, subject, branch, submit_time in cursor:
                    if not branch:
                        branch = 'master'

                    tempDir = tempfile.mkdtemp('tbranch' + str(id));
                    os.removedirs(tempDir);

                    self.env.log.debug("terminal call: " + gitClone + " -b " + branch + " " + repDir + " " + tempDir)
                    os.system(gitClone + " -b " + branch + " " + repDir + " " + tempDir)

                    # Need to distinguish between an email-patch and a binary bundle:
                    _bundle_head_pattern = "git bundle"
                    _cmd = "head -n 1 " + patch_path + " | grep \"" + _bundle_head_pattern + "\""
                    if os.system( _cmd ) != os.EX_OK:
                        self.env.log.debug("applying patch...");
                        self.env.log.debug("terminal call2: " + "cd " + tempDir + "; " + gitAm + patch_path + " 2>&1")
                        log += self.toString(self.getSystemOutput("cd " + tempDir + "; " + gitAm + patch_path + " 2>&1"))
                    else:
                        self.env.log.debug("applying git bundle...");
                        _tmp_branch = "tmp"
                        # apply the bundle
                        log += self.toString(self.getSystemOutput("cd " + tempDir + "; " + gitCo + " -b " + _tmp_branch + " 2>&1"))
                        log += "\n"
                        log += self.toString(self.getSystemOutput("cd " + tempDir + "; " + gitFetch + patch_path + " " + branch + ":" + branch + " 2>&1"))
                        # get back to master and clean up
                        log += "\n"
                        log += self.toString(self.getSystemOutput("cd " + tempDir + "; " + gitCo + " master 2>&1"))
                        log += "\n"
                        log += self.toString(self.getSystemOutput("cd " + tempDir + "; " + gitBranch + " -D " + _tmp_branch + " 2>&1"))
                        # Log (DEBUG)
                        log += "\n"
                        log += self.toString(self.getSystemOutput("cd " + tempDir + "; " + gitLog + " --graph -3 " + branch + " 2>&1"))

                    # Push changes to public repo
                    try:
                      log += "\n" + self.toString(self.getSystemOutput("cd " + tempDir + "; " + gitPush + " origin " + branch + " 2>&1"))

                      os.system("rm -Rf " + tempDir)
                      self.setRejected(id, 0)

                      if email and subject and submit_time:
                          subjectCut = str(subject)[8:80]
                          if len(str(subject)) > 80:
                              subjectCut = subjectCut + "..."
                          self.sendMail("rasdaman-dev@googlegroups.com",
                          "Patch applied: " + subjectCut,
                          "New patch has been applied in rasdaman:\n\n" + str(subject) +
                          "\n\nSubmitted on: " + str(submit_time) +
                          "\nSubmitted by: " + str(email),
                          None)
                          self.sendPatchStatusMail(id, "APPLIED")
                    except RuntimeError:
                          print ""
            except RuntimeError:
                print ""

        return {'page':'applypatchlog.html', 'data':{'messages':log.strip()}}

    def toString(self, msgs):
        res = ""
        for m in msgs:
            res += str(m)
        return res.strip() + "\n\n"

    def setRejected(self, id, flag):
        db = self.env.get_db_cnx()
        cursor = db.cursor()
        try:
            cursor.execute("UPDATE Patches SET rejected=" + str(flag) + " WHERE id=" + id)
        except:
            self.env.log.error("Error un-rejecting patch %s" % id)
        db.commit()

    def processRejectPatch(self, req):
        if not 'TRAC_ADMIN' in req.perm:
            raise TracError('You do not have enough priviledges to reject a patch!')
        Messages = [];
        for id in self.getIDs(req.args['select']):
            self.setRejected(id, 1)
            self.sendPatchStatusMail(id, "REJECTED")
        return {'data':{'messages':Messages}}

    def processTryApplyPatch(self, req):
        if not 'TRAC_ADMIN' in req.perm:
            raise TracError('You do not have enough priviledges to apply a patch!')

        repDir = self.config.get('trac', 'repository_dir')
        gitBin = self.config.get('git', 'git_bin')
        # git commands
        gitAm     = gitBin + ' am -3 --ignore-whitespace --whitespace=fix ' # see LSIS ticket #45
        gitClone  = gitBin + ' clone '
        Messages = [];
        for id in self.getIDs(req.args['select']):
            try:
                file = self.getAttachment(id)
                patch_path = file._get_path('PatchManager', '0.2', "patch" + str(id))
                #log += self.toString(self.getSystemOutput("cd " + tempDir + "; " + gitAm + patch_path + " -3 2>&1"))
            except RuntimeError:
                print ""

            try:
                self.setRejected(id, 0)
                #self.sendPatchStatusMail(id, "APPLIED")

                # send an email to rasdaman-dev
                db = self.env.get_db_cnx()
                cursor = db.cursor()
                cursor.execute("SELECT email, subject, branch, submit_time FROM Patches WHERE id='" + str(id) + "'")
                for email, subject, branch, submit_time in cursor:
                    if not branch:
                        branch = 'master'

                    self.env.log.debug("apply patch to branch: " + branch)

                    tempDir = tempfile.mkdtemp('tbranch' + str(id));
                    os.removedirs(tempDir);

                    self.env.log.debug("terminal call: " + gitClone + " -b " + branch + " " + repDir + " " + tempDir)
                    Messages.extend(self.getSystemOutput(gitClone + " -b " + branch + " " + repDir + " " + tempDir))

                    Messages.extend(self.getSystemOutput("cd " + tempDir + "; " + gitAm + patch_path + " 2>&1"))
                    self.env.log.debug("terminal call2: " + "cd " + tempDir + "; " + gitAm + patch_path + " 2>&1")

                    os.system("rm -Rf " + tempDir)
            except RuntimeError:
                print ""

        return {'data':{'messages':Messages}}

    def process_command(self, patchop, req):
        ind = patchop.find("-")
        if ind != -1:
            req.args.update([("select", patchop[ind+1:])])
            patchop = patchop[:ind]

        if (patchop == "Upload patch"):
            return self.processUploadPatch(req)
        elif (patchop == "Delete"):
            self.processDeletePatch(req)
            req.redirect(req.href.patchmanager())
        elif (patchop == "Download Selected"):
            self.processDownloadPatch(req)
        elif (patchop == "Apply"):
            return self.processApplyPatch(req)
        elif (patchop == "Try Apply"):
            return self.processTryApplyPatch(req)
        elif (patchop == "Reject"):
            ret = self.processRejectPatch(req)
            req.redirect(req.href.patchmanager())
            return ret
        else:
            raise TracError('Don\'t know how to handle operation: "' + patchop + '"')
