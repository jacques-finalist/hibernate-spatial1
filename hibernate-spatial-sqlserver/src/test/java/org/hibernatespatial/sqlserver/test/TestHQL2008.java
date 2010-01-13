/*
 * $Id:$
 *
 * This file is part of Hibernate Spatial, an extension to the
 * hibernate ORM solution for geographic data.
 *
 * Copyright © 2009 Geovise BVBA
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

package org.hibernatespatial.sqlserver.test;

import org.hibernatespatial.test.TestHQL;
import org.hibernatespatial.test.model.LineStringEntity;
import org.hibernatespatial.test.model.PointEntity;
import org.hibernatespatial.test.model.PolygonEntity;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestHQL2008 {

    private static TestHQL delegate = new TestHQL();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        delegate.setUpBeforeClass();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        delegate.tearDownAfterClass();
    }

    @Test
    public void testEquity() throws Exception {
        delegate.testEquity(PointEntity.class);
        delegate.testEquity(LineStringEntity.class);
        delegate.testEquity(PolygonEntity.class);
    }

    @Test
    public void testHqlFunctions() throws Exception {
        delegate.testHqlFunctions(PointEntity.class);
        delegate.testHqlFunctions(LineStringEntity.class);
        delegate.testHqlFunctions(PolygonEntity.class);
    }

    @Test
    public void testHqlRelations() throws Exception {
        delegate.testHqlRelations(PointEntity.class);
        delegate.testHqlRelations(LineStringEntity.class);
        delegate.testHqlRelations(PolygonEntity.class);
    }

    @Test
    public void testHqlAnalysis() throws Exception {
        delegate.testHqlAnalysis();
    }

    @Test
    public void testRestriction() throws Exception {
        delegate.testRestriction(PointEntity.class);
        delegate.testRestriction(LineStringEntity.class);
        delegate.testRestriction(PolygonEntity.class);
    }
}
