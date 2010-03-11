/*
 * $Id$
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

import java.io.InputStream;
import java.util.*;

/**
 * A <code>TestGeometries</code> instance is a list object
 * that contains all the <code>TestGeometry</code>s that
 * are used in a unit test suite.
 *
 * @author Karel Maesen, Geovise BVBA
 */
public class TestGeometries implements List<TestGeometry> {

    private List<TestGeometry> testGeometries;
    private InputStream in;

    /**
     * Sets the source input stream for the
     */
    public void setSource(InputStream in) {
        throw new UnsupportedOperationException("Not implemented method");
    }

    /**
     * Prepare the list of <code>TestGeometry</code>s.
     * <p/>
     * Loads all <code>TestGeometry</code> instances from the source
     */
    public void prepare() {

        //TODO -- how to define EMPTY Geomtries in SQL?
        List<TestGeometry> geometries = new ArrayList<TestGeometry>();
        //POINT test cases
        geometries.add(new TestGeometry(1, "POINT", "POINT(10 5)", 0));
        geometries.add(new TestGeometry(2, "POINT", "POINT(52.25  2.53)", 4326));
        geometries.add(new TestGeometry(3, "POINT", "POINT(150000 200000)", 31370));
        geometries.add(new TestGeometry(4, "POINT", "POINT(10.0 2.0 1.0 3.0)", 4326));

        //LINESTRING test cases
        geometries.add(new TestGeometry(5, "LINESTRING", "LINESTRING(10.0 5.0, 20.0 15.0)", 4326));
        geometries.add(new TestGeometry(6, "LINESTRING", "LINESTRING(10.0 5.0, 20.0 15.0, 30.3 22.4, 10 30.0)", 4326));
        geometries.add(new TestGeometry(7, "LINESTRING", "LINESTRING(10.0 5.0 0.0, 20.0 15.0 3.0)", 4326));
        geometries.add(new TestGeometry(8, "LINESTRING", "LINESTRING(10.0 5.0 0.0 0.0, 20.0 15.0 3.0 1.0)", 4326));
        geometries.add(new TestGeometry(9, "LINESTRING", "LINESTRING(10.0 5.0 1, 20.0 15.0 2, 30.3 22.4 5, 10 30.0 2)", 4326));
        geometries.add(new TestGeometry(10, "LINESTRING",
                "LINESTRING(10.0 5.0 1 1, 20.0 15.0 2 3, 30.3 22.4 5 10, 10 30.0 2 12)", 4326));

        //MULTILINESTRING test cases
        geometries.add(new TestGeometry(11, "MULTILINESTRING",
                "MULTILINESTRING((10.0 5.0, 20.0 15.0),( 25.0 30.0, 30.0 20.0))", 4326));
        geometries.add(new TestGeometry(12, "MULTILINESTRING",
                "MULTILINESTRING((10.0 5.0, 20.0 15.0, 30.3 22.4, 10 30.0), (40.0 20.0, 42.0 18.0, 43.0 16.0, 40 14.0))", 4326));
        geometries.add(new TestGeometry(13, "MULTILINESTRING",
                "MULTILINESTRING((10.0 5.0 1.0, 20.0 15.0 2.0, 30.3 22.4 1.0, 10 30.0 1.0),(40.0 20.0 0.0, 42.0 18.0 1.0, 43.0 16.0 2.0, 40 14.0 3.0))", 4326));
        geometries.add(new TestGeometry(14, "MULTILINESTRING",
                "MULTILINESTRING((10.0 5.0 1.0 0.0, 20.0 15.0 2.0 0.0, 30.3 22.4 1.0 1.0, 10 30.0 1.0 2.0),(40.0 20.0 0.0 3.0, 42.0 18.0 1.0 4.0, 43.0 16.0 2.0 5.0, 40 14.0 3.0 6.0))", 4326));
        geometries.add(new TestGeometry(15, "MULTILINESTRING",
                "MULTILINESTRING((10.0 5.0 1.0 0.0, 20.0 15.0 2.0 0.0, 30.3 22.4 1.0 1.0, 10 30.0 1.0 2.0))", 4326));

        //Polygon  test cases
        geometries.add(new TestGeometry(16, "POLYGON",
                "POLYGON( (0 0, 0 10, 10 10, 10 0, 0 0) )", 4326));
        geometries.add(new TestGeometry(17, "POLYGON",
                "POLYGON( (0 0 0, 0 10 1, 10 10 1, 10 0 1, 0 0 0) )", 4326));
        geometries.add(new TestGeometry(18, "POLYGON",
                "POLYGON( (0 0, 0 10, 10 10, 10 0, 0 0), " +
                        "(2 2, 2 5, 5 5,5 2, 2 2))", 4326));
        geometries.add(new TestGeometry(19, "POLYGON",
                "POLYGON( (110 110, 110 120, 120 120, 120 110, 110 110) )", 4326));


        //MULTIPOLYGON test cases
        geometries.add(new TestGeometry(20, "MULTIPOLYGON",
                "MULTIPOLYGON( ((10 20, 30 40, 44 50, 10 20)), ((105 100, 120 140, 130 134, 105 100)) )", 4326));
        geometries.add(new TestGeometry(21, "MULTIPOLYGON",
                "MULTIPOLYGON( ((10 20 1, 30 40 2, 44 50 2, 10 20 1)), ((105 100 0, 120 140 10, 130 134 20, 105 100 0)) )", 4326));
        geometries.add(new TestGeometry(22, "MULTIPOLYGON",
                "MULTIPOLYGON( " +
                        "( (0 0, 0 50, 50 50, 50 0, 0 0), (10 10, 10 20, 20 20, 20 10, 10 10) ), " +
                        "((105 100, 120 140, 130 134, 105 100)) )", 4326));

        //MultiPoint test cases
        geometries.add(new TestGeometry(25, "MULTIPOINT", "MULTIPOINT(21 2, 25 5, 30 3)", 4326));
        geometries.add(new TestGeometry(26, "MULTIPOINT", "MULTIPOINT(21 2)", 4326));
        geometries.add(new TestGeometry(27, "MULTIPOINT", "MULTIPOINT(21 2 1, 25 5 2, 30 3 5)", 4326));
        geometries.add(new TestGeometry(28, "MULTIPOINT", "MULTIPOINT(21 2 1 0, 25 5 2 4, 30 3 5 2)", 4326));

        //GeometryCollection test cases
        geometries.add(new TestGeometry(30, "GEOMETRYCOLLECTION", "GEOMETRYCOLLECTION(POINT(4 0), LINESTRING(4 2, 5 3))", 4326));
        geometries.add(new TestGeometry(31, "GEOMETRYCOLLECTION", "GEOMETRYCOLLECTION(POINT(4 0), LINESTRING(4 2, 5 3), POLYGON((0 0, 3 0, 3 3,0 3, 0 0)))", 4326));
        geometries.add(new TestGeometry(32, "GEOMETRYCOLLECTION", "GEOMETRYCOLLECTION(POINT(4 0), LINESTRING(4 2, 5 3), POLYGON((0 0, 3 0, 3 3,0 3, 0 0),(1 1, 2 1, 2 2, 1 2, 1 1)))", 4326));
        geometries.add(new TestGeometry(33, "GEOMETRYCOLLECTION", "GEOMETRYCOLLECTION( MULTIPOINT(21 2, 25 5, 30 3), " +
                "MULTIPOLYGON( ((10 20, 30 40, 44 50, 10 20)), ((105 100, 120 140, 130 134, 105 100)) ), " +
                "MULTILINESTRING((10.0 5.0, 20.0 15.0),( 25.0 30.0, 30.0 20.0)))", 4326));
        geometries.add(new TestGeometry(34, "GEOMETRYCOLLECTION", "GEOMETRYCOLLECTION(POINT(4 0), POINT EMPTY, LINESTRING(4 2, 5 3))", 4326));
        geometries.add(new TestGeometry(35, "GEOMETRYCOLLECTION", "GEOMETRYCOLLECTION(POINT(4 0), LINESTRING EMPTY, LINESTRING(4 2, 5 3))", 4326));
        geometries.add(new TestGeometry(36, "GEOMETRYCOLLECTION", "GEOMETRYCOLLECTION(POINT(4 0), GEOMETRYCOLLECTION EMPTY, LINESTRING(4 2, 5 3))", 4326));
        geometries.add(new TestGeometry(37, "GEOMETRYCOLLECTION", "GEOMETRYCOLLECTION(POINT(4 0), POLYGON EMPTY, LINESTRING(4 2, 5 3))", 4326));
        geometries.add(new TestGeometry(38, "GEOMETRYCOLLECTION", "GEOMETRYCOLLECTION(POINT(4 0), MULTILINESTRING EMPTY, LINESTRING(4 2, 5 3))", 4326));
        geometries.add(new TestGeometry(39, "GEOMETRYCOLLECTION", "GEOMETRYCOLLECTION(POINT(4 0), MULTIPOINT EMPTY, LINESTRING(4 2, 5 3))", 4326));
        geometries.add(new TestGeometry(40, "GEOMETRYCOLLECTION", "GEOMETRYCOLLECTION(POINT(4 0), MULTIPOLYGON EMPTY, LINESTRING(4 2, 5 3))", 4326));


        //NULL Geometries
        geometries.add(new TestGeometry(50, "POINT", "POINT EMPTY", 4326));
        geometries.add(new TestGeometry(51, "LINESTRING", "LINESTRING EMPTY", 0));
        geometries.add(new TestGeometry(52, "POLYGON", "POLYGON EMPTY", 4326));
        geometries.add(new TestGeometry(53, "MULTIPOINT", "MULTIPOINT EMPTY", 0));
        geometries.add(new TestGeometry(54, "MULTILINESTRING", "MULTILINESTRING EMPTY", 0));
        geometries.add(new TestGeometry(55, "MULTIPOLYGON", "MULTIPOLYGON EMPTY", 4326));
        geometries.add(new TestGeometry(56, "GEOMETRYCOLLECTION", "GEOMETRYCOLLECTION EMPTY", 0));


        this.testGeometries = geometries;

    }

