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
        int offset = 0;
        offset = addExteriorRing(nativeGeom, geom, offset);
        for (int ring = 0; ring < geom.getNumInteriorRing(); ring++) {
            offset = addInteriorRing(nativeGeom, geom, offset, ring);
        }
    }

    private int addInteriorRing(SqlGeometryV1 nativeGeom, Polygon geom, int offset, int i) {
        LineString ls = geom.getInteriorRingN(i);
        Figure figure = new Figure(FigureAttribute.InteriorRing, offset);
        nativeGeom.setFigure(i + 1, figure);
        offset += ls.getNumPoints();
        return offset;
    }

    private int addExteriorRing(SqlGeometryV1 nativeGeom, Polygon geom, int offset) {
        LineString shell = geom.getExteriorRing();
        Figure exterior = new Figure(FigureAttribute.ExteriorRing, offset);
        nativeGeom.setFigure(0, exterior);
        offset += shell.getNumPoints();
        return offset;
    }

    protected void encodeShapes(SqlGeometryV1 nativeGeom, Polygon geom) {
        nativeGeom.setNumberOfShapes(1);
        nativeGeom.setShape(0, SHAPE);
    }
}
