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
 
package org.hibernatespatial.test;

import java.sql.SQLException;

import junit.framework.TestCase;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernatespatial.test.model.LineStringEntity;
import org.hibernatespatial.test.model.MultiLineStringEntity;
import org.hibernatespatial.test.model.PolygonEntity;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;

/**
 * @author maesenka
 * 
 */
public class TestCRUD extends TestCase {

    private SessionFactory factory;

    private final static int COORDARRAY_LENGTH = 100;

    private GeometryFactory geomFactory = new GeometryFactory(
            new PrecisionModel(PrecisionModel.FLOATING), 31370);

    public void setUpBeforeClass() throws SQLException,
            ClassNotFoundException, ParseException {

        // set up hibernate and register Spatialtest as a persistent entity
        System.out.println("Setting up Hibernate");
        Configuration config = new Configuration();
        config.configure();
        config.addClass(LineStringEntity.class);
        config.addClass(PolygonEntity.class);
        config.addClass(MultiLineStringEntity.class);

        // build the session factory
        // Settings settings = config.buildSettings();
        // SpatialExtension.setDefaultSpatialDialect((SpatialDialect)settings.getDialect());
        factory = config.buildSessionFactory();

    }

    public void tearDownAfterClass() {
        factory.close();
        factory = null;
    }

    private long saveObject(Object obj) throws Exception {
        Session session = factory.openSession();

        Transaction tx = null;
        long id = -1;
        try {
            tx = session.beginTransaction();
            session.save(obj);

            if (obj instanceof LineStringEntity)
                id = ((LineStringEntity) obj).getId();

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        } finally {
            session.close();
        }
        return id;
    }

    private Object retrieveObject(Class clazz, long id) {
        Session session = factory.openSession();
        Object obj = session.get(clazz, id);
        session.close();
        return obj;
    }

    public void testSaveLineStringEntity() throws Exception {
        LineStringEntity line = new LineStringEntity();
        Coordinate[] coordinates = new Coordinate[COORDARRAY_LENGTH];

        double startx = 4319.0;
        double starty = 53255.0;

        for (int i = 0; i < COORDARRAY_LENGTH; i++) {
            coordinates[i] = new Coordinate(startx + i, starty + i);
        }

        Geometry geom = geomFactory.createLineString(coordinates);
        line.setGeometry(geom);
        line.setName("Added by TestCRUD");
        long id = saveObject(line);
        LineStringEntity retrieved = (LineStringEntity) retrieveObject(
                LineStringEntity.class, id);
        // check if we retrieve all the same stuff
        assertTrue(line.getGeometry().equals(retrieved.getGeometry()));
        // assertEquals(line.getGeometry(),
        // retrieved.getGeometry()); DOES NOT WORK:
        // AssertEquals doesn' t work because in JTS 1.7 Geometry has a
        // method with signature boolean equals(Geometry) which does NOT
        // override
        // equals(Object). This last method is called from assertEquals.

        assertEquals(line.getId(), retrieved.getId());
        assertEquals(line.getName(), retrieved.getName());
    }

    public void testSaveNullLineStringEntity() throws Exception {
        LineStringEntity line = new LineStringEntity();

        line.setGeometry(null);
        line.setName("Null geom Added by TestCRUD");
        long id = saveObject(line);

        // System.out.println("id: " + id);

        LineStringEntity retrieved = (LineStringEntity) retrieveObject(
                LineStringEntity.class, id);
        // check if we retrieve a null geometry
        assertNull(retrieved.getGeometry());

    }

}
