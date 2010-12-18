/**
 *
 */
package org.hibernatespatial.testsuite;

import org.dom4j.Document;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.classic.Session;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernatespatial.HBSpatialExtension;
import org.hibernatespatial.pojo.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Karel Maesen
 */
public class TestAutoMapper extends SpatialFunctionalTestCase {

    private static Logger LOGGER = LoggerFactory.getLogger(TestAutoMapper.class);

    private static boolean WRITE_MAPPING = false;

    private SessionFactory sessionFactory;

    public TestAutoMapper(String string) {
        super(string);
    }

    /**
     * Builds a sessionFactory containing mappings for Automapped entities.
     *
     * @param config
     * @return
     */
    public SessionFactory buildSessionFactory(Configuration config) {
        Connection conn = null;
        try {
            conn = getConnection();
            List<String> tables = new ArrayList<String>();
            tables.add("geomtest");
            Document mappingdocument;
            mappingdocument = AutoMapper.map(conn, null, null, tables);
            if (WRITE_MAPPING) writeToFile(mappingdocument);
            config.addXML(mappingdocument.asXML());
            return config.buildSessionFactory();
        } catch (Exception e) {
            try {
                conn.close();
            } catch (SQLException e1) {
            }
            throw new RuntimeException(e);
        }

    }

