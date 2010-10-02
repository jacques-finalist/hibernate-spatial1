package org.hibernatespatial.testsuite.cfg;

import com.vividsolutions.jts.geom.PrecisionModel;
import org.hibernatespatial.HBSpatialExtension;
import org.hibernatespatial.cfg.HSConfiguration;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class HSConfigurationTest {

    @Test
    public void testConfigureFailure() {
        HSConfiguration config = new HSConfiguration();
        config.configure("non-existing-file");
    }

    @Test
    public void testHBSpatExtConfigure() {
        HSConfiguration config = new HSConfiguration();
        config.configure();
        HBSpatialExtension.setConfiguration(config);
        PrecisionModel pm = HBSpatialExtension.getDefaultGeomFactory()
                .getPrecisionModel();
        double scale = pm.getScale();
        assertEquals(5.0, scale, 0.00001);
        assertFalse(pm.isFloating());
    }

    //TODO -repair test

//    @Test
//	public void testConfigure() {
//		HSConfiguration config = new HSConfiguration();
//		Configuration hibConfig = new Configuration();
//		hibConfig.configure();
//		config.configure(hibConfig);
//		assertEquals("org.hibernatespatial.postgis.PostgisSpatialDialect", config
//				.getDefaultDialect());
//
//		config.configure();
//		testResults(config);
//
//	}

    @Test
    public void testConfigureFile() {
        HSConfiguration config = new HSConfiguration();
        config.configure("hibernate-spatial.cfg.xml");
        testResults(config);
    }


    private void testResults(HSConfiguration config) {
        assertEquals("org.hibernatespatial.postgis.PostgisDialect", config
                .getDefaultDialect());
        assertEquals("FIXED", config.getPrecisionModel());
        assertEquals("5", config.getPrecisionModelScale());
    }

}
