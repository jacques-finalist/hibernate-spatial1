/**
 *
 */
package org.hibernatespatial.test.pojo;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernatespatial.HBSpatialExtension;
import org.hibernatespatial.cfg.HSConfiguration;
import org.hibernatespatial.pojo.*;
import org.hibernatespatial.postgis.PostgisExpressionTemplate;
import org.hibernatespatial.test.DataSourceUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.*;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Karel Maesen
 */
public class TestAutoMapper {

    private static TestPojoUtility delegate;

    private static String dbUrl;

    private static String DBNAME = "test";

    private static HSConfiguration config;

    private static Connection conn;


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        config = new HSConfiguration();
        config.configure();

        dbUrl = "jdbc:postgresql://localhost:5432/" + DBNAME;
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(dbUrl, "postgres", "");

        } catch (Exception e) {
            e.printStackTrace();
        }
        delegate = new TestPojoUtility();
        delegate.setUpBeforeClass(config, conn);
    }


    @Test
    public void testList() throws SQLException {
        DataSourceUtils dataSourceUtils = new DataSourceUtils("hibernate-spatial-postgis-test.properties", new PostgisExpressionTemplate());
        dataSourceUtils.insertTestData();
        Session session = delegate.getSessionFactory().openSession();
        try {

            List<String[]> tables = AutoMapper.getMappedTables();

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
            dataSourceUtils.deleteTestData();
        }

    }

    @Test
    public void test_expose_attributes_of_mapped_class(){
        List<String> attributes = AutoMapper.getAttributes(null, null, "geomtest");
        assertTrue(attributes.contains("id"));
        assertTrue(attributes.contains("type"));
        assertTrue(attributes.contains("geom"));
        assertEquals(3,attributes.size());
    }

    @Test
    public void test_expose_id_attribute() throws MissingIdentifierException {
        String identifier = AutoMapper.getIdAttribute(null, null, "geomtest");
        assertEquals("id",identifier);
    }

    @Test
    public void test_expose_geom_attribute() throws GeometryNotFoundException {
        String geometryAttribute = AutoMapper.getGeometryAttribute(null, null, "geomtest");
        assertEquals("geom",geometryAttribute);
    }

    @Test
    public void test_expose_setter_for_attribute() throws GeometryNotFoundException {
        String methodName = AutoMapper.getAttributeSetterName(null, null, "geomtest", "geom");
        assertEquals("setGeom",methodName);
        methodName = AutoMapper.getAttributeSetterName(null, null, "geomtest", "id");
        assertEquals("setId",methodName);
        methodName = AutoMapper.getAttributeSetterName(null, null, "geomtest", "type");
        assertEquals("setType",methodName);
    }

    @Test
    public void test_expose_getter_for_attribute() throws GeometryNotFoundException {
        String methodName = AutoMapper.getAttributeGetterName(null, null, "geomtest", "geom");
        assertEquals("getGeom",methodName);
        methodName = AutoMapper.getAttributeGetterName(null, null, "geomtest", "id");
        assertEquals("getId",methodName);
        methodName = AutoMapper.getAttributeGetterName(null, null, "geomtest", "type");
        assertEquals("getType",methodName);
    }

    @Test
    public void test_getter_for_non_existing_attribute_throws_illegal_argument_exception() throws GeometryNotFoundException {

        try {
            String methodName = AutoMapper.getAttributeGetterName(null, null, "geomtest", "nonexisting");
            fail();
        }catch(IllegalArgumentException e){

        }catch(Exception e){
            fail();
        }

        try {
            String methodName = AutoMapper.getAttributeGetterName(null, null, "geomtest", null);
            fail();
        }catch(IllegalArgumentException e){

        }catch(Exception e){
            fail();
        }
    }

    @Test
    public void test_fail_on_composite_primary_key() throws Exception {
        try {
            //prepare test table
            PreparedStatement pstmt = conn.prepareStatement("create table mucomp (c1 int, c2 int, c3 char(10))");
            pstmt.execute();
            pstmt = conn.prepareStatement("alter table mucomp add primary key (c1, c2)");
            pstmt.execute();

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
            PreparedStatement delTabStmt = conn.prepareStatement("drop table mucomp");
            delTabStmt.execute();
        }

    }

     @Test
    public void test_fail_on_no_primary_key() throws Exception {
        try {
            //prepare test table
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
            PreparedStatement delTabStmt = conn.prepareStatement("drop table nopkey");
            delTabStmt.execute();
        }

    }


    @Test
    public void test_unique_index_accepted_as_primary_key() throws Exception {
        try {
            //prepare test table
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
            PreparedStatement delTabStmt = conn.prepareStatement("drop table unik");
            delTabStmt.execute();
        }

    }

    @AfterClass
    public static void cleanUp() throws SQLException {
        conn.close();
    }

}
