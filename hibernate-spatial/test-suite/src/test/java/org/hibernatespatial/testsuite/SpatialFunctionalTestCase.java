package org.hibernatespatial.testsuite;

import com.vividsolutions.jts.geom.Geometry;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.cfg.Configuration;
import org.hibernate.testing.junit.functional.FunctionalTestCase;
import org.hibernatespatial.test.*;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Sep 30, 2010
 */
public abstract class SpatialFunctionalTestCase extends FunctionalTestCase {

    protected TestData testData;
    protected DataSourceUtils dataSourceUtils;
    protected GeometryEquality geometryEquality;
    protected AbstractExpectationsFactory expectationsFactory;


    public SpatialFunctionalTestCase(String string) {
        super(string);
    }

    public void prepareTest(){
        try {
            TestSupportFactory tsFactory = TestSupportFactories.instance().getTestSupportFactory(getDialect());
            Configuration cfg =  getCfg();
            dataSourceUtils = tsFactory.createDataSourceUtil(cfg);
            expectationsFactory = tsFactory.createExpectationsFactory(dataSourceUtils);
            testData = tsFactory.createTestData(this);
            geometryEquality = tsFactory.createGeometryEquality();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getBaseForMappings() {
        return "org/hibernatespatial/test/";
    }

    public String[] getMappings() {
        return new String[]{"GeomEntity.hbm.xml"};
    }

    abstract protected Logger getLogger();

    /**
     * Adds the query results to a Map.
     *
     * Each row is added as a Map entry with the first column the key,
     * and the second the value. It is assumed that the first column is an
     * identifier of a type assignable to Integer.
     *
     * @param result map of
     * @param query the source Query
     * @param <T> type of the second column in the query results
     */
    protected <T> void addQueryResults(Map<Integer, T> result, Query query) {
        List<Object[]> rows = (List<Object[]>) query.list();
        if (rows.size() == 0) {
            getLogger().warn("No results returned for query!!");
        }
        for (Object[] row : rows) {
            Integer id = (Integer) row[0];
            T val = (T) row[1];
            result.put(id, val);
        }
    }

    protected <T> void compare(Map<Integer, T> expected, Map<Integer, T> received) {
        for (Integer id : expected.keySet()) {
            getLogger().debug("Case :" + id);
            getLogger().debug("expected: " + expected.get(id));
            getLogger().debug("received: " + received.get(id));
            compare(id, expected.get(id), received.get(id));
        }
    }


    protected void compare(Integer id, Object expected, Object received) {
        assertTrue(expected != null && received != null);
        if (expected instanceof byte[]) {
            assertArrayEquals("Failure on testsuite-suite for case " + id, (byte[]) expected, (byte[]) received);

        } else if (expected instanceof Geometry) {
            if (!(received instanceof Geometry))
                fail("Expected a Geometry, but received an object of type " + received.getClass().getCanonicalName());
            assertTrue("Failure on testsuite-suite for case " + id, geometryEquality.test((Geometry) expected, (Geometry) received));

        } else {
            if (expected instanceof Long) {
                assertEquals("Failure on testsuite-suite for case " + id, ((Long) expected).intValue(), received);
            } else {
                assertEquals("Failure on testsuite-suite for case " + id, expected, received);
            }
        }
    }



}
