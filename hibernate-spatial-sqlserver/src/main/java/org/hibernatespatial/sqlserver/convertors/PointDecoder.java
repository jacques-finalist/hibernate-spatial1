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

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Coordinate;
import org.hibernatespatial.mgeom.MGeometryFactory;

/**
 * @author Karel Maesen, Geovise BVBA.
 *         Date: Nov 2, 2009
 */
class PointDecoder implements Decoder<Point> {

    //TODO -- get GeometryFactory from HSExtension
    private final MGeometryFactory geometryFactory = new MGeometryFactory();

    PointDecoder() {
        //TODO -- check how to construct these items.
    }

    public Point decode(SqlGeometryV1 sqlNative) {
        if (!accepts(sqlNative))
            throw new IllegalArgumentException("Point convertor received object of type " + sqlNative.openGisType());
        if (sqlNative.isEmpty())
            return geometryFactory.createPoint((Coordinate) null);
        Point result = geometryFactory.createPoint(sqlNative.getCoordinate(0));
        setSrid(sqlNative, result);
        return result;
    }

    private void setSrid(SqlGeometryV1 sqlNative, Point result) {
        if (sqlNative.getSrid() != null)
            result.setSRID(sqlNative.getSrid());
    }

    public boolean accepts(SqlGeometryV1 sqlNative) {
        return (sqlNative.openGisType() == OpenGisType.POINT);
    }


}
