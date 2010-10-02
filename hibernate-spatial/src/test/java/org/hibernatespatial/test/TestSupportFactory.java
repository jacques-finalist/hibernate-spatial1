package org.hibernatespatial.test;

import org.hibernate.cfg.Configuration;
import org.hibernate.testing.junit.functional.FunctionalTestCase;


/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Sep 30, 2010
 */
public interface TestSupportFactory {

    public DataSourceUtils createDataSourceUtil(Configuration configuration);

    public TestData createTestData(FunctionalTestCase testcase);


    public GeometryEquality createGeometryEquality();

    public AbstractExpectationsFactory createExpectationsFactory(DataSourceUtils dataSourceUtils);
}