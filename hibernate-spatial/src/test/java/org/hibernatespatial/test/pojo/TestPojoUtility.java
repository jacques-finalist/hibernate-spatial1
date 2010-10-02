package org.hibernatespatial.test.pojo;

import org.dom4j.Document;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernatespatial.HBSpatialExtension;
import org.hibernatespatial.cfg.HSConfiguration;
import org.hibernatespatial.pojo.AutoMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TestPojoUtility {

    private SessionFactory sessionFactory;
    private AutoMapper pojoUtil;

    public void setUpBeforeClass(HSConfiguration hsconfig, Connection conn) throws SQLException {

        HBSpatialExtension.setConfiguration(hsconfig);
        List<String> tables = new ArrayList<String>();
        tables.add("geomtest");
//        tables.add("test2") ;
        Document mappingdocument;
        try {
            mappingdocument = AutoMapper.map(conn, null, null, tables);
            writeToFile(mappingdocument);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Configuration config = new Configuration();
        config.addXML(mappingdocument.asXML());
        this.sessionFactory = config.configure().buildSessionFactory();

    }

    private void writeToFile(Document mappingdocument) {
        try {
            File f = File.createTempFile("test-hs-automapper", ".xml");
            FileWriter writer = new FileWriter(f);
            mappingdocument.write(writer);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public SessionFactory getSessionFactory() {
        return this.sessionFactory;
    }

    public AutoMapper getPOJOUtility() {
        return this.pojoUtil;
    }
}
