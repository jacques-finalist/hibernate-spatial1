package com.geodan.hibernatespatial.geodb;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestGeoDBDialect {

	private GeoDBDialect geoDBDialect = new GeoDBDialect();
	
	@Test
	public void testDbGeometryTypeName() {
		assertEquals("GEOM", geoDBDialect.getDbGeometryTypeName());
	}
	
	public void testGetSpatialAggregateSQL() {
		
	}
	
	public void testGetSpatialFilterExpression() {
		
	}
	
	public void testGetSpatialRelateSQL() {
		
	}
}
