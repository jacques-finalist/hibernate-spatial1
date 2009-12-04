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
import com.vividsolutions.jts.geom.LineString;

class LineStringEncoder extends AbstractEncoder<LineString> {

    private final static Figure LINESTRING_FIGURE = new Figure(FigureAttribute.Stroke, 0);

    private final static Shape LINESTRING_SHAPE = new Shape(-1, 0, OpenGisType.LINESTRING);


    protected void encodePoints(SqlGeometryV1 nativeGeom, Geometry geom) {
        if (geom.getNumPoints() == 2) {
            encodeSingleLineSegment(nativeGeom, geom);
        } else {
            super.encodePoints(nativeGeom, geom);
        }
    }

    private void encodeSingleLineSegment(SqlGeometryV1 nativeGeom, Geometry geom) {
        nativeGeom.setIsSingleLineSegment();
        Coordinate[] coords = geom.getCoordinates();
        setCoordinate(nativeGeom, 0, coords[0]);
        setCoordinate(nativeGeom, 1, coords[1]);
    }

    protected void setCoordinate(SqlGeometryV1 nativeGeom, int idx, Coordinate coordinate) {
        if (!nativeGeom.hasZValues() && !Double.isNaN(coordinate.z)) {
            nativeGeom.setHasZValues();
        }
        nativeGeom.setCoordinate(idx, coordinate);
    }

    protected void encodeFigures(SqlGeometryV1 nativeGeom, LineString geometry) {
        nativeGeom.setNumberOfFigures(1);
        nativeGeom.setFigure(0, LINESTRING_FIGURE);
    }

    protected void encodeShapes(SqlGeometryV1 nativeGeom, LineString geometry) {
        nativeGeom.setNumberOfShapes(1);
        nativeGeom.setShape(0, LINESTRING_SHAPE);
    }


    public boolean accepts(Geometry geom) {
        return geom instanceof LineString;
    }
}
