/*
 * $Id:$
 *
 * This file is part of Hibernate Spatial, an extension to the
 * hibernate ORM solution for geographic data.
 *
 * Copyright 2010 Geodan IT b.v.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, visit: http://www.hibernatespatial.org/
 */

package org.hibernatespatial.geodb;

import com.vividsolutions.jts.geom.PrecisionModel;
import org.hibernatespatial.HBSpatialExtension;
import org.hibernatespatial.SpatialDialect;
import org.hibernatespatial.cfg.HSConfiguration;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class TestDialectProvider {

    private final static String DIALECT_NAME = "org.hibernatespatial.geodb.GeoDBDialect";

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
