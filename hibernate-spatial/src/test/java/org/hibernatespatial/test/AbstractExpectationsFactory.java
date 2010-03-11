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
import com.vividsolutions.jts.geom.Polygon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: maesenka
 * Date: Feb 21, 2010
 * Time: 2:06:55 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractExpectationsFactory {


    private final static int TEST_SRID = 4326;


    /**
     * Returns the SRID in which all tests are conducted. This is for now 4326;
     *
     * @return
     */
    public int getTestSrid() {
        return TEST_SRID;
    }


    /**
     * Returns the dimensions of all test geometries.
     *
     * @return map of identifier, dimension
     * @throws SQLException
     */
    public Map<Integer, Integer> getExpectedDimension() throws SQLException {
        return retrieveExpected(getNativeDimensionSQL(), false);
    }


    public Map<Integer, String> getAsText() throws SQLException {
        return retrieveExpected(getNativeAsTextSQL(), false);

    }


    public Map<Integer, byte[]> getAsBinary() throws SQLException {
        return retrieveExpected(getNativeAsBinarySQL(), false);
    }


    public Map<Integer, String> getGeometryType() throws SQLException {
        return retrieveExpected(getGeometryTypeSQL(), false);
    }

    public Map<Integer, Integer> getSrid() throws SQLException {
        return retrieveExpected(getNativeSridSQL(), false);
    }

    public Map<Integer, Boolean> getIsSimple() throws SQLException {
        return retrieveExpected(getNativeIsSimpleSQL(), false);
    }

    public Map<Integer, Boolean> getIsEmpty() throws SQLException {
        return retrieveExpected(getNativeIsemptyQL(), false);
    }

    public Map<Integer, Geometry> getBoundary() throws SQLException {
        return retrieveExpected(getNativeBoundarySQL(), true);
    }

    public Map<Integer, Geometry> getEnvelope() throws SQLException {
        return retrieveExpected(getNativeEnvelopeSQL(), true);
    }

    public Map<Integer, Boolean> getWithin(Geometry geom) throws SQLException {
        return retrieveExpected(createNativeWithinStatement(geom), false);
    }


    public Map<Integer, Boolean> getEquals(Geometry geom) throws SQLException {
        return retrieveExpected(createNativeEqualsStatement(geom), false);
    }

    public Map<Integer, Boolean> getCrosses(Geometry geom) throws SQLException {
        return retrieveExpected(createNativeCrossesStatement(geom), false);
    }

    public Map<Integer, Boolean> getDisjoint(Geometry geom) throws SQLException {
        return retrieveExpected(createNativeDisjointStatement(geom), false);
    }

    public Map<Integer, Boolean> getIntersects(Geometry geom) throws SQLException {
        return retrieveExpected(createNativeIntersectsStatement(geom), false);
    }

    protected abstract NativeSQLStatement createNativeIntersectsStatement(Geometry geom);

    public Map<Integer, Boolean> getTouches(Geometry geom) throws SQLException {
        return retrieveExpected(createNativeTouchesStatement(geom), false);
    }

    protected abstract NativeSQLStatement createNativeTouchesStatement(Geometry geom);

    public Map<Integer, Boolean> getOverlaps(Geometry geom) throws SQLException {
        return retrieveExpected(createNativeOverlapsStatement(geom), false);
    }

    protected abstract NativeSQLStatement createNativeOverlapsStatement(Geometry geom);


    public Map<Integer, Boolean> getRelate(Geometry geom, String matrix) throws SQLException {
        return retrieveExpected(createNativeRelatesStatement(geom, matrix), false);
    }

    protected abstract NativeSQLStatement createNativeRelatesStatement(Geometry geom, String matrix);

    public Map<Integer, Double> getDistance(Geometry geom) throws SQLException {
        return retrieveExpected(createNativeDistanceStatement(geom), false);
    }

    protected abstract NativeSQLStatement createNativeDistanceStatement(Geometry geom);

    /**
     * Returns the SQL statement corresponding to the HQL statement:
     * "select id, dimension(geom) from GeomEntity".
     *
     * @return the SQL String
     */
    protected abstract NativeSQLStatement getNativeDimensionSQL();

    public Map<Integer, Geometry> getBuffer(Double distance) throws SQLException {
        return retrieveExpected(createNativeBufferStatement(distance), true);
    }

    /**
     * Returns the native SQL statement corresponding to the HQL statement:
     * "SELECT id, distance(geom, :filter) from GeomEntity where srid(geom) = 4326"
     *
     * @param distance
     * @return
     */
    protected abstract NativeSQLStatement createNativeBufferStatement(Double distance);


    public Map<Integer, Geometry> getConvexHull(Geometry geom) throws SQLException {
        return retrieveExpected(createConvexHullStatement(geom), true);
    }

    protected abstract NativeSQLStatement createConvexHullStatement(Geometry geom);


    public Map<Integer, Geometry> getIntersection(Geometry geom) throws SQLException {
        return retrieveExpected(createIntersectionStatement(geom), true);
    }

    protected abstract NativeSQLStatement createIntersectionStatement(Geometry geom);

    public Map<Integer, Geometry> getDifference(Geometry geom) throws SQLException {
        return retrieveExpected(createDifferenceStatement(geom), true);
    }

    protected abstract NativeSQLStatement createDifferenceStatement(Geometry geom);

    public Map<Integer, Geometry> getSymDifference(Geometry geom) throws SQLException {
        return retrieveExpected(createSymDifferenceStatement(geom), true);
    }

    protected abstract NativeSQLStatement createSymDifferenceStatement(Geometry geom);

    public Map<Integer, Geometry> getGeomUnion(Geometry geom) throws SQLException {
        return retrieveExpected(createGeomUnionStatement(geom), true);
    }

    protected abstract NativeSQLStatement createGeomUnionStatement(Geometry geom);


    /**
     * Returns the SQL statement corresponding to the HQL statement:
     * "select id, astext(geom) from GeomEntity".
     *
     * @return the SQL String
     */
    protected abstract NativeSQLStatement getNativeAsTextSQL();

    /**
     * Returns the SQL statement corresponding to the HQL statement:
     * "select id, srid(geom) from GeomEntity".
     *
     * @return the SQL String
     */
    protected abstract NativeSQLStatement getNativeSridSQL();

    /**
     * Returns the SQL statement corresponding to the HQL statement:
     * "select id, issimple(geom) from GeomEntity".
     *
     * @return the SQL String
     */
    protected abstract NativeSQLStatement getNativeIsSimpleSQL();

    /**
     * Returns the SQL statement corresponding to the HQL statement:
     * "select id, isempty(geom) from GeomEntity".
     *
     * @return the SQL String
     */
    protected abstract NativeSQLStatement getNativeIsemptyQL();

    /**
     * Returns the SQL statement corresponding to the HQL statement:
     * "select id, boundary(geom) from GeomEntity".
     *
     * @return the SQL String
     */
    protected abstract NativeSQLStatement getNativeBoundarySQL();

    /**
     * Returns the SQL statement corresponding to the HQL statement:
     * "select id, envelope(geom) from GeomEntity".
     *
     * @return the SQL String
     */
    protected abstract NativeSQLStatement getNativeEnvelopeSQL();

    /**
     * Returns the SQL statement corresponding to the HQL statement:
     * "select id, asbinary(geom) from GeomEntity".
     *
     * @return the SQL String
     */
    protected abstract NativeSQLStatement getNativeAsBinarySQL();

    /**
     * Returns the SQL statement corresponding to the HQL statement:
     * "select id, geometrytype(geom) from GeomEntity".
     *
     * @return the SQL String
     */
    protected abstract NativeSQLStatement getGeometryTypeSQL();


    protected abstract Geometry decode(Object o);

    public abstract Polygon getTestPolygon();

    public abstract String getTestPolygonWKT();

    /**
     * Returns a Wrapper for the HQL statement
     * "SELECT id, within(geom, :filter) from GeomEntity where within(geom, :filter) = true and srid(geom) = 4326"
     *
     * @return @param testPolygon
     */
    protected abstract NativeSQLStatement createNativeWithinStatement(Geometry testPolygon);

    protected abstract NativeSQLStatement createNativeEqualsStatement(Geometry geom);

    protected abstract NativeSQLStatement createNativeCrossesStatement(Geometry geom);

    protected abstract NativeSQLStatement createNativeDisjointStatement(Geometry geom);


    private <T> Map<Integer, T> retrieveExpected(NativeSQLStatement nativeSQLStatement, boolean expectGeometry) throws SQLException {
        PreparedStatement preparedStatement = null;
        Connection cn = null;
        Map<Integer, T> expected = new HashMap<Integer, T>();
        try {
            cn = createConnection();
            preparedStatement = nativeSQLStatement.prepare(cn);
            ResultSet results = preparedStatement.executeQuery();
            while (results.next()) {
                int id = results.getInt(1);
                if (expectGeometry) {
                    expected.put(id, (T) decode(results.getObject(2)));
                } else {
                    T val = (T) results.getObject(2);
                    expected.put(id, val);
                }
            }
            return expected;
        } finally {
            if (preparedStatement != null) preparedStatement.close();
            if (cn != null) cn.close();
        }
    }

    protected abstract Connection createConnection() throws SQLException;

    protected NativeSQLStatement createNativeSQLStatement(final String sql) {
        return new NativeSQLStatement() {
            public PreparedStatement prepare(Connection connection) throws SQLException {
                return connection.prepareStatement(sql);
            }
        };
    }

    protected NativeSQLStatement createNativeSQLStatementAllWKTParams(final String sql, final String wkt) {
        return new NativeSQLStatement() {
            public PreparedStatement prepare(Connection connection) throws SQLException {
                PreparedStatement pstmt = connection.prepareStatement(sql);
                for (int i = 1; i <= numPlaceHoldersInSQL(sql); i++) {
                    pstmt.setString(i, getTestPolygonWKT());
                }
                return pstmt;
            }
        };
    }

    protected NativeSQLStatement createNativeSQLStatement(final String sql, final Object[] params) {
        return new NativeSQLStatement() {
            public PreparedStatement prepare(Connection connection) throws SQLException {
                PreparedStatement pstmt = connection.prepareStatement(sql);
                int i = 1;
                for (Object param : params) {
                    pstmt.setObject(i++, param);
                }
                return pstmt;
            }
        };
    }


    protected int numPlaceHoldersInSQL(String sql) {
        return sql.replaceAll("[^?]", "").length();
    }


}
