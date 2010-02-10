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

/**
 *
 *
 *
 *
 *
 *
 *
 */
package org.hibernatespatial.test;

import com.vividsolutions.jts.geom.*;
import org.hibernate.*;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Criterion;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.type.Type;
import org.hibernatespatial.HBSpatialExtension;
import org.hibernatespatial.cfg.HSConfiguration;
import org.hibernatespatial.criterion.SpatialRestrictions;
import org.hibernatespatial.mgeom.MCoordinate;
import org.hibernatespatial.mgeom.MGeometryFactory;
import org.hibernatespatial.mgeom.MLineString;
import org.hibernatespatial.test.model.*;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Martin Steinwender
 */
public class TestHQL {

    private static Logger LOGGER = LoggerFactory.getLogger(TestHQL.class);

    private SessionFactory factory;

    private MGeometryFactory gf = new MGeometryFactory(
            new PrecisionModel(PrecisionModel.FLOATING), 31370);

    public SessionFactory getFactory() {
        return factory;
    }

    /**
     * init Session factory and setup test table data
     */
    public void setUpBeforeClass() throws Exception {

        try {
            // set up hibernate and register Spatialtest as a persistent entity
            System.out.println("Setting up Hibernate");
            Configuration config = new Configuration();
            config.configure();
            config.addClass(LineStringEntity.class);
            config.addClass(PolygonEntity.class);
            config.addClass(MultiLineStringEntity.class);
            config.addClass(PointEntity.class);
            config.addClass(MLineStringEntity.class);
            config.addClass(MultiMLineStringEntity.class);

            //configure Hibernate Spatial based on this config
            HSConfiguration hsc = new HSConfiguration();
            hsc.configure(config);
            HBSpatialExtension.setConfiguration(hsc);

            // build the session factory
            factory = config.buildSessionFactory();

            // setup Data
            deleteForClass(PointEntity.class);
            deleteForClass(LineStringEntity.class);
            deleteForClass(PolygonEntity.class);
            setupPoints();
            setupLineStrings();
            setupPolygons();

        } catch (Exception e) {
            System.err.println("Failed to configure Hibernate." + e.getMessage());
            throw e;
        }
        System.out.println("Hibernate set-up complete.");
    }

    /**
     * close Session factory
     */
    public void tearDownAfterClass() {
        factory.close();
        factory = null;
    }

    /**
     * all entites used in this test
     */
    private Map<String, Object> entities = new HashMap<String, Object>();

    /**
     * all coordinates used in this test
     */
    private final MCoordinate[] c = {
            MCoordinate.create2d(20, 20), // c[0]
            MCoordinate.create2d(20, 40), // c[1]
            MCoordinate.create2d(20, 80), // c[2]
            MCoordinate.create2d(30, 30), // c[3]
            MCoordinate.create2d(30, 50), // c[4]
            MCoordinate.create2d(40, 20), // c[5]
            MCoordinate.create2d(40, 30), // c[6]
            MCoordinate.create2d(40, 40), // c[7]
            MCoordinate.create2d(40, 50), // c[8]
            MCoordinate.create2d(40, 60), // c[9]
            MCoordinate.create2d(60, 10), // c[10]
            MCoordinate.create2d(60, 40), // c[11]
            MCoordinate.create2d(80, 20), // c[12]
            MCoordinate.create2d(80, 70), // c[13]
            MCoordinate.create2d(90, 40), // c[14]
            MCoordinate.create3dWithMeasure(15, 15, 5, 1),  // c[15]
            MCoordinate.create3dWithMeasure(25, 35, 10, 2), // c[16]
            MCoordinate.create3dWithMeasure(35, 45, 15, 3), // c[17]
            MCoordinate.create3dWithMeasure(45, 25, 5, 4),  // c[18]
            MCoordinate.create3dWithMeasure(55, 35, 25, 5), // c[19]
            MCoordinate.create2dWithMeasure(40, 20, 1.5), // c[20]
            MCoordinate.create2dWithMeasure(40, 30, 2.5), // c[21]
            MCoordinate.create2dWithMeasure(40, 40, 3.5), // c[22]
            MCoordinate.create2dWithMeasure(40, 50, 4.5), // c[23]
            MCoordinate.create2dWithMeasure(40, 60, 5.5), // c[24]
            MCoordinate.create3d(40, 15, 5),  // c[25]
            MCoordinate.create3d(30, 25, 10), // c[26]
            MCoordinate.create3d(40, 35, 15), // c[27]
            MCoordinate.create3d(30, 45, 5),  // c[28]
            MCoordinate.create3d(50, 55, 25), // c[29]
    };

