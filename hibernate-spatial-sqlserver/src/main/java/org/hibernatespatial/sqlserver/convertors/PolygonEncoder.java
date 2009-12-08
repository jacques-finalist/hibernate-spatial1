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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

public class PolygonEncoder extends AbstractEncoder<Polygon> {

    private static Shape SHAPE = new Shape(-1, 0, OpenGisType.POLYGON);

    public boolean accepts(Geometry geom) {
        return geom instanceof Polygon;
    }

    protected void encodeFigures(SqlGeometryV1 nativeGeom, Polygon geom) {
        nativeGeom.setNumberOfFigures(geom.getNumInteriorRing() + 1);
        int pointOffset = 0;
        int figure = 0;
        addFiguresForPolygon(nativeGeom, geom, pointOffset, figure);
    }

    protected void addFiguresForPolygon(SqlGeometryV1 nativeGeom, Polygon geom, int pointOffset, int figure) {
        pointOffset = addExteriorRing(nativeGeom, geom, pointOffset, figure);
        for (int ring = 0; ring < geom.getNumInteriorRing(); ring++) {
            pointOffset = addInteriorRing(nativeGeom, geom, ring, pointOffset, ++figure);
        }
    }

    private int addInteriorRing(SqlGeometryV1 nativeGeom, Polygon geom, int ring, int pointOffset, int numFigure) {
        LineString ls = geom.getInteriorRingN(ring);
        Figure figure = new Figure(FigureAttribute.InteriorRing, pointOffset);
        nativeGeom.setFigure(numFigure, figure);
        pointOffset += ls.getNumPoints();
        return pointOffset;
    }

    private int addExteriorRing(SqlGeometryV1 nativeGeom, Polygon geom, int offset, int numFigure) {
        LineString shell = geom.getExteriorRing();
        Figure exterior = new Figure(FigureAttribute.ExteriorRing, offset);
        nativeGeom.setFigure(numFigure, exterior);
        offset += shell.getNumPoints();
        return offset;
    }

    protected void encodeShapes(SqlGeometryV1 nativeGeom, Polygon geom) {
        nativeGeom.setNumberOfShapes(1);
        nativeGeom.setShape(0, SHAPE);
    }
}
