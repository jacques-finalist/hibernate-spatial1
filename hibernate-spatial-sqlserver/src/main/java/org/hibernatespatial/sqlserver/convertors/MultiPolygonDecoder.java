/*
 * $Id$
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

import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;

public class MultiPolygonDecoder extends AbstractDecoder<MultiPolygon> {

    private final PolygonDecoder polygonDecoder = new PolygonDecoder();

    @Override
    protected OpenGisType getOpenGisType() {
        return OpenGisType.MULTIPOLYGON;
    }

    protected MultiPolygon createNullGeometry() {
        return getGeometryFactory().createMultiPolygon(null);
    }

    protected MultiPolygon createGeometry(SqlGeometryV1 nativeGeom) {
        return createGeometry(nativeGeom, 0);
    }

    @Override
    protected MultiPolygon createGeometry(SqlGeometryV1 nativeGeom, int shapeIndex) {
        int startChildShape = shapeIndex + 1;
        List<Polygon> polygons = new ArrayList<Polygon>(nativeGeom.getNumShapes());
        for (int childIdx = startChildShape; childIdx < nativeGeom.getNumShapes(); childIdx++) {
            if (!nativeGeom.isParentShapeOf(shapeIndex, childIdx)) continue;
            polygons.add(polygonDecoder.createGeometry(nativeGeom, childIdx));
        }
        return getGeometryFactory().createMultiPolygon(polygons.toArray(new Polygon[polygons.size()]));
    }


}
