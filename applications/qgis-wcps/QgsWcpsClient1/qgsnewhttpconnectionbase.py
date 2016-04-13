# -*- coding: utf-8 -*-

# Form implementation generated from reading ui file 'qgsnewhttpconnectionbase.ui'
#
# Created: Thu Oct  9 21:06:44 2014
#      by: PyQt4 UI code generator 4.9.1
#
# WARNING! All changes made in this file will be lost!

from PyQt4 import QtCore, QtGui

try:
    _fromUtf8 = QtCore.QString.fromUtf8
except AttributeError:
    _fromUtf8 = lambda s: s

class Ui_qgsnewhttpconnectionbase(object):
    def setupUi(self, qgsnewhttpconnectionbase):
        qgsnewhttpconnectionbase.setObjectName(_fromUtf8("qgsnewhttpconnectionbase"))
        qgsnewhttpconnectionbase.resize(642, 153)
        self.buttonBox = QtGui.QDialogButtonBox(qgsnewhttpconnectionbase)
        self.buttonBox.setGeometry(QtCore.QRect(280, 110, 341, 32))
        self.buttonBox.setFocusPolicy(QtCore.Qt.NoFocus)
        self.buttonBox.setOrientation(QtCore.Qt.Horizontal)
        self.buttonBox.setStandardButtons(QtGui.QDialogButtonBox.Cancel|QtGui.QDialogButtonBox.Ok)
        self.buttonBox.setObjectName(_fromUtf8("buttonBox"))
        self.label_NewSrvName = QtGui.QLabel(qgsnewhttpconnectionbase)
        self.label_NewSrvName.setGeometry(QtCore.QRect(20, 33, 91, 17))
        self.label_NewSrvName.setObjectName(_fromUtf8("label_NewSrvName"))
        self.label_NewSrvUrl = QtGui.QLabel(qgsnewhttpconnectionbase)
        self.label_NewSrvUrl.setGeometry(QtCore.QRect(20, 75, 91, 17))
        self.label_NewSrvUrl.setObjectName(_fromUtf8("label_NewSrvUrl"))
        self.txt_NewSrvName = QtGui.QLineEdit(qgsnewhttpconnectionbase)
        self.txt_NewSrvName.setEnabled(True)
        self.txt_NewSrvName.setGeometry(QtCore.QRect(120, 30, 501, 27))
        self.txt_NewSrvName.setCursor(QtGui.QCursor(QtCore.Qt.IBeamCursor))
        self.txt_NewSrvName.setFocusPolicy(QtCore.Qt.ClickFocus)
        self.txt_NewSrvName.setObjectName(_fromUtf8("txt_NewSrvName"))
        self.txt_NewSrvUrl = QtGui.QLineEdit(qgsnewhttpconnectionbase)
        self.txt_NewSrvUrl.setGeometry(QtCore.QRect(120, 70, 501, 27))
        self.txt_NewSrvUrl.setObjectName(_fromUtf8("txt_NewSrvUrl"))

        self.retranslateUi(qgsnewhttpconnectionbase)
        QtCore.QObject.connect(self.buttonBox, QtCore.SIGNAL(_fromUtf8("accepted()")), qgsnewhttpconnectionbase.accept)
        QtCore.QObject.connect(self.buttonBox, QtCore.SIGNAL(_fromUtf8("rejected()")), qgsnewhttpconnectionbase.reject)
        QtCore.QMetaObject.connectSlotsByName(qgsnewhttpconnectionbase)

    def retranslateUi(self, qgsnewhttpconnectionbase):
        qgsnewhttpconnectionbase.setWindowTitle(QtGui.QApplication.translate("qgsnewhttpconnectionbase", "New WCPS Server ", None, QtGui.QApplication.UnicodeUTF8))
        self.label_NewSrvName.setText(QtGui.QApplication.translate("qgsnewhttpconnectionbase", "Server Name", None, QtGui.QApplication.UnicodeUTF8))
        self.label_NewSrvUrl.setText(QtGui.QApplication.translate("qgsnewhttpconnectionbase", "Server URL", None, QtGui.QApplication.UnicodeUTF8))
