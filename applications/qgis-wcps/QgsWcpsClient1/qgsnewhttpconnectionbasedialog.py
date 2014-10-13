# -*- coding: utf-8 -*-
import os, pickle
from PyQt4.QtGui import *
from PyQt4.QtCore import *
from PyQt4 import QtCore, QtGui

from wcps_client_dialog_base import Ui_WCPSClient
from qgsnewhttpconnectionbase import Ui_qgsnewhttpconnectionbase


#global setttings and saved server list
global config
import config
srvlst = []

class qgsnewhttpconnectionbase(QDialog,  QObject, Ui_qgsnewhttpconnectionbase):
    MSG_BOX_TITLE = "WCPS Client"

    def __init__(self, parent, fl, toEdit, choice):
        QDialog.__init__(self, parent, fl)
        self.toEdit = toEdit
        self.idx_sel = choice
        self.parent = parent
        self.flags = fl
        self.setupUi(self)
        self.txt_NewSrvName.setFocus(True)
        self.setWindowTitle('WCPS Client') # +version())


    def accept(self):
        global config
        print 'IDX: ',self.idx_sel
        srvlst = config.srv_list['servers']
        srv_name = self.txt_NewSrvName.text()
        srv_url = self.txt_NewSrvUrl.text()

            # verify that URL starts with http://
        if not srv_url.startswith("http://"):
            msg = "Sorry, you need to supply a 'Server URL' starting with http://\n"
            self.warning_msg(msg)
            srv_name = self.txt_NewSrvName.text()


        if self.toEdit is False:
            try:
                idx = zip(*config.srv_list['servers'])[0].index(srv_name)
                while idx is not None:
                    self.txt_NewSrvName.setText(srv_name+'_1')
                    self.txt_NewSrvUrl.setText(srv_url)
                    msg = "Sorry, but the 'Server Name' has to be unique.\n      A   '_1'   has been added to the name."
                    self.warning_msg(msg)
                    srv_name = self.txt_NewSrvName.text()
                    idx = zip(*config.srv_list['servers'])[0].index(srv_name)


            except ValueError:
                srvlst.append([srv_name, srv_url])

        if self.toEdit is True:
            try:
                idx = zip(*config.srv_list['servers'])[0].index(srv_name)
            except ValueError:
                idx = self.idx_sel
                srvlst.pop(idx)
                srvlst.insert(idx,[srv_name, srv_url])

        config.srv_list = {'servers': srvlst }
        if (len(srv_name) > 0 and len(srv_url) > 10):
            self.parent.write_srv_list()
            self.parent.updateServerListing()
        else:
            msg = "Sorry, the provided 'Server Name' "+str(srv_name)+" or the provided 'Server URL '"+srv_url+" is not valid"
            self.warning_msg(msg)

        self.close()


    def warning_msg(self, msg):
        msgBox = QtGui.QMessageBox()
        msgBox.setText(msg)
        msgBox.addButton(QtGui.QPushButton('OK'), QtGui.QMessageBox.YesRole)
        msgBox.exec_()


