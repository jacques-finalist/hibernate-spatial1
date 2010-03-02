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

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import org.hibernatespatial.mgeom.MLineString;

import java.util.ArrayList;
import java.util.List;

class MultiLineStringDecoder extends AbstractDecoder<MultiLineString> {

    private final LineStringDecoder lineStringDecoder = new LineStringDecoder();


    @Override
    protected OpenGisType getOpenGisType() {
        return OpenGisType.MULTILINESTRING;
    }

    protected MultiLineString createNullGeometry() {
        return getGeometryFactory().createMultiLineString(new LineString[]{});
    }

    protected MultiLineString createGeometry(SqlGeometryV1 nativeGeom) {
        return createGeometry(nativeGeom, 0);
    }

    @Override
    protected MultiLineString createGeometry(SqlGeometryV1 nativeGeom, int shapeIndex) {
        int startChildIndex = shapeIndex + 1;
        List<LineString> lineStrings = new ArrayList<LineString>(nativeGeom.getNumShapes());
        for (int childIdx = startChildIndex; childIdx < nativeGeom.getNumShapes(); childIdx++) {
            if (!nativeGeom.isParentShapeOf(shapeIndex, childIdx)) continue;
            lineStrings.add(lineStringDecoder.createGeometry(nativeGeom, childIdx));
        }
        if (nativeGeom.hasMValues()) {
            return getGeometryFactory().createMultiMLineString(lineStrings.toArray(new MLineString[lineStrings.size()]));
        } else {
            return getGeometryFactory().createMultiLineString(lineStrings.toArray(new LineString[lineStrings.size()]));
        }
    }


}
