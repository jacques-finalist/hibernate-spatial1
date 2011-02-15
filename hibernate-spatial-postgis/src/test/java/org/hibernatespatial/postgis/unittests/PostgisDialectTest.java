package org.hibernatespatial.postgis.unittests;

import junit.framework.TestCase;
import org.hibernatespatial.SpatialDialect;
import org.hibernatespatial.SpatialFunction;
import org.hibernatespatial.postgis.PostgisDialect;
import org.junit.Test;

/**
 * Tests support for
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 1/19/11
 */
public class PostgisDialectTest extends TestCase {

    SpatialDialect dialect = new PostgisDialect();

    @Test
    public void testSupports() throws Exception {
        for (SpatialFunction sf : SpatialFunction.values()) {
            assertTrue("Dialect doesn't support " + sf, dialect.supports(sf));
        }
    }
}
