# -*- coding: utf-8 -*-

import os, pickle
from PyQt5.QtWidgets import QDialog
from PyQt5.QtCore import QObject
from .wcps_client_dialog_base import Ui_WCPSClient

# create the dialog for zoom to point
from .display_txt import Ui_Dialog_Disp

#global setttings and saved server list
global config
from . import config

class display_txt(QDialog, QObject, Ui_Dialog_Disp):

    def __init__(self, parent):
        QDialog.__init__(self, parent)
        self.setupUi(self)
        self.setWindowTitle('ProcessCoverage Response')