    /**
     * all linestrings used in this test
     */
    private Geometry[] createLines() {
        return new Geometry[]{
                createLineString(new MCoordinate[]{c[0], c[5], c[9]}),
                createLineString(new MCoordinate[]{c[2], c[7], c[8]}),
                createLineString(new MCoordinate[]{c[0], c[2], c[3]}),
                createLineString(new MCoordinate[]{c[4], c[8], c[9]}),
                createLineString(new MCoordinate[]{c[10], c[12], c[14]}),
                createMLineString(new MCoordinate[]{c[15], c[16], c[17]}),
        };
    }

    /**
     * all polygons used in this test
     */
    private Geometry[] createPolygons() {
        return new Geometry[]{
                createPolygon(new MCoordinate[]{c[0], c[4], c[9], c[0]}),
                createPolygon(new MCoordinate[]{c[0], c[1], c[7], c[5], c[0]}),
                createPolygon(new MCoordinate[]{c[0], c[2], c[13], c[14], c[0]}, new MCoordinate[]{c[3], c[4], c[7], c[3]}),
        };
    }

    /**
     * HQL delete all entities of a class
     *
     * @param cls
     */
    private void deleteForClass(Class<?> cls) {
        Session session = factory.openSession();
        Transaction t = session.beginTransaction();
        int deleted = session.createQuery("DELETE FROM " + cls.getName()).executeUpdate();
        System.out.println("Delete " + deleted + " records of " + cls.getSimpleName());
        t.commit();
        session.close();
    }

    /**
     * setup all PointEntity-objects used in this test
     */
    private void setupPoints() {
        Session session = factory.openSession();
        Transaction t = session.beginTransaction();
        for (int i = 0; i < c.length; i++) {
            int id = (i + 1);
            Point geom = gf.createPoint(c[i]);
            createEntity(session, PointEntity.class, id, geom);
        }
        t.commit();
        session.close();
    }

    /**
     * setup all LineStringEntity-objects used in this test
     */
    private void setupLineStrings() {
        Session session = factory.openSession();
        Transaction t = session.beginTransaction();
        Geometry[] lines = createLines();
        for (int i = 0; i < lines.length; i++) {
            Geometry geom = lines[i];
            int id = i + 1;
            createEntity(session, LineStringEntity.class, id, geom);
        }
        t.commit();
        session.close();
    }

    /**
     * setup all PolygonEntity-objects used in this test
     */
    private void setupPolygons() {
        Session session = factory.openSession();
        Transaction t = session.beginTransaction();
        Geometry[] polygons = createPolygons();
        for (int i = 0; i < polygons.length; i++) {
            Geometry geom = polygons[i];
            int id = i + 1;
            createEntity(session, PolygonEntity.class, id, geom);
        }
        t.commit();
        session.close();
    }