    private void writeToFile(Document mappingdocument) {
        try {
            File f = File.createTempFile("testsuite-suite-hs-automapper", ".xml");
            FileWriter writer = new FileWriter(f);
            mappingdocument.write(writer);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void prepareTest() {
        super.prepareTest();
        if (!appliesTo(getDialect())) return;
        insertTestData();
        Configuration cfg = getCfg();
        cfg.setProperty(Environment.HBM2DDL_AUTO, "update");
        sessionFactory = buildSessionFactory(cfg);
    }

    public void cleanupTest() {
        this.sessionFactory.close();
    }

    public Session openMappedSession() {
        return sessionFactory.openSession();
    }

    @Test
    public void test_automapper() throws Exception {

        //todo -- use appliesTo()-method
        if (!(this.getDialect() instanceof PostgreSQLDialect)) {
            return;
        }

        Session session = openMappedSession();
        try {

            List<String[]> tables = AutoMapper.getMappedTables();

            assertFalse(tables.isEmpty());
            for (String[] tncomp : tables) {
                Class entityClass = AutoMapper.getClass(tncomp[0], tncomp[1], tncomp[2]);
                Criteria c = session.createCriteria(entityClass);
                List results = c.list();
                assertTrue(results.size() > 1);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            session.close();
        }


//    test_expose_attributes_of_mapped_class() {
        List<String> attributes = AutoMapper.getAttributes(null, null, "geomtest");
        assertTrue(attributes.contains("id"));
        assertTrue(attributes.contains("type"));
        assertTrue(attributes.contains("geom"));
        assertEquals(3, attributes.size());

        //test_expose_id_attribute() throws MissingIdentifierException
        String identifier = AutoMapper.getIdAttribute(null, null, "geomtest");
        assertEquals("id", identifier);


        //   test_expose_geom_attribute() throws GeometryNotFoundException {
        String geometryAttribute = AutoMapper.getGeometryAttribute(null, null, "geomtest");
        assertEquals("geom", geometryAttribute);


//    test_expose_setter_for_attribute() throws GeometryNotFoundException {
        String methodName = AutoMapper.getAttributeSetterName(null, null, "geomtest", "geom");
        assertEquals("setGeom", methodName);
        methodName = AutoMapper.getAttributeSetterName(null, null, "geomtest", "id");
        assertEquals("setId", methodName);
        methodName = AutoMapper.getAttributeSetterName(null, null, "geomtest", "type");
        assertEquals("setType", methodName);

//     test_expose_getter_for_attribute() throws GeometryNotFoundException {
        methodName = AutoMapper.getAttributeGetterName(null, null, "geomtest", "geom");
        assertEquals("getGeom", methodName);
        methodName = AutoMapper.getAttributeGetterName(null, null, "geomtest", "id");
        assertEquals("getId", methodName);
        methodName = AutoMapper.getAttributeGetterName(null, null, "geomtest", "type");
        assertEquals("getType", methodName);

//        test_getter_for_non_existing_attribute_throws_illegal_argument_exception() throws GeometryNotFoundException {

        try {
            methodName = AutoMapper.getAttributeGetterName(null, null, "geomtest", "nonexisting");
            fail();
        } catch (IllegalArgumentException e) {

        } catch (Exception e) {
            fail();
        }

        try {
            methodName = AutoMapper.getAttributeGetterName(null, null, "geomtest", null);
            fail();
        } catch (IllegalArgumentException e) {

        } catch (Exception e) {
            fail();
        }


        //test_fail_on_composite_primary_key() throws Exception {
        Connection conn = null;
        try {
            conn = getConnection();
            //prepare testsuite-suite table
            PreparedStatement pstmt = conn.prepareStatement("create table mucomp (c1 int not null, c2 int not null, c3 char(10))");
            pstmt.execute();
            pstmt = conn.prepareStatement("alter table mucomp add primary key (c1, c2)");
            pstmt.execute();
            conn.commit();

            NamingStrategy naming = new SimpleNamingStrategy();
            TypeMapper typeMapper = new TypeMapper(HBSpatialExtension.getDefaultSpatialDialect().getDbGeometryTypeName());
            DatabaseMetaData dmd = conn.getMetaData();
            FeatureMapper fMapper = new FeatureMapper(naming, typeMapper);
            try {
                ClassInfo cInfo = fMapper.createClassInfo(null, "public", "mucomp", dmd);
                fail("Attempt to map class with multiple primary keys");
            } catch (TableNotFoundException e) {
                fail("TableNotFoundException thrown");
            } catch (MissingIdentifierException e) {
                //OK
            } catch (Exception e) {
                fail("MissingIdentifierException expected");
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (conn != null) {
                PreparedStatement delTabStmt = conn.prepareStatement("drop table mucomp");
                delTabStmt.execute();
                conn.commit();
                conn.close();
            }
        }


        //test_fail_on_no_primary_key() throws Exception {
        try {
            conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement("create table nopkey (c1 int, c2 int, c3 char(10))");
            pstmt.execute();

            NamingStrategy naming = new SimpleNamingStrategy();
            TypeMapper typeMapper = new TypeMapper(HBSpatialExtension.getDefaultSpatialDialect().getDbGeometryTypeName());
            DatabaseMetaData dmd = conn.getMetaData();
            FeatureMapper fMapper = new FeatureMapper(naming, typeMapper);
            try {
                ClassInfo cInfo = fMapper.createClassInfo(null, "public", "nopkey", dmd);
                fail("Attempt to map class with no primary key");
            } catch (TableNotFoundException e) {
                fail("TableNotFoundException thrown");
            } catch (MissingIdentifierException e) {
                //OK
            } catch (Exception e) {
                fail("MissingIdentifierException expected");
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (conn != null) {
                PreparedStatement delTabStmt = conn.prepareStatement("drop table nopkey");
                delTabStmt.execute();
                conn.close();
            }

        }


//   test_unique_index_accepted_as_primary_key() throws Exception {

        try {
            conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement("create table unik (c1 int, c2 int, c3 char(10))");
            pstmt.execute();
            pstmt = conn.prepareStatement("create unique index un_idx on unik(c1)");
            pstmt.execute();

            NamingStrategy naming = new SimpleNamingStrategy();
            TypeMapper typeMapper = new TypeMapper(HBSpatialExtension.getDefaultSpatialDialect().getDbGeometryTypeName());
            DatabaseMetaData dmd = conn.getMetaData();
            FeatureMapper fMapper = new FeatureMapper(naming, typeMapper);
            try {
                ClassInfo cInfo = fMapper.createClassInfo(null, "public", "unik", dmd);
                assertEquals("c1", cInfo.getIdAttribute().getColumnName());

            } catch (TableNotFoundException e) {
                fail("TableNotFoundException thrown");
            } catch (MissingIdentifierException e) {
                fail("Unique index not accepted as primary key");
            } catch (Exception e) {
                fail("MissingIdentifierException expected");
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (conn != null) {
                PreparedStatement delTabStmt = conn.prepareStatement("drop table unik");
                delTabStmt.execute();
                conn.close();
            }
        }

    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}
