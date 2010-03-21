package org.hibernatespatial.test.pojo.reader;

import org.dom4j.Document;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernatespatial.HBSpatialExtension;
import org.hibernatespatial.cfg.HSConfiguration;
import org.hibernatespatial.helper.FinderException;
import org.hibernatespatial.pojo.AutoMapper;
import org.hibernatespatial.readers.BasicFeatureReader;
import org.hibernatespatial.readers.Feature;
import org.hibernatespatial.readers.FeatureReader;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestBasicFeatureReader {

    private SessionFactory sessionFactory;
    private AutoMapper pojoUtil;

    public void setUpBeforeClass(HSConfiguration hsconfig, Connection conn) throws SQLException {

        HBSpatialExtension.setConfiguration(hsconfig);
        List<String> tables = new ArrayList<String>();
        tables.add("geomtest");
        Document mappingdocument;
        try {
            mappingdocument = AutoMapper.map(conn, null, null, tables);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Configuration config = new Configuration();
        config.addXML(mappingdocument.asXML());
        this.sessionFactory = config.configure().buildSessionFactory();

    }

    public void testReaderNoFilters(int expected) throws FinderException {
        Class<?> clazz = AutoMapper.getClass(null, null, "geomtest");
        FeatureReader reader = new BasicFeatureReader(clazz, this.sessionFactory, null, null);
        int count = 0;
        while (reader.hasNext()) {
            Feature f = reader.next();
            assertNotNull(f.getId());
            count++;
        }
        assertEquals(expected, count);
        reader.close();
    }

}
