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

import java.io.InputStream;
import java.util.*;

public class TestObjects implements List<TestObject> {

    private List<TestObject> testObjects;
    private InputStream in;

    /**
     * Sets the source input stream for the
     */
    public void setSource(InputStream in) {
        throw new UnsupportedOperationException("Not implemented method");
    }

    /**
     * Prepare the list of <code>TestObject</code>s.
     * <p/>
     * Loads all <code>TestObject</code> instances from the source
     */
    public void prepare() {

        //TODO -- how to define EMPTY Geomtries in SQL?
        List<TestObject> objects = new ArrayList<TestObject>();
        //POINT test cases
        objects.add(new TestObject(1, "POINT", "POINT(10 5)", 0));
        objects.add(new TestObject(2, "POINT", "POINT(52.25  2.53)", 4326));
        objects.add(new TestObject(3, "POINT", "POINT(150000 200000)", 31370));
        objects.add(new TestObject(4, "POINT", "POINT(10.0 2.0 1.0 3.0)", 4326));

        //LINESTRING test cases
        objects.add(new TestObject(5, "LINESTRING", "LINESTRING(10.0 5.0, 20.0 15.0)", 4326));
        objects.add(new TestObject(6, "LINESTRING", "LINESTRING(10.0 5.0, 20.0 15.0, 30.3 22.4, 10 30.0)", 4326));
        objects.add(new TestObject(7, "LINESTRING", "LINESTRING(10.0 5.0 0.0, 20.0 15.0 3.0)", 4326));
        objects.add(new TestObject(8, "LINESTRING", "LINESTRING(10.0 5.0 0.0 0.0, 20.0 15.0 3.0 1.0)", 4326));
        objects.add(new TestObject(9, "LINESTRING", "LINESTRING(10.0 5.0 1, 20.0 15.0 2, 30.3 22.4 5, 10 30.0 2)", 4326));
        objects.add(new TestObject(10, "LINESTRING",
                "LINESTRING(10.0 5.0 1 1, 20.0 15.0 2 3, 30.3 22.4 5 10, 10 30.0 2 12)", 4326));

        //MULTILINESTRING test cases
        objects.add(new TestObject(11, "MULTILINESTRING",
                "MULTILINESTRING((10.0 5.0, 20.0 15.0),( 25.0 30.0, 30.0 20.0))", 4326));
        objects.add(new TestObject(12, "MULTILINESTRING",
                "MULTILINESTRING((10.0 5.0, 20.0 15.0, 30.3 22.4, 10 30.0), (40.0 20.0, 42.0 18.0, 43.0 16.0, 40 14.0))", 4326));
        objects.add(new TestObject(13, "MULTILINESTRING",
                "MULTILINESTRING((10.0 5.0 1.0, 20.0 15.0 2.0, 30.3 22.4 1.0, 10 30.0 1.0),(40.0 20.0 0.0, 42.0 18.0 1.0, 43.0 16.0 2.0, 40 14.0 3.0))", 4326));
        objects.add(new TestObject(14, "MULTILINESTRING",
                "MULTILINESTRING((10.0 5.0 1.0 0.0, 20.0 15.0 2.0 0.0, 30.3 22.4 1.0 1.0, 10 30.0 1.0 2.0),(40.0 20.0 0.0 3.0, 42.0 18.0 1.0 4.0, 43.0 16.0 2.0 5.0, 40 14.0 3.0 6.0))", 4326));
        objects.add(new TestObject(15, "MULTILINESTRING",
                "MULTILINESTRING((10.0 5.0 1.0 0.0, 20.0 15.0 2.0 0.0, 30.3 22.4 1.0 1.0, 10 30.0 1.0 2.0))", 4326));

        //Polygon  test cases
        objects.add(new TestObject(16, "POLYGON",
                "POLYGON( (0 0, 0 10, 10 10, 10 0, 0 0) )", 4326));
        objects.add(new TestObject(17, "POLYGON",
                "POLYGON( (0 0 0, 0 10 1, 10 10 1, 10 0 1, 0 0 0) )", 4326));
        objects.add(new TestObject(18, "POLYGON",
                "POLYGON( (0 0, 0 10, 10 10, 10 0, 0 0), " +
                        "(2 2, 2 5, 5 5,5 2, 2 2))", 4326));
        objects.add(new TestObject(19, "POLYGON",
                "POLYGON( (110 110, 110 120, 120 120, 120 110, 110 110) )", 4326));


        //MULTIPOLYGON test cases
        objects.add(new TestObject(20, "MULTIPOLYGON",
                "MULTIPOLYGON( ((10 20, 30 40, 44 50, 10 20)), ((105 100, 120 140, 130 134, 105 100)) )", 4326));
        objects.add(new TestObject(21, "MULTIPOLYGON",
                "MULTIPOLYGON( ((10 20 1, 30 40 2, 44 50 2, 10 20 1)), ((105 100 0, 120 140 10, 130 134 20, 105 100 0)) )", 4326));
        objects.add(new TestObject(22, "MULTIPOLYGON",
                "MULTIPOLYGON( " +
                        "( (0 0, 0 50, 50 50, 50 0, 0 0), (10 10, 10 20, 20 20, 20 10, 10 10) ), " +
                        "((105 100, 120 140, 130 134, 105 100)) )", 4326));

        //MultiPoint test cases
        objects.add(new TestObject(25, "MULTIPOINT", "MULTIPOINT(21 2, 25 5, 30 3)", 4326));
        objects.add(new TestObject(26, "MULTIPOINT", "MULTIPOINT(21 2)", 4326));
        objects.add(new TestObject(27, "MULTIPOINT", "MULTIPOINT(21 2 1, 25 5 2, 30 3 5)", 4326));
        objects.add(new TestObject(28, "MULTIPOINT", "MULTIPOINT(21 2 1 0, 25 5 2 4, 30 3 5 2)", 4326));

        //GeometryCollection test cases
        objects.add(new TestObject(30, "GEOMETRYCOLLECTION", "GEOMETRYCOLLECTION(POINT(4 0), LINESTRING(4 2, 5 3))", 4326));
        objects.add(new TestObject(31, "GEOMETRYCOLLECTION", "GEOMETRYCOLLECTION(POINT(4 0), LINESTRING(4 2, 5 3), POLYGON((0 0, 3 0, 3 3,0 3, 0 0)))", 4326));
        objects.add(new TestObject(32, "GEOMETRYCOLLECTION", "GEOMETRYCOLLECTION(POINT(4 0), LINESTRING(4 2, 5 3), POLYGON((0 0, 3 0, 3 3,0 3, 0 0),(1 1, 2 1, 2 2, 1 2, 1 1)))", 4326));
        objects.add(new TestObject(33, "GEOMETRYCOLLECTION", "GEOMETRYCOLLECTION( MULTIPOINT(21 2, 25 5, 30 3), " +
                "MULTIPOLYGON( ((10 20, 30 40, 44 50, 10 20)), ((105 100, 120 140, 130 134, 105 100)) ), " +
                "MULTILINESTRING((10.0 5.0, 20.0 15.0),( 25.0 30.0, 30.0 20.0)))", 4326));
        objects.add(new TestObject(34, "GEOMETRYCOLLECTION", "GEOMETRYCOLLECTION(POINT(4 0), POINT EMPTY, LINESTRING(4 2, 5 3))", 4326));
        objects.add(new TestObject(35, "GEOMETRYCOLLECTION", "GEOMETRYCOLLECTION(POINT(4 0), LINESTRING EMPTY, LINESTRING(4 2, 5 3))", 4326));
        objects.add(new TestObject(36, "GEOMETRYCOLLECTION", "GEOMETRYCOLLECTION(POINT(4 0), GEOMETRYCOLLECTION EMPTY, LINESTRING(4 2, 5 3))", 4326));
        objects.add(new TestObject(37, "GEOMETRYCOLLECTION", "GEOMETRYCOLLECTION(POINT(4 0), POLYGON EMPTY, LINESTRING(4 2, 5 3))", 4326));
        objects.add(new TestObject(38, "GEOMETRYCOLLECTION", "GEOMETRYCOLLECTION(POINT(4 0), MULTILINESTRING EMPTY, LINESTRING(4 2, 5 3))", 4326));
        objects.add(new TestObject(39, "GEOMETRYCOLLECTION", "GEOMETRYCOLLECTION(POINT(4 0), MULTIPOINT EMPTY, LINESTRING(4 2, 5 3))", 4326));
        objects.add(new TestObject(40, "GEOMETRYCOLLECTION", "GEOMETRYCOLLECTION(POINT(4 0), MULTIPOLYGON EMPTY, LINESTRING(4 2, 5 3))", 4326));


        //NULL Geometries
        objects.add(new TestObject(50, "POINT", "POINT EMPTY", 4326));
        objects.add(new TestObject(51, "LINESTRING", "LINESTRING EMPTY", 0));
        objects.add(new TestObject(52, "POLYGON", "POLYGON EMPTY", 4326));
        objects.add(new TestObject(53, "MULTIPOINT", "MULTIPOINT EMPTY", 0));
        objects.add(new TestObject(54, "MULTILINESTRING", "MULTILINESTRING EMPTY", 0));
        objects.add(new TestObject(55, "MULTIPOLYGON", "MULTIPOLYGON EMPTY", 4326));
        objects.add(new TestObject(56, "GEOMETRYCOLLECTION", "GEOMETRYCOLLECTION EMPTY", 0));


        this.testObjects = objects;

    }

