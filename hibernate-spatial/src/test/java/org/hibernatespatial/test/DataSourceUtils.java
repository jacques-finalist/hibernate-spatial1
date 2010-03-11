/*
 * $Id:$
 *
 * This file is part of Hibernate Spatial, an extension to the
 * hibernate ORM solution for geographic data.
 *
 * Copyright © 2007-2010 Geovise BVBA
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

package org.hibernatespatial.test;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * <p>Unit test support class.</p>
 *
 * @author Karel Maesen, Geovise BVBA.
 *         Date: Nov 2, 2009
 */
public class DataSourceUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(DataSourceUtils.class);

    private final String propertyFile;
    private final SQLExpressionTemplate sqlExpressionTemplate;

    private TestObjects testObjects;
    private Properties properties;
    private DataSource dataSource;

    public DataSourceUtils(String propertyFile, SQLExpressionTemplate sqlExpressionTemplate) {
        this.propertyFile = propertyFile;
        this.sqlExpressionTemplate = sqlExpressionTemplate;
        readProperties();
        createBasicDataSource();
        loadTestGeometries();
    }

    private void readProperties() {
        InputStream is = null;
        try {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(propertyFile);
            properties = new Properties();
            properties.load(is);
        } catch (IOException e) {
            throw (new RuntimeException(e));
        } finally {
            if (is != null) try {
                is.close();
            } catch (IOException e) {
                //nothing to do
            }
        }
    }

    private void createBasicDataSource() {
        String url = properties.getProperty("jdbcUrl");
        String user = properties.getProperty("dbUsername");
        String pwd = properties.getProperty("dbPassword");
        String driverName = properties.getProperty("driver");
        BasicDataSource bds = new BasicDataSource();
        bds.setDriverClassName(driverName);
        bds.setUrl(url);
        bds.setUsername(user);
        bds.setPassword(pwd);
        dataSource = bds;
    }


    private void loadTestGeometries() {
        testObjects = new TestObjects();
        testObjects.prepare();
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public Connection createConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    public void deleteTestData() throws SQLException {
        Connection cn = null;
        try {
            cn = getDataSource().getConnection();
            PreparedStatement pmt = cn.prepareStatement("delete from geomtest");
            if (!pmt.execute()) {
                int updateCount = pmt.getUpdateCount();
                LOGGER.info("Removing " + updateCount + " rows.");
            }
            pmt.close();
        } finally {
            try {
                if (cn != null) cn.close();
            } catch (SQLException e) {
                // nothing to do
            }
        }
    }

    // we need to be able to test what happens with invalid data for marshalling/unmarshalling
    // but when testing relations/functions we must have only valid geoms.

    public void insertTestData() throws SQLException {
        Connection cn = null;
        try {
            cn = getDataSource().getConnection();
            Statement stmt = cn.createStatement();
            for (TestObject testObject : testObjects) {
                String sql = sqlExpressionTemplate.toInsertSql(testObject);
                LOGGER.debug("adding stmt: " + sql);
                stmt.addBatch(sql);
            }
            int[] insCounts = stmt.executeBatch();
            stmt.close();
            LOGGER.info("Loaded " + sum(insCounts) + " rows.");
        } finally {
            try {
                if (cn != null) cn.close();
            } catch (SQLException e) {
                // nothing to do
            }
        }
    }

    public Map<Integer, Object> rawDbObjects(String type) {
        Map<Integer, Object> map = new HashMap<Integer, Object>();
        Connection cn = null;
        try {
            cn = getDataSource().getConnection();
            PreparedStatement pstmt = cn.prepareStatement("select id, geom from geomtest where type = ? order by id");
            pstmt.setString(1, type);
            ResultSet results = pstmt.executeQuery();
            while (results.next()) {
                Integer id = results.getInt(1);
                Object obj = results.getObject(2);
                map.put(id, obj);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (cn != null) cn.close();
            } catch (SQLException e) {
                // nothing we can do.
            }
        }
        return map;

    }

    public Map<Integer, Geometry> expectedGeoms(String type) {
        Map<Integer, Geometry> result = new HashMap<Integer, Geometry>();
        EWKTReader parser = new EWKTReader();
        for (TestObject testObject : testObjects) {
            if (testObject.type.equalsIgnoreCase(type)) {
                try {
                    result.put(testObject.id, parser.read(testObject.wkt));
                } catch (ParseException e) {
                    System.out.println(String.format("Parsing WKT fails for case %d : %s", testObject.id, testObject.wkt));
                    throw new RuntimeException(e);
                }
            }
        }
        return result;
    }

    private static int sum(int[] insCounts) {
        int result = 0;
        for (int idx = 0; idx < insCounts.length; idx++) {
            result += insCounts[idx];
        }
        return result;
    }

}
