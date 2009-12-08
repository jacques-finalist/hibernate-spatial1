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
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class MultiPolygonEncoder extends AbstractEncoder<MultiPolygon> {

    private PolygonEncoder polygonEncoder = new PolygonEncoder();

    protected void encodeFigures(SqlGeometryV1 nativeGeom, MultiPolygon geom) {
        //count the total number of figures
        int numFigures = 0;
        for (int i = 0; i < geom.getNumGeometries(); i++) {
            Polygon polygon = (Polygon) geom.getGeometryN(i);
            numFigures += getNumFigures(polygon);
        }

        nativeGeom.setNumberOfFigures(numFigures);
        int pointOffset = 0;
        int figure = 0;
        for (int i = 0; i < geom.getNumGeometries(); i++) {
            Polygon polygon = (Polygon) geom.getGeometryN(i);
            polygonEncoder.addFiguresForPolygon(nativeGeom, polygon, pointOffset, figure);
            pointOffset += polygon.getNumPoints();
            figure += getNumFigures(polygon);
        }
    }

    private int getNumFigures(Polygon polygon) {
        return polygon.getNumInteriorRing() + 1;
    }

    protected void encodeShapes(SqlGeometryV1 nativeGeom, MultiPolygon geom) {
        nativeGeom.setNumberOfShapes(geom.getNumGeometries() + 1);
        //first encode the parent
        Shape parent = new Shape(-1, 0, OpenGisType.MULTIPOLYGON);
        final int parentOffset = 0;
        nativeGeom.setShape(parentOffset, parent);
        int figureOffset = 0;
        for (int i = 0; i < geom.getNumGeometries(); i++) {
            Polygon polygon = (Polygon) geom.getGeometryN(i);
            Shape shape = new Shape(parentOffset, figureOffset, OpenGisType.POLYGON);
            nativeGeom.setShape(1 + i, shape);
            figureOffset += getNumFigures(polygon);
        }
    }

    public boolean accepts(Geometry geom) {
        return geom instanceof MultiPolygon;
    }
}
