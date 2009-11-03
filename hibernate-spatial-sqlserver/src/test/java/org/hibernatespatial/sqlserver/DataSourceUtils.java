/*
 * $Id$
 *
 * This file is part of Hibernate Spatial, an extension to the
 * hibernate ORM solution for geographic data.
 *
 * Copyright © 2009 Geovise BVBA
 *
 * This work was partially supported by the European Commission,
 * under the 6th Framework Programme, contract IST-2-004688-STP.
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

package org.hibernatespatial.sqlserver;

import org.apache.commons.dbcp.BasicDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.*;
import java.io.InputStream;
import java.io.IOException;
import java.sql.*;

/**
 * <p>Unit test support class.</p>
 *
 * @author Karel Maesen, Geovise BVBA.
 *         Date: Nov 2, 2009
 */
public class DataSourceUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(DataSourceUtils.class);
    static Properties properties;

    static {
        InputStream is = null;
        try {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream("hibernate-spatial-sqlsever-test.properties");
            properties = new Properties();
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) try {
                is.close();
            } catch (IOException e) {
                //nothing to do
            }
        }
    }

    private static final DataSource dataSource = createBasicDataSource();

    private static class GeomTest {
        //TODO -- how to define EMPTY Geomtries in SQL?
        final static String[] DATA = new String[]{
                "insert into geomtest values (1, 'POINT', Geometry::STGeomFromText('POINT(10 5)', 0))",
                "insert into geomtest values (2, 'POINT', Geometry::STGeomFromText('POINT(52.25  2.53)', 4326))",
                "insert into geomtest values (3, 'POINT', Geometry::STGeomFromText('POINT(150000 200000)', 31370))",
                "insert into geomtest values (4, 'POINT', Geometry::STGeomFromText('POINT(10.0 2.0 1.0 3.0)', 4326))",
        };
    }

    private static DataSource createBasicDataSource() {
        String url = properties.getProperty("jdbcUrl");
        String user = properties.getProperty("dbUsername");
        String pwd = properties.getProperty("dbPassword");
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        result.setUrl(url);
        result.setUsername(user);
        result.setPassword(pwd);
        return result;
    }

    public static DataSource getDataSource() {
        return dataSource;
    }


    public static void removeReadTestData() {
        Connection cn = null;
        try {
            cn = getDataSource().getConnection();
            PreparedStatement pmt = cn.prepareStatement("delete from geomtest");
            if (!pmt.execute()) {
                int updateCount = pmt.getUpdateCount();
                LOGGER.info("Removing " + updateCount + " rows.");
            }
            pmt.close();
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            try {
                cn.close();
            } catch (SQLException e) {
                // nothing to do
            }
        }
    }


    public static void loadReadTestData() {
        Connection cn = null;
        try {
            cn = getDataSource().getConnection();
            Statement stmt = cn.createStatement();
            for (int i = 0; i < GeomTest.DATA.length; i++) {
                LOGGER.debug("adding stmt: " + GeomTest.DATA[i]);
                stmt.addBatch(GeomTest.DATA[i]);
            }
            int[] insCounts = stmt.executeBatch();
            stmt.close();
            LOGGER.info("Loaded " + sum(insCounts) + " rows.");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cn.close();
            } catch (SQLException e) {
                // nothing to do
            }
        }
    }

    public static Map<Integer, byte[]> rawByteArrays(String type) {
        Map<Integer, byte[]> map = new HashMap<Integer, byte[]>();
        Connection cn = null;
        try {
            cn = getDataSource().getConnection();
            PreparedStatement pstmt = cn.prepareStatement("select id, geom from geomtest where type = ? order by id");
            pstmt.setString(1, type);
            ResultSet results = pstmt.executeQuery();
            while (results.next()) {
                Integer id = results.getInt(1);
                byte[] bytes = results.getBytes(2);
                map.put(id, bytes);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                cn.close();
            } catch (SQLException e) {
                // nothing we can do.
            }
        }
        return map;

    }

    private static int sum(int[] insCounts) {
        int result = 0;
        for (int idx = 0; idx < insCounts.length; idx++) {
            result += insCounts[idx];
        }
        return result;
    }

}
