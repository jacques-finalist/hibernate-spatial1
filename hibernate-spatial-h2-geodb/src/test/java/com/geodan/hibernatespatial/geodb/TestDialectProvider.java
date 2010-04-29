package com.geodan.hibernatespatial.geodb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.hibernatespatial.HBSpatialExtension;
import org.hibernatespatial.SpatialDialect;
import org.hibernatespatial.cfg.HSConfiguration;
import org.junit.Test;

import com.vividsolutions.jts.geom.PrecisionModel;

public class TestDialectProvider {
	
	private final static String DIALECT_NAME = "com.geodan.hibernatespatial.geodb.GeoDBDialect";

	@Test
	public void testDefaultDialect() {
		DialectProvider dialect = new DialectProvider();
		assertEquals(DIALECT_NAME, dialect.getDefaultDialect().getClass().getCanonicalName());
	}
	
	@Test
	public void testSupportedDialects() {
		DialectProvider dialect = new DialectProvider();
		assertTrue(Arrays.asList(dialect.getSupportedDialects()).contains(DIALECT_NAME));
	}
	
	@Test
	public void testCreateSpatialDialect() {
		DialectProvider dialect = new DialectProvider();
		SpatialDialect spatialDialect = dialect.createSpatialDialect(DIALECT_NAME);
		assertNotNull(spatialDialect);
		assertTrue(spatialDialect.getGeometryUserType() instanceof GeoDBGeometryUserType);
	}
	
    @Test
    public void testHBSpatialExtensionConfiguration() {
        HSConfiguration config = new HSConfiguration();
        config.configure();
        HBSpatialExtension.setConfiguration(config);

        PrecisionModel pm = HBSpatialExtension.getDefaultGeomFactory()
                .getPrecisionModel();
        double scale = pm.getScale();
        assertEquals(5.0, scale, 0.00001);
        assertFalse(pm.isFloating());
    }	
}