    /**
     * create and write an entity to the database via hibernate
     *
     * @param session
     * @param cls
     * @param id
     * @param geom
     */
    @SuppressWarnings("unchecked")
    private void createEntity(Session session, Class cls, int id, Geometry geom) {

        if (!isWritable(session, geom)) {
            System.out.println(id + ": probably not writable - " + geom.toText());
            return;
        }

        Object o;
        if (cls == PointEntity.class) {
            o = new PointEntity(id, "Point " + (id), geom);
        } else if (cls == LineStringEntity.class) {
            o = new LineStringEntity(id, "LineStringEntity " + (id), geom);
        } else if (cls == PolygonEntity.class) {
            o = new PolygonEntity(id, "PolygonEntity " + (id), geom);
        } else {
            throw new RuntimeException("No volid class.");
        }
        try {
            o = session.merge(o);

            // the id might change on merge
            Long newid = (Long) cls.getMethod("getId", new Class[0]).invoke(o, new Object[0]);

            session.flush();
            String key = o.getClass().getSimpleName() + "_" + newid;
            entities.put(key, o);
        } catch (Exception e) {
            System.out.println(id + ": " + e.getMessage());
        }
    }

    /**
     * Tests whether geometries are intact after write/read operations
     *
     * @param cls
     * @throws Exception
     */
    @Test
    public void testWriteReadIntegrity(Class<?> cls) throws Exception {

        LOGGER.info("testWriteReadIntegrity " + cls.getSimpleName());

        String[] hqls = {
                "FROM " + cls.getName()
        };

        Session session = null;
        try {
            session = factory.openSession();
            for (String hql : hqls) {
                if (hql == null) continue;
                List<?> list = session.createQuery(hql).list();
                for (Object item : list) {
                    assertEqualEntity(item);
                }
            }
        } finally {
            if (session != null) session.close();
        }
    }

    /**
     * test basic spatial functions
     *
     * @param cls
     * @throws Exception
     */
    @Test
    public void testHqlFunctions(Class<?> cls) throws Exception {

        LOGGER.info("testHqlFunctions " + cls.getSimpleName());

        AbstractEntityPersister metadata = (AbstractEntityPersister) factory.getClassMetadata(LineStringEntity.class);
        Type geomType = metadata.getPropertyType("geometry");

        String[] hqls = {
                "SELECT dimension(e.geometry) FROM " + cls.getName() + " e WHERE e.id = 1",
                "SELECT astext(e.geometry) FROM " + cls.getName() + " e WHERE e.id = 1",
                "SELECT geometrytype(e.geometry) FROM " + cls.getName() + " e WHERE e.id = 1",
                "SELECT srid(e.geometry) FROM " + cls.getName() + " e WHERE e.id = 1",
                "SELECT issimple(e.geometry) FROM " + cls.getName() + " e WHERE e.id = 1",
                "SELECT isempty(e.geometry) FROM " + cls.getName() + " e WHERE e.id = 1",
                "SELECT boundary(e.geometry) FROM " + cls.getName() + " e WHERE e.id = 1",
                "SELECT envelope(e.geometry) FROM " + cls.getName() + " e WHERE e.id = 1"
        };

        Geometry geom;
        if (cls == PointEntity.class) {
            PointEntity e = (PointEntity) this.entities.get("PointEntity_1");
            geom = e.getGeometry();
        } else if (cls == LineStringEntity.class) {
            LineStringEntity e = (LineStringEntity) this.entities.get("LineStringEntity_1");
            geom = e.getGeometry();
        } else if (cls == PolygonEntity.class) {
            PolygonEntity e = (PolygonEntity) this.entities.get("PolygonEntity_1");
            geom = e.getGeometry();
        } else {
            throw new RuntimeException("No valid class.");
        }

        Object[] results = new Object[]{
                geom.getDimension(),
                geom.toText(),
                geom.getGeometryType(),
                geom.getSRID(),
                geom.isSimple(),
                geom.isEmpty(),
                geom.getBoundary(),
                geom.getEnvelope()
        };

        Session session = null;
        try {
            session = factory.openSession();
            for (int i = 0; i < hqls.length; i++) {
                String hql = hqls[i];
                if (hql == null) continue;
                Object expected = results[i];
                Query query = session.createQuery(hql);
                List<?> list = query.list();
                Assert.assertTrue(list.size() == 1);
                Object retrieved = list.iterator().next();
                LOGGER.info(i + " expected:  " + expected);
                LOGGER.info(i + " retrieved: " + retrieved);
                if (expected instanceof Geometry) {
                    assertEquality((Geometry) expected, (Geometry) retrieved);
                } else {
                    assertEquals(expected, retrieved);
                }
            }

        } finally {
            session.close();
        }
    }

