/**
 * $Id$
 *
 * This file is part of Hibernate Spatial, an extension to the 
 * hibernate ORM solution for geographic data. 
 *
 * Copyright © 2007 Geovise BVBA
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
 * For more information, visit: http://www.hibernatespatial.org/
 */
package org.hibernatespatial.test.model;

import com.vividsolutions.jts.geom.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Settings;
import org.hibernatespatial.mgeom.MCoordinate;
import org.hibernatespatial.mgeom.MGeometryFactory;
import org.hibernatespatial.mgeom.MLineString;
import org.hibernatespatial.mgeom.MultiMLineString;

import java.lang.reflect.Constructor;

/**
 * Generates test data for the unit tests
 * <p/>
 * TODO : - generate polygons with holes. TODO : - add points, multipoints,
 * multipolygons
 *
 * @Depracated
 */
public class DataGenerator {

    static final private int SIZE = 1000;

    static final private double minCoordValue = 0.0d;

    static final private double maxCoordValue = 100000.0d;

    static final private int maxNumGeom = 10;

    static final private int maxNumCoords = 20;

    // create the geometryfactory, with floating-point precision model, and
    // Belgian lambert
    static final private MGeometryFactory geomFactory = new MGeometryFactory(
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
        config.addClass(MLineStringEntity.class);
        config.addClass(MultiMLineStringEntity.class);

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
        generateData(PointEntity.class, factory, new PointCreator());
        generateData(MLineStringEntity.class, factory, new MLineStringCreator());
        generateData(MultiMLineStringEntity.class, factory, new MultiMLineStringCreator());
        factory.close();


    }

    private static void generateData(Class entityClass, SessionFactory factory,
                                     GeomCreator creator) {
        Session session = factory.openSession();

        Transaction tx = null;
        try {
            System.out.println("Writing " + entityClass.getSimpleName());
            for (int i = 0; i < SIZE; i++) {
                tx = session.beginTransaction();
                Geometry geom = creator.create();

                Constructor constructor = entityClass
                        .getConstructor(new Class[]{java.lang.Long.TYPE,
                                String.class, Geometry.class});
                Object entity = constructor.newInstance(new Object[]{i,
                        "feature " + i, geom});

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

    private class MLineStringCreator implements GeomCreator<MLineString> {
        public MLineString create() {
            int numCoords = getRandomNumCoords(2);
            MCoordinate[] coordinates = new MCoordinate[numCoords];
            double mValue = 0.0d;
            for (int i = 0; i < numCoords; i++) {
                Coordinate rc = getRandomCoordinate();
                MCoordinate mrc = new MCoordinate(rc);
                if (i > 0) {
                    mValue += coordinates[i - 1].distance(rc);
                }
                mrc.m = mValue;
                coordinates[i] = mrc;
            }
            return geomFactory.createMLineString(coordinates);
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

    private class MultiMLineStringCreator implements GeomCreator<MultiMLineString> {
        public MultiMLineString create() {
            int numGeoms = 1 + getRandomNumGeoms();
            MLineStringCreator mlsc = new MLineStringCreator();
            MLineString[] mlines = new MLineString[numGeoms];
            for (int i = 0; i < numGeoms; i++) {
                mlines[i] = mlsc.create();
            }
            return geomFactory.createMultiMLineString(mlines);
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

    private class PointCreator implements GeomCreator<Point> {
        public static final double numPoints = 1000.;

        public Point create() {
            Coordinate c = getRandomCoordinate();
            return geomFactory.createPoint(c);
        }
    }

}
