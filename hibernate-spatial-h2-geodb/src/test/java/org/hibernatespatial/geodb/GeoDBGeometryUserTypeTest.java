/**
 * 
 */
package org.hibernatespatial.geodb;

import geodb.GeoDB;
import junit.framework.Assert;

import org.hibernatespatial.test.EWKTReader;
import org.junit.Ignore;
import org.junit.Test;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;

/**
 * @author reinoldp
 * 
 */
public class GeoDBGeometryUserTypeTest {

	/**
	 * Test method for
	 * {@link org.hibernatespatial.geodb.GeoDBGeometryUserType#convert2JTS(java.lang.Object)}
	 * .
	 * With EWKB
	 * @throws ParseException
	 */
	@Test
	@Ignore("Does not work yet with GeoDB 0.6")
	public void testConvert2JTS_EWKB() throws ParseException {
		// create test EWKB
		Geometry g = new EWKTReader().read("SRID=4326;POINT (2 20)");
		// to ewkb
		byte[] ewkb = GeoDB.gToEWKB(g);
		Geometry converted = new GeoDBGeometryUserType().convert2JTS(ewkb);
		Assert.assertEquals(g, converted);
	}
	
	/**
	 * Test method for
	 * {@link org.hibernatespatial.geodb.GeoDBGeometryUserType#convert2JTS(java.lang.Object)}
	 * .
	 * With WKB
	 * @throws ParseException
	 */
	@Test
	public void testConvert2JTS_WKB() throws ParseException {
		// create test EWKB
		Geometry g = new EWKTReader().read("SRID=4326;POINT (2 20)");
		// to ewkb
		byte[] wkb = GeoDB.gToWKB(g);
		Geometry converted = new GeoDBGeometryUserType().convert2JTS(wkb);
		Assert.assertEquals(g, converted);
	}
	
	/**
	 * Test method for
	 * {@link org.hibernatespatial.geodb.GeoDBGeometryUserType#convert2JTS(java.lang.Object)}
	 * .
	 * With EWKB and zero bounding box
	 * @throws ParseException
	 */
	@Test
	@Ignore("Does not work yet with GeoDB 0.6")
	public void testConvert2JTS_EWKB_Special() throws ParseException {
		// create test EWKB
		Geometry g = new EWKTReader().read("SRID=4326;POINT (0 0)");
		// to ewkb
		byte[] ewkb = GeoDB.gToEWKB(g);
		// assert special test condition
		Assert.assertEquals((byte)0,ewkb[0]);
		Geometry converted = new GeoDBGeometryUserType().convert2JTS(ewkb);
		Assert.assertEquals(g, converted);
	}


}