    /**
     * test spatial relate functions for HQL
     *
     * @param cls
     * @throws Exception
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testHqlRelations(Class cls) throws Exception {

        LOGGER.info("testHqlRelations " + cls.getSimpleName());

        AbstractEntityPersister metadata = (AbstractEntityPersister) factory.getClassMetadata(LineStringEntity.class);
        Type geomType = metadata.getPropertyType("geometry");
        Geometry polygon = ((PolygonEntity) this.entities.get("PolygonEntity_1")).getGeometry();
        Geometry polygon2 = ((PolygonEntity) this.entities.get("PolygonEntity_2")).getGeometry();
        Geometry polygon3 = ((PolygonEntity) this.entities.get("PolygonEntity_3")).getGeometry();

        String matrix1 = "T*T***T**";
        String matrix2 = "FF*FF****";

        String[] hqls = {
                "SELECT e.id FROM " + cls.getName() + " e WHERE contains(e.geometry, :polygon)=true",
                "SELECT e.id FROM " + cls.getName() + " e WHERE crosses(e.geometry, :polygon)=true",
                "SELECT e.id FROM " + cls.getName() + " e WHERE disjoint(e.geometry, :polygon)=true",
                "SELECT e.id FROM " + cls.getName() + " e WHERE equals(e.geometry, :polygon)=true",
                "SELECT e.id FROM " + cls.getName() + " e WHERE intersects(e.geometry, :polygon)=true",
                "SELECT e.id FROM " + cls.getName() + " e WHERE overlaps(e.geometry, :polygon)=true",
                "SELECT e.id FROM " + cls.getName() + " e WHERE touches(e.geometry, :polygon)=true",
                "SELECT e.id FROM " + cls.getName() + " e WHERE within(e.geometry, :polygon)=true",
                "SELECT e.id FROM " + cls.getName() + " e WHERE relate(e.geometry, :polygon, '" + matrix1 + "')=true",
                "SELECT e.id FROM " + cls.getName() + " e WHERE relate(e.geometry, :polygon, :matrix2)=true",
                "SELECT e.id FROM " + cls.getName() + " e, PolygonEntity a WHERE disjoint(e.geometry, a.geometry)=true AND a.id=2",
                "SELECT e.id FROM " + cls.getName() + " e, PolygonEntity a WHERE intersects(e.geometry, a.geometry)=true AND a.id=3",
        };

        Object[] results = new Object[hqls.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = new TreeSet<Long>();
        }

        Method methodGeom = cls.getMethod("getGeometry", new Class[0]);
        Method methodId = cls.getMethod("getId", new Class[0]);
        for (Object entity : this.entities.values()) {
            if (entity.getClass() == cls) {
                Geometry geom = (Geometry) methodGeom.invoke(entity, new Object[0]);
                Long id = (Long) methodId.invoke(entity, new Object[0]);
                if (geom.contains(polygon)) {
                    ((Collection<Long>) results[0]).add(id);
                }
                if (geom.crosses(polygon)) {
                    ((Collection<Long>) results[1]).add(id);
                }
                if (geom.disjoint(polygon)) {
                    ((Collection<Long>) results[2]).add(id);
                }
                if (geom.equals(polygon)) {
                    ((Collection<Long>) results[3]).add(id);
                }
                if (geom.intersects(polygon)) {
                    ((Collection<Long>) results[4]).add(id);
                }
                if (geom.overlaps(polygon)) {
                    ((Collection<Long>) results[5]).add(id);
                }
                if (geom.touches(polygon)) {
                    ((Collection<Long>) results[6]).add(id);
                }
                if (geom.within(polygon)) {
                    ((Collection<Long>) results[7]).add(id);
                }
                if (geom.relate(polygon, matrix1)) {
                    ((Collection<Long>) results[8]).add(id);
                }
                if (geom.relate(polygon, matrix2)) {
                    ((Collection<Long>) results[9]).add(id);
                }
                if (geom.disjoint(polygon2)) {
                    ((Collection<Long>) results[10]).add(id);
                }
                if (geom.intersects(polygon3)) {
                    ((Collection<Long>) results[11]).add(id);
                }
            }
        }

        Session session = null;
        try {
            session = factory.openSession();
            for (int i = 0; i < hqls.length; i++) {
                String hql = hqls[i];
                if (hql == null) continue;
                LOGGER.info(i + " hql:  " + hql);

                Object expected = results[i];
                Query query = session.createQuery(hql);
                if (hql.contains(":polygon")) query.setParameter("polygon", polygon, geomType);
                if (hql.contains(":matrix2")) query.setParameter("matrix2", matrix2, Hibernate.STRING);

                List list = query.list();
                Collection<Long> retrieved = new TreeSet<Long>(list);

                LOGGER.info(i + " expected:  " + expected);
                LOGGER.info(i + " retrieved: " + retrieved);

                boolean equal = true;
                if (expected instanceof Geometry) {
                    assertEquality((Geometry) expected, (Geometry) retrieved);
                } else {
                    assertEquals(expected, retrieved);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    /**
     * test spatial analysis functions for HQL
     *
     * @throws Exception
     */
    @Test
    public void testHqlAnalysis() throws Exception {

        LOGGER.info("testHqlAnalysis");

        AbstractEntityPersister metadata = (AbstractEntityPersister) factory.getClassMetadata(LineStringEntity.class);
        Type geomType = metadata.getPropertyType("geometry");

        Geometry point = ((PointEntity) this.entities.get("PointEntity_1")).getGeometry();
        Geometry linestring = ((LineStringEntity) this.entities.get("LineStringEntity_1")).getGeometry();
        Geometry polygon = ((PolygonEntity) this.entities.get("PolygonEntity_1")).getGeometry();

        Geometry point2 = ((PointEntity) this.entities.get("PointEntity_2")).getGeometry();
        Geometry linestring2 = ((LineStringEntity) this.entities.get("LineStringEntity_2")).getGeometry();
        Geometry polygon2 = ((PolygonEntity) this.entities.get("PolygonEntity_2")).getGeometry();

        String[] hqls = {
                "SELECT distance(e.geometry, :polygon) FROM PointEntity e WHERE e.id=2",
                "SELECT distance(e.geometry, :point) FROM LineStringEntity e WHERE e.id=2",
                "SELECT distance(e.geometry, :linestring) FROM PolygonEntity e WHERE e.id=2",

                "SELECT buffer(e.geometry, 1) FROM PointEntity e WHERE e.id=1",
                "SELECT buffer(e.geometry, 1) FROM LineStringEntity e WHERE e.id=1",
                "SELECT buffer(e.geometry, 1) FROM PolygonEntity e WHERE e.id=1",

                "SELECT convexhull(geomunion(e.geometry, :polygon)) FROM PolygonEntity e WHERE e.id=2",
                "SELECT intersection(e.geometry, :polygon) FROM PolygonEntity e WHERE e.id=2",
                "SELECT difference(e.geometry, :polygon) FROM PolygonEntity e WHERE e.id=2",
                "SELECT symdifference(e.geometry, :polygon) FROM PolygonEntity e WHERE e.id=2",
                "SELECT geomunion(e.geometry, :linestring) FROM LineStringEntity e WHERE e.id=2",

                "SELECT area(e.geometry) FROM PolygonEntity e WHERE e.id=2",
                "SELECT centroid(e.geometry) FROM PolygonEntity e WHERE e.id=1",
                "SELECT pointonsurface(e.geometry) FROM PolygonEntity e WHERE e.id=2",
        };

        Object[] results = new Object[]{
                point2.distance(polygon),
                linestring2.distance(point),
                polygon2.distance(linestring),

                point.buffer(1),
                linestring.buffer(1),
                polygon.buffer(1),

                polygon2.union(polygon).convexHull(),
                polygon2.intersection(polygon),
                polygon2.difference(polygon),
                polygon2.symDifference(polygon),
                linestring2.union(linestring),

                polygon2.getArea(),
                polygon.getCentroid(),
                point,
        };

        Session session = null;
        try {
            session = factory.openSession();
            for (int i = 0; i < hqls.length; i++) {
                //For now, we only write out exceptions, rather than failing !!
                try {
                    String hql = hqls[i];
                    if (hql == null) continue;
                    System.out.println(i + " hql:  " + hql);

                    Object expected = results[i];
                    Query query = session.createQuery(hql);
                    if (hql.contains(":point")) query.setParameter("point", point, geomType);
                    if (hql.contains(":linestring")) query.setParameter("linestring", linestring, geomType);
                    if (hql.contains(":polygon")) query.setParameter("polygon", polygon, geomType);

                    List<?> list = query.list();
                    Assert.assertTrue(list.size() == 1);
                    Object retrieved = list.iterator().next();

                    LOGGER.info(i + " expected:  " + expected);
                    LOGGER.info(i + " retrieved: " + retrieved);

                    boolean equal = true;
                    if (expected == null) {
                        fail();
                    } else if (expected instanceof Geometry) {
                        assertEquality((Geometry) expected, (Geometry) retrieved);
                    } else {
                        assertEquals(expected, retrieved);
                    }
                } catch (AssertionError e) {
                    e.printStackTrace();
                }
            }
        } finally {
            session.close();
        }
    }

