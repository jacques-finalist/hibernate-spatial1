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
        LineString[] lineStrings;
        int startChildIndex = shapeIndex + 1;
        int endChildShapeIndex = nativeGeom.getEndChildShape(shapeIndex);
        if (nativeGeom.hasMValues()) {
            lineStrings = new MLineString[endChildShapeIndex - startChildIndex];
            collectLineStrings(nativeGeom, lineStrings, startChildIndex, endChildShapeIndex);
            return getGeometryFactory().createMultiMLineString((MLineString[]) lineStrings);
        } else {
            lineStrings = new LineString[endChildShapeIndex - startChildIndex];
            collectLineStrings(nativeGeom, lineStrings, startChildIndex, endChildShapeIndex);
            return getGeometryFactory().createMultiLineString(lineStrings);
        }
    }

    private void collectLineStrings(SqlGeometryV1 nativeGeom, LineString[] lineStrings, int startChild, int endChild) {

        for (int idx = startChild, i = 0; idx < endChild; idx++, i++){
            lineStrings[i] = lineStringDecoder.createGeometry(nativeGeom, idx);
        }
    }

}
