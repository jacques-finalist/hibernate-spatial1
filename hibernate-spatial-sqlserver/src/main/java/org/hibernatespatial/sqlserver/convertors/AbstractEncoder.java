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
import com.vividsolutions.jts.geom.Geometry;
import org.hibernatespatial.mgeom.MGeometry;

abstract class AbstractEncoder<G extends Geometry> implements Encoder<G> {

    public SqlGeometryV1 encode(G geom) {
        SqlGeometryV1 nativeGeom = new SqlGeometryV1();
        nativeGeom.setSrid(geom.getSRID());
        if (geom.isValid()) nativeGeom.setIsValid();
        nativeGeom.setNumberOfPoints(geom.getNumPoints());
        if (hasMValues(geom))
            nativeGeom.setHasMValues();
        encodePoints(nativeGeom, geom);
        encodeFigures(nativeGeom, geom);
        encodeShapes(nativeGeom, geom);
        return nativeGeom;
    }

    protected boolean hasMValues(G geom) {
        return geom instanceof MGeometry;
    }

    protected void encodePoints(SqlGeometryV1 nativeGeom, Geometry geom) {

        Coordinate[] coords = geom.getCoordinates();
        for (int i = 0; i < coords.length; i++) {
            setCoordinate(nativeGeom, i, coords[i]);
        }
    }

    protected void setCoordinate(SqlGeometryV1 nativeGeom, int idx, Coordinate coordinate) {
        if (!nativeGeom.hasZValues() && !Double.isNaN(coordinate.z)) {
            nativeGeom.setHasZValues();
        }
        nativeGeom.setCoordinate(idx, coordinate);
    }

    abstract protected void encodeFigures(SqlGeometryV1 nativeGeom, G geom);

    abstract protected void encodeShapes(SqlGeometryV1 nativeGeom, G geom);

}