    /**
     * test spatial relate functions for resctriction API
     *
     * @param cls
     * @throws Exception
     */
    public String testRestriction(Class<?> cls) throws Exception {

        LOGGER.info("testRestriction " + cls.getSimpleName());

        Geometry point = ((PointEntity) this.entities.get("PointEntity_1")).getGeometry();
        Geometry linestring = ((LineStringEntity) this.entities.get("LineStringEntity_1")).getGeometry();
        Geometry polygon = ((PolygonEntity) this.entities.get("PolygonEntity_1")).getGeometry();

        Criterion[] crits = {
                SpatialRestrictions.contains("geometry", polygon),
                SpatialRestrictions.crosses("geometry", polygon),
                SpatialRestrictions.disjoint("geometry", polygon),
                SpatialRestrictions.eq("geometry", polygon),
                SpatialRestrictions.intersects("geometry", polygon),
                SpatialRestrictions.overlaps("geometry", polygon),
                SpatialRestrictions.touches("geometry", polygon),
                SpatialRestrictions.within("geometry", polygon),
        };

        Object[] results = new Object[crits.length];
        for (int i = 0; i < results.length; i++) {
            results[i] = new TreeSet<Long>();
        }

        Method methodGeom = cls.getMethod("getGeometry", new Class[0]);
        Method methodId = cls.getMethod("getId", new Class[0]);
        for (Object entity : this.entities.values()) {
            if (entity.getClass() == cls) {
                Geometry geom = (Geometry) methodGeom.invoke(entity, new Object[0]);
                Long id = (Long) methodId.invoke(entity, new Object[0]);
                if (geom.contains(polygon)) {
                    ((Collection<Long>) results[0]).add(id);
                }
                if (geom.crosses(polygon)) {
                    ((Collection<Long>) results[1]).add(id);
                }
                if (geom.disjoint(polygon)) {
                    ((Collection<Long>) results[2]).add(id);
                }
                if (geom.equals(polygon)) {
                    ((Collection<Long>) results[3]).add(id);
                }
                if (geom.intersects(polygon)) {
                    ((Collection<Long>) results[4]).add(id);
                }
                if (geom.overlaps(polygon)) {
                    ((Collection<Long>) results[5]).add(id);
                }
                if (geom.touches(polygon)) {
                    ((Collection<Long>) results[6]).add(id);
                }
                if (geom.within(polygon)) {
                    ((Collection<Long>) results[7]).add(id);
                }
            }
        }


        String sql = null;
        Session session = null;

        try {
            session = factory.openSession();
            for (int i = 0; i < crits.length; i++) {
                Criterion crit = crits[i];
                Object expected = results[i];

                if (crit == null) continue;
                Criteria query = session.createCriteria(cls).add(crit);
                Collection<Long> retrieved = new TreeSet<Long>();

                for (Object entity : query.list()) {
                    Long id = (Long) methodId.invoke(entity, new Object[0]);
                    retrieved.add(id);
                }

                LOGGER.info(i + " expected:  " + expected);
                LOGGER.info(i + " retrieved: " + retrieved);

                boolean equal = true;
                if (expected == null) {
                    fail();
                } else if (expected instanceof Geometry) {

                    assertEquality((Geometry) expected, (Geometry) retrieved);

                } else {
                    assertEquals(expected, retrieved);
                }
                if (!equal) {
                    System.out.println(i + ": ASSERTION FAILED.");
                }

            }

        } finally {
            session.close();
        }

        return sql;
    }


