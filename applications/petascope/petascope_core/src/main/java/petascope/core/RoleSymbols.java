package petascope.core;

/**
 * List of rasdaman role names used in petascope
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class RoleSymbols {
    
    // -- roles for rasdaman

    public static final String ROLE_ADMIN = "admin";
    
    
    public static final String PRIV_SERVER_MGMT = "PRIV_SERVER_MGMT";
    
    public static final String PRIV_LIST_TRIGGERS = "PRIV_LIST_TRIGGERS";
    public static final String PRIV_LIST_USERS = "PRIV_LIST_USERS";
    public static final String PRIV_LIST_ROLES = "PRIV_LIST_ROLES";
    
    public static final String PRIV_TRIGGER_MGMT = "PRIV_TRIGGER_MGMT";
    public static final String PRIV_USER_MGMT = "PRIV_USER_MGMT";
    public static final String PRIV_ROLE_MGMT = "PRIV_ROLE_MGMT";
    
    // -- roles for petascope
    
    public static final String PRIV_OWS_UPDATE_SRV = "PRIV_OWS_UPDATE_SRV";
    
    public static final String PRIV_OWS_WCS_GET_COV = "PRIV_OWS_WCS_GET_COV";
    
    public static final String PRIV_OWS_WCS_INSERT_COV = "PRIV_OWS_WCS_INSERT_COV";
    public static final String PRIV_OWS_WCS_UPDATE_COV = "PRIV_OWS_WCS_UPDATE_COV";
    public static final String PRIV_OWS_WCS_DELETE_COV = "PRIV_OWS_WCS_DELETE_COV";
    
    public static final String PRIV_OWS_WCS_PROCESS_COV = "PRIV_OWS_WCS_PROCESS_COV";
    public static final String PRIV_OWS_WMS_GET_MAP = "PRIV_OWS_WMS_GET_MAP";
    
    public static final String PRIV_OWS_WMS_INSERT_LAYER = "PRIV_OWS_WMS_INSERT_LAYER";
    public static final String PRIV_OWS_WMS_DELETE_LAYER = "PRIV_OWS_WMS_DELETE_LAYER";
    
    public static final String PRIV_OWS_WMS_INSERT_STYLE = "PRIV_OWS_WMS_INSERT_STYLE";
    public static final String PRIV_OWS_WMS_UPDATE_STYLE = "PRIV_OWS_WMS_UPDATE_STYLE";
    public static final String PRIV_OWS_WMS_DELETE_STYLE = "PRIV_OWS_WMS_DELETE_STYLE";
    
    public static final String PRIV_OWS_WCS_BLACKWHITELIST_COV = "PRIV_OWS_WCS_BLACKWHITELIST_COV";
    public static final String PRIV_OWS_WMS_BLACKWHITELIST_LAYER = "PRIV_OWS_WMS_BLACKWHITELIST_LAYER";
    public static final String PRIV_OWS_STATISTICS = "PRIV_OWS_STATISTICS";    
    
    
}
