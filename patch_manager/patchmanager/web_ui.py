from trac.core import *
from trac.util.html import html
from trac.web import IRequestHandler
from trac.web.chrome import INavigationContributor, ITemplateProvider
from PatchMan import *
import logging
import email
import StringIO
import string
import re
import math

class PatchManagerPlugin(Component):
    implements(INavigationContributor, IRequestHandler)


    # INavigationContributor methods
    def get_active_navigation_item(self, req):
        return 'patchmanager'

    def get_navigation_items(self, req):
        yield ('mainnav', 'patchmanager',
               html.A('Patch Manager', href= req.href.patchmanager()))

    # IRequestHandler methods
    def match_request(self, req):
        match = re.match(r'/patchmanager(/page/(\d+))?(/all)?(/applied)?(/pending)?(/rejected)?$', req.path_info)
        return not (match is None)

    def process_request(self, req):

        self.env.log.debug("Processing request: " + str(req.args))
        patchMan = self.env[PatchMan];
        match = re.match(r'/patchmanager(/page/(\d+))?(/all)?(/applied)?(/pending)?(/rejected)?$', req.path_info)


        results_per_page = 10.0;

        patchCount = patchMan.patchCount();
        if (match.group(2) is None):
            cpage = 1
        else:
            cpage = int(match.group(2))

        if (not match.group(3) is None or not match.group(4) is None or not match.group(5) is None or not match.group(6) is None):
            cpage = 1
            results_per_page = patchCount

        totalPages = int(math.ceil(patchCount / results_per_page))

        data={'patches':patchMan.listPatches(req, match.group(4), match.group(5), match.group(6), (cpage - 1)*results_per_page,
        results_per_page), 'totalPages':totalPages, 'currentPage':cpage, 'base_url':req.base_path, 'branches':patchMan.listBranches()};

        if ('patchop' in req.args):
            result = patchMan.process_command(req.args['patchop'], req)
        else:
            result = None

        page = 'patchmanager.html';

        if (result):
            if ('page' in result):
                page = result['page']
            if ('data' in result):
                data.update(result['data']);

        data["TEXT"]=patchMan._getAgreementText();
        if 'TRAC_ADMIN' in req.perm:
            data["ADMIN"]=True
        return page, data, None