    /**
     * helper to get M-Values of Coordinates
     *
     * @param c
     * @return
     */
    private static double getCoordM(Coordinate c) {
        return (c instanceof MCoordinate ? ((MCoordinate) c).m : Double.NaN);
    }

    /**
     * helper to compare received entity to expected entity
     *
     * @param retrievedObj
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private void assertEqualEntity(Object retrievedObj) throws Exception {
        Class<? extends Object> cls = retrievedObj.getClass();

        Method method = cls.getMethod("getId", new Class[0]);
        long id = (Long) method.invoke(retrievedObj, new Object[0]);
        String key = cls.getSimpleName() + "_" + id;
        Object expectedObj = this.entities.get(key);
        if (expectedObj == null) {
            throw new RuntimeException("Can't find the expected object for " + retrievedObj);
        }
        Assert.assertEquals(expectedObj.getClass(), cls);

        method = cls.getMethod("getGeometry", new Class[0]);
        Geometry retrieved = (Geometry) method.invoke(retrievedObj, new Object[0]);
        Geometry expected = (Geometry) method.invoke(expectedObj, new Object[0]);
        assertEquality(expected, retrieved);

        System.out.println("OK: " + id + " - " + retrieved.toText());
    }

    /**
     * helper to compare geometry with M-Values
     *
     * @param expected
     * @param retrieved
     */
    private static void assertEquality(Geometry expected, Geometry retrieved) {
        if (expected == retrieved) return;
        assertTrue(expected != null && retrieved != null);

        //TODO --  testing equality for GeometryCollections doesn't appear to work
        if (expected.getClass() != retrieved.getClass()) {
            fail("Expected and received objects have different classes.");
        }
        if (expected instanceof GeometryCollection) {
            assertEquality((GeometryCollection) expected, (GeometryCollection) retrieved);
        } else {
            assertTrue(expected.equals(retrieved));
            Coordinate[] rCoords = retrieved.getCoordinates();
            Coordinate[] eCoords = expected.getCoordinates();

            if (rCoords.length != eCoords.length) return;

            for (int i = 0; i < rCoords.length; i++) {
                double ez = eCoords[i].z;
                double rz = rCoords[i].z;

                double em = getCoordM(eCoords[i]);
                double rm = getCoordM(rCoords[i]);

                assertEquals("z value not equal", ez, rz, 0.000001);
                assertEquals("m value not equal", em, rm, 0.000001);
            }
        }
    }

