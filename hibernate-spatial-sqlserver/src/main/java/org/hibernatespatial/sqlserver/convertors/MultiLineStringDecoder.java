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

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import org.hibernatespatial.mgeom.MLineString;

public class MultiLineStringDecoder extends AbstractDecoder<MultiLineString> {

    private LineStringDecoder lineStringDecoder = new LineStringDecoder();

    public boolean accepts(SqlGeometryV1 nativeGeom) {
        return nativeGeom.openGisType() == OpenGisType.MULTILINESTRING;
    }

    protected MultiLineString createNullGeometry() {
        return getGeometryFactory().createMultiLineString(new LineString[]{});
    }

    protected MultiLineString createGeometry(SqlGeometryV1 nativeGeom) {
        LineString[] lineStrings;
        if (nativeGeom.hasMValues()) {
            lineStrings = new MLineString[nativeGeom.getNumShapes() - 1];
            collectLineStrings(nativeGeom, lineStrings);
            return getGeometryFactory().createMultiMLineString((MLineString[]) lineStrings);
        } else {
            lineStrings = new LineString[nativeGeom.getNumShapes() - 1];
            collectLineStrings(nativeGeom, lineStrings);
            return getGeometryFactory().createMultiLineString(lineStrings);
        }

    }

    private void collectLineStrings(SqlGeometryV1 nativeGeom, LineString[] lineStrings) {
        int[] pointOffsets = collectPointOffsets(nativeGeom);
        for (int i = 0; i < pointOffsets.length - 1; i++) {
            int startOffset = pointOffsets[i];
            int nextOffset = pointOffsets[i + 1];
            if (nativeGeom.hasMValues())
                lineStrings[i] = lineStringDecoder.createMLineString(nativeGeom, startOffset, nextOffset);
            else
                lineStrings[i] = lineStringDecoder.createLineString(nativeGeom, startOffset, nextOffset);
        }
    }


    /**
     * Returns an array of point offsets for quickly returning the parts
     * in the point array the correspond to the linestrings.
     * <p/>
     * The last offset points beyond the end of the point array.
     *
     * @param nativeGeom
     * @return
     */
    private int[] collectPointOffsets(SqlGeometryV1 nativeGeom) {
        int[] pointOffsets = new int[nativeGeom.getNumShapes()];

        // first shape is the parent shape; second and following are linestrings.
        for (int shpIdx = 1; shpIdx < nativeGeom.getNumShapes(); shpIdx++) {
            Shape shape = nativeGeom.getShape(shpIdx);
            assert (shape.openGisType == OpenGisType.LINESTRING);
            int figureOffset = shape.figureOffset;
            Figure figure = nativeGeom.getFigure(figureOffset);
            pointOffsets[shpIdx - 1] = figure.pointOffset;
        }
        pointOffsets[nativeGeom.getNumShapes() - 1] = nativeGeom.getNumPoints();
        return pointOffsets;
    }

}
