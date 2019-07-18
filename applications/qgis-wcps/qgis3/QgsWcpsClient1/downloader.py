# -*- coding: utf-8 -*-

from builtins import str
from PyQt5.QtCore import QCoreApplication, QFile, QUrl
from PyQt5.QtNetwork import QNetworkRequest, QNetworkReply

def download_url(manager, url, output_path, progress_dialog=None):
    global xml_result
    xml_result = []

    # set up the  output path
    if output_path is not None:
        out_file = QFile(output_path)
        if not out_file.open(QFile.WriteOnly):
            raise IOError(out_file.errorString())

    # write data to file
    def write_data():
        global xml_result
        xml_result = reply.readAll().data()
        out_file.write(xml_result)
        out_file.flush()

    # read data from response
    def read_data():
        global xml_result
        xml_result.append(reply.readAll().data())

    # request the content of the url
    request = QNetworkRequest(QUrl(url))
    reply = manager.get(request)

    if output_path is None:
        reply.readyRead.connect(read_data)
    else:
        reply.readyRead.connect(write_data)

    if progress_dialog:
        def progress_event(received, total):
            QCoreApplication.processEvents()

            progress_dialog.setLabelText("%s / %s" % (received, total))
            progress_dialog.setMaximum(total)
            progress_dialog.setValue(received)

        # cancel the download
        def cancel_action():
            reply.abort()

        reply.downloadProgress.connect(progress_event)
        progress_dialog.canceled.connect(cancel_action)

    # wait until donwload is finished
    while not reply.isFinished():
        QCoreApplication.processEvents()

    result = reply.error()
    if result == QNetworkReply.NoError:
        if output_path is None:
            return True, None, xml_result
        else:
            out_file.close()
            return True, None, xml_result
    else:
        if output_path is not None:
            out_file.close()
        return result, str(reply.errorString())

    if  progress_dialog:
        progress_dialog.close()