    public int size() {
        return testObjects.size();
    }

    public boolean isEmpty() {
        return testObjects.isEmpty();
    }

    public boolean contains(Object o) {
        return testObjects.contains(o);
    }

    public Iterator<TestObject> iterator() {
        return testObjects.iterator();
    }

    public Object[] toArray() {
        return testObjects.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return testObjects.toArray(a);
    }

    public boolean add(TestObject testObject) {
        return testObjects.add(testObject);
    }

    public boolean remove(Object o) {
        return testObjects.remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        return testObjects.containsAll(c);
    }

    public boolean addAll(Collection<? extends TestObject> c) {
        return testObjects.addAll(c);
    }

    public boolean addAll(int index, Collection<? extends TestObject> c) {
        return testObjects.addAll(index, c);
    }

    public boolean removeAll(Collection<?> c) {
        return testObjects.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return testObjects.retainAll(c);
    }

    public void clear() {
        testObjects.clear();
    }

    public boolean equals(Object o) {
        return testObjects.equals(o);
    }

    public int hashCode() {
        return testObjects.hashCode();
    }

    public TestObject get(int index) {
        return testObjects.get(index);
    }

    public TestObject set(int index, TestObject element) {
        return testObjects.set(index, element);
    }

    public void add(int index, TestObject element) {
        testObjects.add(index, element);
    }

    public TestObject remove(int index) {
        return testObjects.remove(index);
    }

    public int indexOf(Object o) {
        return testObjects.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return testObjects.lastIndexOf(o);
    }

    public ListIterator<TestObject> listIterator() {
        return testObjects.listIterator();
    }

    public ListIterator<TestObject> listIterator(int index) {
        return testObjects.listIterator(index);
    }

    public List<TestObject> subList(int fromIndex, int toIndex) {
        return testObjects.subList(fromIndex, toIndex);
    }
}
