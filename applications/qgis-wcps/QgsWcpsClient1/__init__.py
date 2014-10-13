# -*- coding: utf-8 -*-


# noinspection PyPep8Naming
def classFactory(iface):  # pylint: disable=invalid-name
    """Load QgisWcpsClient1 class from file wcps_client.

    :param iface: A QGIS interface instance.
    :type iface: QgsInterface
    """
    #
    from .wcps_client import QgsWcpsClient1
    return QgsWcpsClient1(iface)
