/*
 * $Id$
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

import junit.framework.JUnit4TestAdapter;

import org.hibernatespatial.test.TestCRUD;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author martin
 */
public class TestSql2008CRUD {

    private final static TestCRUD delegate;

    static {
        delegate = new TestCRUD();
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        delegate.setUpBeforeClass();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        delegate.tearDownAfterClass();
    }

    @Test
    public void testSave2DPointEntity() throws Exception {
        delegate.testSavePoint(2);
    }

    @Test
    public void testSave3DPointEntity() throws Exception {
        delegate.testSavePoint(3);
    }

    @Test
    public void testSaveLineStringEntity() throws Exception {
        delegate.testSaveLineStringEntity();
    }

    @Test
    public void testSaveMLineStringEntity() throws Exception {
        delegate.testSaveMLineStringEntity2D();
    }

    @Test
    public void testSaveMultiMLineStringEntity() throws Exception {
        delegate.testSaveMultiMLineStringEntity2D();
    }

    @Test
    public void testSaveNullLineStringEntity() throws Exception {
        delegate.testSaveNullLineStringEntity();
    }

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(TestSql2008CRUD.class);
    }
}