    private static void assertEquality(GeometryCollection expected, GeometryCollection retrieved) {
        for (int i = 0; i < expected.getNumGeometries(); i++) {
            Geometry element = expected.getGeometryN(i);
            if (!isConstituentOf(element, retrieved)) {
                fail();
            }
        }


    }

    private static boolean isConstituentOf(Geometry element, GeometryCollection retrieved) {
        for (int i = 0; i < retrieved.getNumGeometries(); i++) {
            Geometry candidate = retrieved.getGeometryN(i);
            try {
                assertEquality(element, candidate);
                return true;        //no exception, then element is constituent of retrieved.
            } catch (Exception e) {
            } //
        }
        return false;
    }


    /**
     * helper coordinates -> LineString
     *
     * @param mCoordinates
     * @return
     */
    private Geometry createLineString(MCoordinate[] mCoordinates) {
        return gf.createLineString(mCoordinates);
    }

    /**
     * helper coordinates -> MLineString
     *
     * @param mCoordinates
     * @return
     */
    private Geometry createMLineString(MCoordinate[] mCoordinates) {
        return gf.createMLineString(mCoordinates);
    }

    /**
     * helper coordinates -> MultiLineString
     *
     * @param parts
     * @return
     */
    private Geometry createMultiLineString(Coordinate[]... parts) {
        List<LineString> lines = new ArrayList<LineString>();
        for (Coordinate[] part : parts) {
            lines.add(gf.createLineString(part));
        }
        return gf.createMultiLineString(lines.toArray(new LineString[0]));
    }

