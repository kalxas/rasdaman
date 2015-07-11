from wcst.wcst import WCSTExecutor


class ConfigManager:
    """
     Holds the global information needed across the application.
     The values below will be overridden as soon as the application is initialized, the values
     below serve only as documentation
    """
    wcs_service = "http://localhost:8080/rasdaman/ows"
    crs_resolver = "http://localhost:8080/def/"
    default_crs = "http://localhost:8080/def/OGC/0/Index2D"
    tmp_directory = "/tmp/"
    mock = True
    insitu = False
    automated = False
    default_null_values = []
    root_url = "file://"
    executor = WCSTExecutor(wcs_service)
    default_field_name_prefix = "field_"
    default_unit_of_measure = "10^0"