package org.hibernatespatial.test.cfg;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.hibernate.cfg.Configuration;
import org.hibernatespatial.HBSpatialExtension;
import org.hibernatespatial.cfg.HSConfiguration;
import org.hibernatespatial.cfg.HSProperty;
import org.junit.Test;

public class HSConfigurationTest {

	private static final String hibernate_config_location = "/Users/maesenka/workspaces/hibernate-spatial/hibernate-spatial-mysql/src/test/java/hibernate.cfg.xml";

	private static final String hs_config_location = "/Users/maesenka/workspaces/hibernate-spatial/hibernate-spatial/src/test/java/hibernate-spatial.cfg.xml";

	@Test
	public void testConfigure() {
		HSConfiguration config = new HSConfiguration();
		Configuration hibConfig = new Configuration();
		hibConfig.configure(new File(hibernate_config_location));
		config.configure(hibConfig);
		assertEquals("org.hibernatespatial.mysql.MySQLSpatialDialect", config
				.getDefaultDialect());

		config.configure();
		testResults(config);

	}

	@Test
	public void testConfigureFile() {
		HSConfiguration config = new HSConfiguration();
		config.configure(new File(hs_config_location));
		testResults(config);
	}

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
	}

	private void testResults(HSConfiguration config) {
		assertEquals("org.hibernatespatial.postgis.PostgisDialect", config
				.getDefaultDialect());
		assertEquals("FIXED", config.getPrecisionModel());
		assertEquals("5", config.getPrecisionModelScale());
	}
}