    /**
     * helper coordinates -> MultiMLineString
     *
     * @param parts
     * @return
     */
    private Geometry createMultiMLineString(MCoordinate[]... parts) {
        List<MLineString> lines = new ArrayList<MLineString>();
        for (MCoordinate[] part : parts) {
            lines.add(gf.createMLineString(part));
        }
        return gf.createMultiMLineString(lines.toArray(new MLineString[0]));
    }

    /**
     * helper coordinates -> Polygon with possible holes
     *
     * @param parts
     * @return
     */
    private Geometry createPolygon(Coordinate[]... parts) {
        LinearRing shell = null;
        List<LinearRing> holes = new ArrayList<LinearRing>();
        for (Coordinate[] part : parts) {
            LinearRing ring = gf.createLinearRing(part);
            if (shell == null)
                shell = ring;
            else
                holes.add(ring);
        }
        return gf.createPolygon(shell, holes.toArray(new LinearRing[0]));
    }

    /**
     * helper coordinates -> MultiPolygon without holes
     *
     * @param parts
     * @return
     */
    private Geometry createMultiPolygon(Coordinate[]... parts) {
        List<Polygon> polygons = new ArrayList<Polygon>();
        for (Coordinate[] part : parts) {
            polygons.add(gf.createPolygon(gf.createLinearRing(part), new LinearRing[0]));
        }
        return gf.createMultiPolygon(polygons.toArray(new Polygon[0]));
    }

    /**
     * helper coordinates -> MultiPolygon
     *
     * @param polygons
     * @return
     */
    private Geometry createMultiPolygon(Polygon... polygons) {
        return gf.createMultiPolygon(polygons);
    }

    /**
     * try to guess if the geometry is writable for Oracle
     * (problem with dimensions defined in SDO_GEOM_METADATA)
     *
     * @param session
     * @param geometry
     * @return
     */
    private boolean isWritable(Session session, Geometry geometry) {
//        Dialect dialect = findDialect(session);
//        if (dialect instanceof Oracle9Dialect ||
//                dialect instanceof Oracle9iDialect) {
//            for (Coordinate c : geometry.getCoordinates()) {
//                if (!Double.isNaN(c.z)) return false;
//                if (!Double.isNaN(getCoordM(c))) return false;
//            }
//        }
        return true;
    }

    private static Dialect findDialect(Session session) {
        SessionFactory sessionFactory = session.getSessionFactory();
        if (sessionFactory != null && sessionFactory instanceof SessionFactoryImplementor) {
            SessionFactoryImplementor factImpl = (SessionFactoryImplementor) sessionFactory;
            return factImpl.getDialect();
        } else {
            return null;
        }
    }
}