    public int size() {
        return testGeometries.size();
    }

    public boolean isEmpty() {
        return testGeometries.isEmpty();
    }

    public boolean contains(Object o) {
        return testGeometries.contains(o);
    }

    public Iterator<TestGeometry> iterator() {
        return testGeometries.iterator();
    }

    public Object[] toArray() {
        return testGeometries.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return testGeometries.toArray(a);
    }

    public boolean add(TestGeometry testGeometry) {
        return testGeometries.add(testGeometry);
    }

    public boolean remove(Object o) {
        return testGeometries.remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        return testGeometries.containsAll(c);
    }

    public boolean addAll(Collection<? extends TestGeometry> c) {
        return testGeometries.addAll(c);
    }

    public boolean addAll(int index, Collection<? extends TestGeometry> c) {
        return testGeometries.addAll(index, c);
    }

    public boolean removeAll(Collection<?> c) {
        return testGeometries.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return testGeometries.retainAll(c);
    }

    public void clear() {
        testGeometries.clear();
    }

    public boolean equals(Object o) {
        return testGeometries.equals(o);
    }

    public int hashCode() {
        return testGeometries.hashCode();
    }

    public TestGeometry get(int index) {
        return testGeometries.get(index);
    }

    public TestGeometry set(int index, TestGeometry element) {
        return testGeometries.set(index, element);
    }

    public void add(int index, TestGeometry element) {
        testGeometries.add(index, element);
    }

    public TestGeometry remove(int index) {
        return testGeometries.remove(index);
    }

    public int indexOf(Object o) {
        return testGeometries.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return testGeometries.lastIndexOf(o);
    }

    public ListIterator<TestGeometry> listIterator() {
        return testGeometries.listIterator();
    }

    public ListIterator<TestGeometry> listIterator(int index) {
        return testGeometries.listIterator(index);
    }

    public List<TestGeometry> subList(int fromIndex, int toIndex) {
        return testGeometries.subList(fromIndex, toIndex);
    }
}
