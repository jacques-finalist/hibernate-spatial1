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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

public class PolygonDecoder extends AbstractDecoder<Polygon> {

    @Override
    protected OpenGisType getOpenGisType() {
        return OpenGisType.POLYGON;
    }

    protected Polygon createNullGeometry() {
        return getGeometryFactory().createPolygon(null, null);
    }

    protected Polygon createGeometry(SqlGeometryV1 nativeGeom) {
        return createGeometry(nativeGeom, 0);
    }

    protected Polygon createGeometry(SqlGeometryV1 nativeGeom, int shapeIndex) {
        //polygons consist of one exterior ring figure, and several interior ones.
        int startFigure = nativeGeom.getStartFigureForShape(shapeIndex);
        int endFigure = nativeGeom.getEndFigureForShape(shapeIndex);
        LinearRing[] holes = new LinearRing[endFigure - startFigure - 1];
        LinearRing shell = null;
        for (int figureIdx = startFigure, i = 0; figureIdx < endFigure; figureIdx++) {
            int startPnt = nativeGeom.getStartPointForFigure(figureIdx);
            int endPnt = nativeGeom.getEndPointForFigure(figureIdx);
            if (nativeGeom.isFigureInteriorRing(figureIdx)) {
                holes[i++] = toLinearRing(nativeGeom, startPnt, endPnt);
            } else {
                shell = toLinearRing(nativeGeom, startPnt, endPnt);
            }
        }
        return getGeometryFactory().createPolygon(shell, holes);
    }

    private LinearRing toLinearRing(SqlGeometryV1 nativeGeom, int pointOffset, int nextPntOffset) {
        Coordinate[] coordinates = nativeGeom.coordinateRange(pointOffset, nextPntOffset);
        return getGeometryFactory().createLinearRing(coordinates);
    }

}
