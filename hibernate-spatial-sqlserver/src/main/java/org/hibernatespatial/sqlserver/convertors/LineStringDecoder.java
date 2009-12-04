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
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.LineString;
import org.hibernatespatial.mgeom.MCoordinate;

class LineStringDecoder extends AbstractDecoder<LineString> {

    public boolean accepts(SqlGeometryV1 nativeGeom) {
        return nativeGeom.openGisType() == OpenGisType.LINESTRING;
    }

    protected LineString createNullGeometry() {
        return getGeometryFactory().createLineString((CoordinateSequence) null);
    }

    protected LineString createGeometry(SqlGeometryV1 nativeGeom) {
        return createLineString(nativeGeom, 0, nativeGeom.getNumPoints());
    }

    protected LineString createLineString(SqlGeometryV1 nativeGeom, int offset, int nextOffset) {
        Coordinate[] coords = createCoordinateArray(nextOffset - offset, nativeGeom);
        for (int idx = offset, i = 0; idx < nextOffset; idx++, i++) {
            coords[i] = nativeGeom.getCoordinate(idx);
        }
        return createLineString(coords, nativeGeom);
    }

    private LineString createLineString(Coordinate[] coords, SqlGeometryV1 nativeGeom) {
        if (nativeGeom.hasMValues()) {
            return getGeometryFactory().createMLineString((MCoordinate[]) coords);
        } else {
            return getGeometryFactory().createLineString(coords);
        }

    }

    private Coordinate[] createCoordinateArray(int size, SqlGeometryV1 nativeGeom) {
        if (nativeGeom.hasMValues()) {
            return new MCoordinate[size];
        } else {
            return new Coordinate[size];
        }
    }

}
