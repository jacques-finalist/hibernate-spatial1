/*
 * $Id:$
 *
 * This file is part of Hibernate Spatial, an extension to the
 * hibernate ORM solution for geographic data.
 *
 * Copyright © 2009 Geovise BVBA
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, visit: http://www.hibernatespatial.org/
 */

package org.hibernatespatial.sqlserver.convertors;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;

public class MultiPointDecoder extends AbstractDecoder<MultiPoint> {

    public boolean accepts(SqlGeometryV1 nativeGeom) {
        return nativeGeom.openGisType() == OpenGisType.MULTIPOINT;
    }

    protected MultiPoint createNullGeometry() {
        return getGeometryFactory().createMultiPoint(new Point[]{});
    }

    protected MultiPoint createGeometry(SqlGeometryV1 nativeGeom) {
        Coordinate[] coords = new Coordinate[nativeGeom.getNumPoints()];
        for (int cIdx = 0; cIdx < nativeGeom.getNumPoints(); cIdx++) {
            coords[cIdx] = nativeGeom.getCoordinate(cIdx);
        }
        return getGeometryFactory().createMultiPoint(coords);
    }


}
