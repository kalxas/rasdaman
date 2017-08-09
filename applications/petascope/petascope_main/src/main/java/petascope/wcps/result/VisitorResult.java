package petascope.wcps.result;

/**
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public interface VisitorResult {
    public void setMimeType(String mimeType);
    public String getMimeType();
}
