/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU  General Public License for more details.
 *
 * You should have received a copy of the GNU  General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

package petascope.wms2.service.getmap;

import petascope.wms2.metadata.BoundingBox;
import petascope.wms2.metadata.Layer;
import petascope.wms2.service.base.Validator;
import petascope.wms2.service.exception.error.WMSException;
import petascope.wms2.service.exception.error.WMSInternalException;
import petascope.wms2.service.exception.error.WMSInvalidBbox;
import petascope.wms2.service.exception.error.WMSInvalidLayerException;

import java.util.List;


/**
 * Validates the get map request
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class GetMapValidator implements Validator<GetMapRequest> {

    /**
     * Constructor for the class
     *
     * @param request the get map request
     * @throws WMSException
     */
    @Override
    public void validate(GetMapRequest request) throws WMSException {
        validateBoundingBox(request.getLayers(), request.getBbox());
        validateLayers(request.getLayers());
    }

    /**
     * Validates the bounding box for each layer
     *
     * @param layers the layers that were requested
     * @param bbox   the bbox that was requested
     * @throws WMSInternalException
     * @throws WMSInvalidBbox
     */
    private void validateBoundingBox(Iterable<Layer> layers, BoundingBox bbox) throws WMSInternalException, WMSInvalidBbox {
        for (Layer layer : layers) {
            Iterable<BoundingBox> bboxes = layer.getBoundingBoxes();
            if (bboxes == null) {
                throw new WMSInternalException(new Exception("The layer " + layer.getTitle() + " did not contain any bounding boxes."));
            }
            for (BoundingBox boundingBox : bboxes) {
                if (boundingBox.getCrs().getCrsName().equalsIgnoreCase(bbox.getCrs().getCrsName())) {
                    if (!boundingBox.forceCanContain(bbox)) {
                        throw new WMSInvalidBbox(bbox.toString());
                    }
                }
            }
        }
    }

    /**
     * Throws an error if the request does not contain any layers
     *
     * @param layers the layers of the request
     * @throws WMSInvalidLayerException
     */
    private void validateLayers(List<Layer> layers) throws WMSInvalidLayerException {
        if (layers == null || layers.size() == 0) {
            throw new WMSInvalidLayerException("");
        }
    }
}
