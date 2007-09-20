/**
 * $Id$
 *
 * This file is part of Spatial Hibernate, an extension to the 
 * hibernate ORM solution for geographic data. 
 *  
 * Copyright © 2007 K.U. Leuven LRD, Spatial Applications Division, Belgium
 *
 * This work was partially supported by the European Commission, 
 * under the 6th Framework Programme, contract IST-2-004688-STP.
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
 * For more information, visit: http://www.cadrie.com/
 */
 
package org.hibernatespatial.test.model;

import java.lang.reflect.Constructor;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Settings;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

/**
 * Generates test data for the unit tests
 * 
 * TODO : - generate polygons with holes. TODO : - add points, multipoints,
 * multipolygons
 * 
 */
public class DataGenerator {

    static final private int SIZE = 1000;

    static final private double minCoordValue = 0.0d;

    static final private double maxCoordValue = 100000.0d;

    static final private int maxNumGeom = 10;

    static final private int maxNumCoords = 20;

    // create the geometryfactory, with floating-point precision model, and
    // Belgian lambert
    static final private GeometryFactory geomFactory = new GeometryFactory(
            new PrecisionModel(), 31370);

    public void generate() {
        // set up hibernate and register Spatialtest as a persistent entity
        Configuration config = new Configuration();
        config.configure();
        config.addClass(LineStringEntity.class);
        config.addClass(MultiLineStringEntity.class);
        config.addClass(PolygonEntity.class);
        config.addClass(PointEntity.class);
        config.addClass(MultiPointEntity.class);
        config.addClass(MultiPolygonEntity.class);

        Settings settings = config.buildSettings();
        System.out.println("Generating Data for Dialect: "
                + settings.getDialect().getClass().getName());
        // HBSpatialExtension.setDefaultSpatialDialect((SpatialDialect) settings
        // .getDialect());

        // build the session factory
        SessionFactory factory = config.buildSessionFactory();

        // generate the data
        generateData(LineStringEntity.class, factory, new LineStringCreator());
        generateData(MultiLineStringEntity.class, factory,
                new MultiLineStringCreator());
        generateData(PolygonEntity.class, factory, new PolygonCreator());

        factory.close();

    }

    private static void generateData(Class entityClass,
            SessionFactory factory, GeomCreator creator) {
        Session session = factory.openSession();

        Transaction tx = null;
        try {
            System.out.println("Writing " + entityClass.getSimpleName());
            for (int i = 0; i < SIZE; i++) {
                tx = session.beginTransaction();
                Geometry geom = creator.create();

                Constructor constructor = entityClass
                        .getConstructor(new Class[] { java.lang.Long.TYPE,
                                String.class, Geometry.class });
                Object entity = constructor.newInstance(new Object[] { i,
                        "feature " + i, geom });

                session.save(entity);
                tx.commit();
            }
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            throw new RuntimeException("Failed loading data of type "
                    + entityClass.getSimpleName(), e);
        } finally {
            session.close();
            session = null;
        }

    }

    private static double getRandomCoordinateValue() {
        return (maxCoordValue - minCoordValue) * Math.random();
    }

    private static int getRandomNumGeoms() {
        double dn = 1 + (maxNumGeom - 1) * Math.random();
        int num = Math.round((float) dn);
        return num;
    }

    private static int getRandomNumCoords(int minValue) {
        double dn = minValue + (maxNumCoords - minValue) * Math.random();
        int num = Math.round((float) dn);
        return num;
    }

    private static Coordinate getRandomCoordinate() {
        double x = getRandomCoordinateValue();
        double y = getRandomCoordinateValue();
        return new Coordinate(x, y);
    }

    interface GeomCreator<T extends Geometry> {
        public T create();
    }

    private class LineStringCreator implements GeomCreator<LineString> {
        public LineString create() {

            int numCoords = getRandomNumCoords(2);
            Coordinate[] coordinates = new Coordinate[numCoords];
            for (int i = 0; i < numCoords; i++) {
                coordinates[i] = getRandomCoordinate();
            }
            return geomFactory.createLineString(coordinates);
        }
    }

    private class MultiLineStringCreator implements
            GeomCreator<MultiLineString> {
        public MultiLineString create() {
            // at least two linestrings, otherwise some databases like
            // Oracle will
            // store multilinestring as linestring in stead.
            int numGeoms = 1 + getRandomNumGeoms();
            LineStringCreator lsc = new LineStringCreator();
            LineString[] lines = new LineString[numGeoms];
            for (int i = 0; i < numGeoms; i++) {
                lines[i] = lsc.create();
            }
            return geomFactory.createMultiLineString(lines);
        }
    }

    // This create small boxes
    // TODO -- find a better way to generate random linear rings
    private class LinearRingCreator implements GeomCreator<LinearRing> {
        public LinearRing create() {

            int numCoords = 4;
            Coordinate[] coordinates = new Coordinate[numCoords + 1];

            coordinates[0] = getRandomCoordinate();
            coordinates[1] = new Coordinate(coordinates[0].x,
                    coordinates[0].y + 10.0d);
            coordinates[2] = new Coordinate(coordinates[0].x + 10.0d,
                    coordinates[0].y + 10.0d);
            coordinates[3] = new Coordinate(coordinates[0].x + 10.0d,
                    coordinates[0].y);
            coordinates[numCoords] = coordinates[0];
            LinearRing lr = geomFactory.createLinearRing(coordinates);
            return lr;
        }
    }

    private class PolygonCreator implements GeomCreator<Polygon> {
        public Polygon create() {
            LinearRing lr = (new LinearRingCreator()).create();
            Polygon pg = geomFactory.createPolygon(lr, null);
            return pg;
        }
    }

    // TO DO -- add polygons with holes!!

}
