/*
 * $Id:$
 *
 * This file is part of Hibernate Spatial, an extension to the
 * hibernate ORM solution for geographic data.
 *
 * Copyright © 2007-2010 Geovise BVBA
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

package org.hibernatespatial.test;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;

/**
 * This class tests for the equality between geometries.
 * <p/>
 * The notion of geometric equality can differ slightly between
 * spatial databases.
 */
public class GeometryEquality {

    public boolean test(Geometry geom1, Geometry geom2) {
        if (geom1 == null) return geom2 == null;
        if (geom1.isEmpty()) return geom2.isEmpty();
        if (geom1 instanceof GeometryCollection) {
            if (!(geom2 instanceof GeometryCollection)) return false;
            GeometryCollection expectedCollection = (GeometryCollection) geom1;
            GeometryCollection receivedCollection = (GeometryCollection) geom2;
            for (int partIndex = 0; partIndex < expectedCollection.getNumGeometries(); partIndex++) {
                Geometry partExpected = expectedCollection.getGeometryN(partIndex);
                Geometry partReceived = receivedCollection.getGeometryN(partIndex);
                if (!test(partExpected, partReceived)) return false;
            }
            return true;
        } else {
            return testSimpleGeometryEquality(geom1, geom2);
        }
    }

    /**
     * Test whether two geometries, not of type GeometryCollection are equal.
     *
     * @param geom1
     * @param geom2
     * @return
     */
    protected boolean testSimpleGeometryEquality(Geometry geom1, Geometry geom2) {
        return geom1.equals(geom2);
    }
}
