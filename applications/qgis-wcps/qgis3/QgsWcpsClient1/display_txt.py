# -*- coding: utf-8 -*-

from builtins import object
from PyQt5 import QtCore, QtGui, QtWidgets

try:
    _fromUtf8 = QtCore.QString.fromUtf8
except AttributeError:
    _fromUtf8 = lambda s: s

class Ui_Dialog_Disp(object):
    def setupUi(self, Dialog_Disp):
        Dialog_Disp.setObjectName(_fromUtf8("Dialog_Disp"))
        Dialog_Disp.resize(721, 610)
        self.pushButton_DIsp_Done = QtWidgets.QPushButton(Dialog_Disp)
        self.pushButton_DIsp_Done.setGeometry(QtCore.QRect(610, 570, 98, 27))
        self.pushButton_DIsp_Done.setObjectName(_fromUtf8("pushButton_DIsp_Done"))
        self.textBrowser_Disp = QtWidgets.QTextBrowser(Dialog_Disp)
        self.textBrowser_Disp.setGeometry(QtCore.QRect(10, 10, 701, 551))
        self.textBrowser_Disp.setObjectName(_fromUtf8("textBrowser_Disp"))

        self.retranslateUi(Dialog_Disp)

        self.pushButton_DIsp_Done.clicked.connect(Dialog_Disp.close)

        QtCore.QMetaObject.connectSlotsByName(Dialog_Disp)

    def retranslateUi(self, Dialog_Disp):
        Dialog_Disp.setWindowTitle(QtWidgets.QApplication.translate("Dialog_Disp", "Dialog", None))
        self.pushButton_DIsp_Done.setText(QtWidgets.QApplication.translate("Dialog_Disp", "Done", None))
