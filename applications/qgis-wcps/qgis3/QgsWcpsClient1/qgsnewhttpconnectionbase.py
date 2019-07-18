# -*- coding: utf-8 -*-

from builtins import object
from PyQt5 import QtCore, QtGui, QtWidgets

try:
    _fromUtf8 = QtCore.QString.fromUtf8
except AttributeError:
    _fromUtf8 = lambda s: s

class Ui_qgsnewhttpconnectionbase(object):
    def setupUi(self, qgsnewhttpconnectionbase):
        qgsnewhttpconnectionbase.setObjectName(_fromUtf8("qgsnewhttpconnectionbase"))
        qgsnewhttpconnectionbase.resize(642, 153)
        self.buttonBox = QtWidgets.QDialogButtonBox(qgsnewhttpconnectionbase)
        self.buttonBox.setGeometry(QtCore.QRect(280, 110, 341, 32))
        self.buttonBox.setFocusPolicy(QtCore.Qt.NoFocus)
        self.buttonBox.setOrientation(QtCore.Qt.Horizontal)
        self.buttonBox.setStandardButtons(QtWidgets.QDialogButtonBox.Cancel|QtWidgets.QDialogButtonBox.Ok)
        self.buttonBox.setObjectName(_fromUtf8("buttonBox"))
        self.label_NewSrvName = QtWidgets.QLabel(qgsnewhttpconnectionbase)
        self.label_NewSrvName.setGeometry(QtCore.QRect(20, 33, 91, 17))
        self.label_NewSrvName.setObjectName(_fromUtf8("label_NewSrvName"))
        self.label_NewSrvUrl = QtWidgets.QLabel(qgsnewhttpconnectionbase)
        self.label_NewSrvUrl.setGeometry(QtCore.QRect(20, 75, 91, 17))
        self.label_NewSrvUrl.setObjectName(_fromUtf8("label_NewSrvUrl"))
        self.txt_NewSrvName = QtWidgets.QLineEdit(qgsnewhttpconnectionbase)
        self.txt_NewSrvName.setEnabled(True)
        self.txt_NewSrvName.setGeometry(QtCore.QRect(120, 30, 501, 27))
        self.txt_NewSrvName.setCursor(QtGui.QCursor(QtCore.Qt.IBeamCursor))
        self.txt_NewSrvName.setFocusPolicy(QtCore.Qt.ClickFocus)
        self.txt_NewSrvName.setObjectName(_fromUtf8("txt_NewSrvName"))
        self.txt_NewSrvUrl = QtWidgets.QLineEdit(qgsnewhttpconnectionbase)
        self.txt_NewSrvUrl.setGeometry(QtCore.QRect(120, 70, 501, 27))
        self.txt_NewSrvUrl.setObjectName(_fromUtf8("txt_NewSrvUrl"))

        self.retranslateUi(qgsnewhttpconnectionbase)
        self.buttonBox.accepted.connect(qgsnewhttpconnectionbase.accept)
        self.buttonBox.rejected.connect(qgsnewhttpconnectionbase.reject)
        QtCore.QMetaObject.connectSlotsByName(qgsnewhttpconnectionbase)

    def retranslateUi(self, qgsnewhttpconnectionbase):
        qgsnewhttpconnectionbase.setWindowTitle(QtWidgets.QApplication.translate("qgsnewhttpconnectionbase", "New WCPS Server ", None))
        self.label_NewSrvName.setText(QtWidgets.QApplication.translate("qgsnewhttpconnectionbase", "Server Name", None))
        self.label_NewSrvUrl.setText(QtWidgets.QApplication.translate("qgsnewhttpconnectionbase", "Server URL", None))
