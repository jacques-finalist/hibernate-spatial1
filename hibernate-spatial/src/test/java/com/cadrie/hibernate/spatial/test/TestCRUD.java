package com.cadrie.hibernate.spatial.test;

import java.sql.SQLException;

import junit.framework.TestCase;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import com.cadrie.hibernate.spatial.test.model.LineStringEntity;
import com.cadrie.hibernate.spatial.test.model.MultiLineStringEntity;
import com.cadrie.hibernate.spatial.test.model.PolygonEntity;
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
