package petascope.wms2.service.exception.error;

import org.jetbrains.annotations.NotNull;

/**
 * Exception to be thrown when importing a coverage to WMS but coverage is non-georeferenced (e.g: Index2D) or unsupported CRS (e.g: OGC)
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class WMSUnsupportedCrsToTransformException extends WMSException {
    /**
     * Constructor for the class
     *
     * @param crs the invalid crs from importing coverage.
     */
    public WMSUnsupportedCrsToTransformException(@NotNull String crs) {
        super(ERROR_MESSAGE.replace("$CRS", crs));
    }

    @NotNull
    @Override
    public String getExceptionCode() {
        return EXCEPTION_CODE;
    }


    private static final String EXCEPTION_CODE = "UnsupportedToTransformCRS";
    private static final String ERROR_MESSAGE = "CRS transformation not supported for the requested crs: $CRS";
}
