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
 * Created by IntelliJ IDEA.
 * User: maesenka
 * Date: Mar 20, 2010
 * Time: 5:22:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class GeometryEquality {

    public static boolean test(Geometry geom1, Geometry geom2) {
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
            return geom1.equals(geom2);
        }
    }
}
