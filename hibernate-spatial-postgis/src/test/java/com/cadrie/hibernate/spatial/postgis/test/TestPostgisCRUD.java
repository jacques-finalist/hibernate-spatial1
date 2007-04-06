package com.cadrie.hibernate.spatial.postgis.test;

import junit.framework.JUnit4TestAdapter;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cadrie.hibernate.spatial.test.TestCRUD;

public class TestPostgisCRUD {

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
    public void testSaveLineStringEntity() throws Exception {
	delegate.testSaveLineStringEntity();
    }
    
    @Test
    public void testSaveNullLineStringEntity() throws Exception {
	delegate.testSaveNullLineStringEntity();
    }

    public static junit.framework.Test suite() {
	return new JUnit4TestAdapter(TestPostgisCRUD.class);
    }
    
    
}
