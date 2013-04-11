/*
 * $Id$
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

import org.hibernatespatial.SpatialRelation;
import org.junit.Test;

import static org.hibernate.criterion.Restrictions.isEmpty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Tests basic settings of the {@link GeoDBDialect}.
 *
 * @Author Jan Boonen, Geodan IT b.v.
 */
public class TestGeoDBDialect {

    private GeoDBDialect geoDBDialect = new GeoDBDialect();

    @Test
    public void testDbGeometryTypeName() {
        assertEquals("GEOM", geoDBDialect.getDbGeometryTypeName());
    }

    @Test
    public void testGetSpatialAggregateSQL() {
        String sql = geoDBDialect.getSpatialAggregateSQL("geom", 1);
        assertEquals(sql, "extent(geom)");
    }

    @Test
    public void testGetSpatialFilterExpression() {
        assertEquals("(geom && ? ) ", geoDBDialect.getSpatialFilterExpression("geom"));
    }

    @Test
    public void testGetSpatialRelateSQL() {
        assertEquals(" ST_Contains(geom, ?)", geoDBDialect.getSpatialRelateSQL("geom", SpatialRelation.CONTAINS));
        assertEquals(" ST_Crosses(geom, ?)", geoDBDialect.getSpatialRelateSQL("geom", SpatialRelation.CROSSES));
        assertEquals(" ST_Disjoint(geom, ?)", geoDBDialect.getSpatialRelateSQL("geom", SpatialRelation.DISJOINT));
        assertEquals(" ST_Equals(geom, ?)", geoDBDialect.getSpatialRelateSQL("geom", SpatialRelation.EQUALS));
        assertEquals(" ST_Intersects(geom, ?)", geoDBDialect.getSpatialRelateSQL("geom", SpatialRelation.INTERSECTS));
        assertEquals(" ST_Overlaps(geom, ?)", geoDBDialect.getSpatialRelateSQL("geom", SpatialRelation.OVERLAPS));
        assertEquals(" ST_Touches(geom, ?)", geoDBDialect.getSpatialRelateSQL("geom", SpatialRelation.TOUCHES));
        assertEquals(" ST_Within(geom, ?)", geoDBDialect.getSpatialRelateSQL("geom", SpatialRelation.WITHIN));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetSpatialRelateSQLUnsupported() {
        geoDBDialect.getSpatialRelateSQL("geom", SpatialRelation.FILTER);
    }

}
