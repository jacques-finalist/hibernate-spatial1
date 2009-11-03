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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Coordinate;
import org.hibernatespatial.mgeom.MGeometry;
import org.hibernatespatial.mgeom.MCoordinate;

/**
 * @author Karel Maesen, Geovise BVBA.
 *         Date: Nov 2, 2009
 */
class PointEncoder implements Encoder<Point> {

    public SqlGeometryV1 encode(Point geom) {
        SqlGeometryV1 sqlGeom = new SqlGeometryV1();
        sqlGeom.setSrid(geom.getSRID());
        sqlGeom.setIsSinglePoint();
        sqlGeom.setIsValid();
        sqlGeom.setNumberOfPoints(1);
        Coordinate coordinate = geom.getCoordinate();
        if (!is3DPoint(coordinate)) {
            sqlGeom.setHasZValues();
        }
        if (!isMPoint(coordinate)) {
            sqlGeom.setHasMValues();
        }
        sqlGeom.setCoordinate(0, coordinate);
        return sqlGeom;
    }

    private boolean isMPoint(Coordinate coordinate) {
        return (coordinate instanceof MCoordinate) &&
                Double.isNaN(((MCoordinate) coordinate).m);
    }

    private boolean is3DPoint(Coordinate coordinate) {
        return Double.isNaN(coordinate.z);
    }

    public boolean accepts(Geometry geom) {
        return geom instanceof Point;
    }
}
