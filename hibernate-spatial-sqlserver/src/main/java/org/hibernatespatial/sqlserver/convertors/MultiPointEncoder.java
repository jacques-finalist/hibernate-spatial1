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
import com.vividsolutions.jts.geom.MultiPoint;

public class MultiPointEncoder extends AbstractEncoder<MultiPoint> {

    protected void encodeFigures(SqlGeometryV1 nativeGeom, MultiPoint geom) {
        nativeGeom.setNumberOfFigures(geom.getNumPoints());
        for (int figureOffset = 0; figureOffset < geom.getNumPoints(); figureOffset++) {
            //in this special case are figureOffset and pointOffset always equal.
            Figure figure = new Figure(FigureAttribute.Stroke, figureOffset);
            nativeGeom.setFigure(figureOffset, figure);
        }

    }

    protected void encodeShapes(SqlGeometryV1 nativeGeom, MultiPoint geom) {
        nativeGeom.setNumberOfShapes(geom.getNumPoints() + 1);
        int shapeOffset;
        int parentOffset = 0;
        int figureOffset = 0;
        shapeOffset = parentOffset;
        Shape parent = new Shape(-1, figureOffset, OpenGisType.MULTIPOINT);
        nativeGeom.setShape(shapeOffset, parent);
        for (int figure = 0; figure < geom.getNumPoints(); figure++) {
            Shape shape = new Shape(parentOffset, figure, OpenGisType.POINT);
            nativeGeom.setShape(++shapeOffset, shape);
        }
    }

//This will only work when the MGeometryFactory has been repaired.
//Currently the MGeometryFactory transforms all coordinates into MCoordinates.   

//    protected boolean hasMValues(MultiPoint multiPoint){
//        //determine by inspecting an
//        //arbitrary coordinate.
//        Coordinate coordinate = multiPoint.getCoordinate();
//        return coordinate instanceof MCoordinate;
//    }

    public boolean accepts(Geometry geom) {
        return geom instanceof MultiPoint;
    }
}
