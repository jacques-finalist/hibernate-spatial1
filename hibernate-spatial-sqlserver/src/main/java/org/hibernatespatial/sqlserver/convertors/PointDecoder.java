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
import com.vividsolutions.jts.geom.Point;

/**
 * @author Karel Maesen, Geovise BVBA.
 *         Date: Nov 2, 2009
 */
class PointDecoder extends AbstractDecoder<Point> {

    @Override
    protected OpenGisType getOpenGisType() {
        return OpenGisType.POINT;
    }

    protected Point createNullGeometry() {
        return getGeometryFactory().createPoint((Coordinate) null);
    }

    protected Point createGeometry(SqlGeometryV1 nativeGeom) {
        return createPoint(nativeGeom, 0);
    }

    @Override
    protected Point createGeometry(SqlGeometryV1 nativeGeom, int shapeIndex) {
        int figureOffset = nativeGeom.getStartFigureForShape(shapeIndex);
        int pntOffset = nativeGeom.getStartPointForFigure(figureOffset);
        return createPoint(nativeGeom, pntOffset);
    }

    private Point createPoint(SqlGeometryV1 nativeGeom, int pntOffset){
        return getGeometryFactory().createPoint(nativeGeom.getCoordinate(pntOffset));
    }


}
