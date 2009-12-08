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

package org.hibernatespatial.sqlserver;

import java.util.ArrayList;
import java.util.List;

class GeometryTestCases {

    //TODO -- how to define EMPTY Geomtries in SQL?

    final static List<TestWKT> TEST_WKTS = new ArrayList<TestWKT>();

    static {
        //POINT test cases
        TEST_WKTS.add(new TestWKT(1, "POINT", "POINT(10 5)", 0));
        TEST_WKTS.add(new TestWKT(2, "POINT", "POINT(52.25  2.53)", 4326));
        TEST_WKTS.add(new TestWKT(3, "POINT", "POINT(150000 200000)", 31370));
        TEST_WKTS.add(new TestWKT(4, "POINT", "POINT(10.0 2.0 1.0 3.0)", 4326));
        //LINESTRING test cases
        TEST_WKTS.add(new TestWKT(5, "LINESTRING", "LINESTRING(10.0 5.0, 20.0 15.0)", 4326));
        TEST_WKTS.add(new TestWKT(6, "LINESTRING", "LINESTRING(10.0 5.0, 20.0 15.0, 30.3 22.4, 10 30.0)", 4326));
        TEST_WKTS.add(new TestWKT(7, "LINESTRING", "LINESTRING(10.0 5.0 0.0, 20.0 15.0 3.0)", 4326));
        TEST_WKTS.add(new TestWKT(8, "LINESTRING", "LINESTRING(10.0 5.0 0.0 0.0, 20.0 15.0 3.0 1.0)", 4326));
        TEST_WKTS.add(new TestWKT(9, "LINESTRING", "LINESTRING(10.0 5.0 1, 20.0 15.0 2, 30.3 22.4 5, 10 30.0 2)", 4326));
        TEST_WKTS.add(new TestWKT(10, "LINESTRING",
                "LINESTRING(10.0 5.0 1 1, 20.0 15.0 2 3, 30.3 22.4 5 10, 10 30.0 2 12)", 4326));
        //MULTILINESTRING test cases
        TEST_WKTS.add(new TestWKT(11, "MULTILINESTRING",
                "MULTILINESTRING((10.0 5.0, 20.0 15.0),( 25.0 30.0, 30.0 20.0))", 4326));
        TEST_WKTS.add(new TestWKT(12, "MULTILINESTRING",
                "MULTILINESTRING((10.0 5.0, 20.0 15.0, 30.3 22.4, 10 30.0), (40.0 20.0, 42.0 18.0, 43.0 16.0, 40 14.0))", 4326));
        TEST_WKTS.add(new TestWKT(13, "MULTILINESTRING",
                "MULTILINESTRING((10.0 5.0 1.0, 20.0 15.0 2.0, 30.3 22.4 1.0, 10 30.0 1.0),(40.0 20.0 0.0, 42.0 18.0 1.0, 43.0 16.0 2.0, 40 14.0 3.0))", 4326));
        TEST_WKTS.add(new TestWKT(14, "MULTILINESTRING",
                "MULTILINESTRING((10.0 5.0 1.0, 20.0 15.0 2.0 0.0, 30.3 22.4 1.0 1.0, 10 30.0 1.0 2.0),(40.0 20.0 0.0 3.0, 42.0 18.0 1.0 4.0, 43.0 16.0 2.0 5.0, 40 14.0 3.0 6.0))", 4326));
        TEST_WKTS.add(new TestWKT(15, "MULTILINESTRING",
                "MULTILINESTRING((10.0 5.0 1.0, 20.0 15.0 2.0 0.0, 30.3 22.4 1.0 1.0, 10 30.0 1.0 2.0))", 4326));

        //Polygon
        TEST_WKTS.add(new TestWKT(16, "POLYGON",
                "POLYGON( (0 0, 0 10, 10 10, 10 0, 0 0) )", 4326));
        TEST_WKTS.add(new TestWKT(17, "POLYGON",
                "POLYGON( (0 0 0, 0 10 1, 10 10 1, 10 0 1, 0 0 0) )", 4326));
        TEST_WKTS.add(new TestWKT(18, "POLYGON",
                "POLYGON( (0 0, 0 10, 10 10, 10 0, 0 0), " +
                        "(2 2, 2 5, 5 5,5 2, 2 2))", 4326));

        //MULTIPOLYGON
        TEST_WKTS.add(new TestWKT(20, "MULTIPOLYGON",
                "MULTIPOLYGON( ((10 20, 30 40, 44 50, 10 20)), ((5 0, 20 40, 30 34, 5 0)) )", 4326));
        TEST_WKTS.add(new TestWKT(21, "MULTIPOLYGON",
                "MULTIPOLYGON( ((10 20 1, 30 40 2, 44 50 2, 10 20 1)), ((5 0 0, 20 40 10, 30 34 20, 5 0 0)) )", 4326));
        TEST_WKTS.add(new TestWKT(22, "MULTIPOLYGON",
                "MULTIPOLYGON( " +
                        "( (0 0, 0 50, 50 50, 0 0), (10 10, 10 20, 20 20, 20 10, 10 10) ), " +
                        "((5 0, 20 40, 30 34, 5 0)) )", 4326));


    }
}
