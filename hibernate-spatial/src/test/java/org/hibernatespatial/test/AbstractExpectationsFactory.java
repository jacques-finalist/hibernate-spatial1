/*
 * $Id$
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
 * An <code>AbstractExpectationsFactory</code> provides the expected
 * values to be used in the unit tests of the spatial functions
 * provided by specific providers.
 * <p/>
 * The expected values are returned as a map of (identifier, expected value) pairs.
 *
 * @author Karel Maesen, Geovise BVBA
 * @see TestSpatialFunctions
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
     * Returns the expected dimensions of all test geometries.
     *
     * @return map of identifier, dimension
     * @throws SQLException
     */
    public Map<Integer, Integer> getDimension() throws SQLException {
        return retrieveExpected(getNativeDimensionSQL(), false);
    }

    /**
     * Returns the expected WKT of all test geometries.
     *
     * @return map of identifier, WKT-string
     * @throws SQLException
     */
    public Map<Integer, String> getAsText() throws SQLException {
        return retrieveExpected(createNativeAsTextStatement(), false);

    }


    /**
     * Returns the expected WKB representations of all test geometries
     *
     * @return map of identifier, WKB representation
     * @throws SQLException
     */
    public Map<Integer, byte[]> getAsBinary() throws SQLException {
        return retrieveExpected(createNativeAsBinaryStatement(), false);
    }

    /**
     * Returns the expected type names of all test geometries
     *
     * @return map of identifier, type name
     * @throws SQLException
     */
    public Map<Integer, String> getGeometryType() throws SQLException {
        return retrieveExpected(createGeometryTypeStatement(), false);
    }

    /**
     * Returns the expected SRID codes of all test geometries
     *
     * @return map of identifier, SRID
     * @throws SQLException
     */
    public Map<Integer, Integer> getSrid() throws SQLException {
        return retrieveExpected(createNativeSridStatement(), false);
    }

    /**
     * Returns whether the test geometries are simple
     *
     * @return map of identifier and whether test geometry is simple
     * @throws SQLException
     */
    public Map<Integer, Boolean> getIsSimple() throws SQLException {
        return retrieveExpected(createNativeIsSimpleStatement(), false);
    }

    /**
     * Returns whether the test geometries are empty
     *
     * @return map of identifier and whether test geometry is empty
     * @throws SQLException
     */
    public Map<Integer, Boolean> getIsEmpty() throws SQLException {
        return retrieveExpected(createNativeIsEmptyStatement(), false);
    }

    /**
     * Returns the expected boundaries of all test geometries
     *
     * @return map of identifier and boundary geometry
     * @throws SQLException
     */
    public Map<Integer, Geometry> getBoundary() throws SQLException {
        return retrieveExpected(createNativeBoundaryStatement(), true);
    }

    /**
     * Returns the expected envelopes of all test geometries
     *
     * @return map of identifier and envelope
     * @throws SQLException
     */
    public Map<Integer, Geometry> getEnvelope() throws SQLException {
        return retrieveExpected(createNativeEnvelopeStatement(), true);
    }

    /**
     * Returns the expected results of the within operator
     *
     * @param geom test geometry
     * @return
     * @throws SQLException
     */
    public Map<Integer, Boolean> getWithin(Geometry geom) throws SQLException {
        return retrieveExpected(createNativeWithinStatement(geom), false);
    }

    /**
     * Returns the expected results of the equals operator
     *
     * @param geom
     * @return
     * @throws SQLException
     */
    public Map<Integer, Boolean> getEquals(Geometry geom) throws SQLException {
        return retrieveExpected(createNativeEqualsStatement(geom), false);
    }

    /**
     * Returns the expected results of the crosses operator
     *
     * @param geom
     * @return
     * @throws SQLException
     */
    public Map<Integer, Boolean> getCrosses(Geometry geom) throws SQLException {
        return retrieveExpected(createNativeCrossesStatement(geom), false);
    }

    /**
     * Returns the expected results of the disjoint operator
     *
     * @param geom
     * @return
     * @throws SQLException
     */
    public Map<Integer, Boolean> getDisjoint(Geometry geom) throws SQLException {
        return retrieveExpected(createNativeDisjointStatement(geom), false);
    }

    /**
     * Returns the expected results of the intersects operator
     *
     * @param geom
     * @return
     * @throws SQLException
     */
    public Map<Integer, Boolean> getIntersects(Geometry geom) throws SQLException {
        return retrieveExpected(createNativeIntersectsStatement(geom), false);
    }

    /**
     * Returns the expected results of the touches operator
     *
     * @param geom
     * @return
     * @throws SQLException
     */
    public Map<Integer, Boolean> getTouches(Geometry geom) throws SQLException {
        return retrieveExpected(createNativeTouchesStatement(geom), false);
    }

    /**
     * Returns the expected results of the overlaps operator
     *
     * @param geom
     * @return
     * @throws SQLException
     */
    public Map<Integer, Boolean> getOverlaps(Geometry geom) throws SQLException {
        return retrieveExpected(createNativeOverlapsStatement(geom), false);
    }

    /**
     * Returns the expected results of the relate operator
     *
     * @param geom
     * @param matrix
     * @return
     * @throws SQLException
     */
    public Map<Integer, Boolean> getRelate(Geometry geom, String matrix) throws SQLException {
        return retrieveExpected(createNativeRelateStatement(geom, matrix), false);
    }

    /**
     * Returns the expected results of the distance function
     *
     * @param geom geometry parameter to distance function
     * @return
     * @throws SQLException
     */
    public Map<Integer, Double> getDistance(Geometry geom) throws SQLException {
        return retrieveExpected(createNativeDistanceStatement(geom), false);
    }

    /**
     * Returns the expected results of the buffering function
     *
     * @param distance distance parameter to the buffer function
     * @return
     * @throws SQLException
     */
    public Map<Integer, Geometry> getBuffer(Double distance) throws SQLException {
        return retrieveExpected(createNativeBufferStatement(distance), true);
    }

    /**
     * Returns the expected results of the convexhull function
     *
     * @param geom geometry with which each test geometry is unioned before convexhull calculation
     * @return
     * @throws SQLException
     */
    public Map<Integer, Geometry> getConvexHull(Geometry geom) throws SQLException {
        return retrieveExpected(createConvexHullStatement(geom), true);
    }

    /**
     * Returns the expected results of the intersection function
     *
     * @param geom parameter to the intersection function
     * @return
     * @throws SQLException
     */
    public Map<Integer, Geometry> getIntersection(Geometry geom) throws SQLException {
        return retrieveExpected(createIntersectionStatement(geom), true);
    }

    /**
     * Returns the expected results of the difference function
     *
     * @param geom parameter to the difference function
     * @return
     * @throws SQLException
     */
    public Map<Integer, Geometry> getDifference(Geometry geom) throws SQLException {
        return retrieveExpected(createDifferenceStatement(geom), true);
    }

    /**
     * Returns the expected results of the symdifference function
     *
     * @param geom parameter to the symdifference function
     * @return
     * @throws SQLException
     */

    public Map<Integer, Geometry> getSymDifference(Geometry geom) throws SQLException {
        return retrieveExpected(createSymDifferenceStatement(geom), true);
    }

    /**
     * Returns the expected results of the geomunion function
     *
     * @param geom parameter to the geomunion function
     * @return
     * @throws SQLException
     */
    public Map<Integer, Geometry> getGeomUnion(Geometry geom) throws SQLException {
        return retrieveExpected(createGeomUnionStatement(geom), true);
    }

    /**
     * Returns a statement corresponding to the HQL statement:
     * "SELECT id, touches(geom, :filter) from GeomEntity where touches(geom, :filter) = true and srid(geom) = 4326"
     *
     * @param geom the geometry corresponding to the ':filter' query parameter
     * @return
     */
    protected abstract NativeSQLStatement createNativeTouchesStatement(Geometry geom);

    /**
     * Returns a statement corresponding to the HQL statement:
     * "SELECT id, overlaps(geom, :filter) from GeomEntity where overlaps(geom, :filter) = true and srid(geom) = 4326"
     *
     * @param geom the geometry corresponding to the ':filter' query parameter
     * @return
     */
    protected abstract NativeSQLStatement createNativeOverlapsStatement(Geometry geom);

    /**
     * Returns a statement corresponding to the HQL statement:
     * "SELECT id, relate(geom, :filter, :matrix) from GeomEntity where relate(geom, :filter, :matrix) = true and srid(geom) = 4326"
     *
     * @param geom   the geometry corresponding to the ':filter' query parameter
     * @param matrix the string corresponding to the ':matrix' query parameter
     * @return
     */
    protected abstract NativeSQLStatement createNativeRelateStatement(Geometry geom, String matrix);

    /**
     * Returns a statement corresponding to the HQL statement:
     * "SELECT id, intersects(geom, :filter) from GeomEntity where intersects(geom, :filter) = true and srid(geom) = 4326"
     *
     * @param geom the geometry corresponding to the ':filter' query parameter
     * @return
     */
    protected abstract NativeSQLStatement createNativeIntersectsStatement(Geometry geom);

    /**
     * Returns a statement corresponding to the HQL statement:
     * "SELECT id, distance(geom, :filter) from GeomEntity where srid(geom) = 4326"
     *
     * @param geom
     * @return
     */
    protected abstract NativeSQLStatement createNativeDistanceStatement(Geometry geom);

    /**
     * Returns a statement corresponding to the HQL statement:
     * "select id, dimension(geom) from GeomEntity".
     *
     * @return the SQL String
     */
    protected abstract NativeSQLStatement getNativeDimensionSQL();

    /**
     * Returns a statement corresponding to the HQL statement:
     * "SELECT id, distance(geom, :distance) from GeomEntity where srid(geom) = 4326"
     *
     * @param distance parameter corresponding to the ':distance' query parameter
     * @return the native SQL Statement
     */
    protected abstract NativeSQLStatement createNativeBufferStatement(Double distance);

    /**
     * Returns a statement corresponding to the HQL statement:
     * "SELECT id, convexhull(geomunion(geom, :polygon)) from GeomEntity where srid(geom) = 4326"
     *
     * @param geom parameter corresponding to the ':polygon' query parameter
     * @return
     */
    protected abstract NativeSQLStatement createConvexHullStatement(Geometry geom);

    /**
     * Returns a statement corresponding to the HQL statement:
     * "SELECT id, intersection(geom, :polygon) from GeomEntity where srid(geom) = 4326"
     *
     * @param geom parameter corresponding to the ':polygon' query parameter
     * @return
     */
    protected abstract NativeSQLStatement createIntersectionStatement(Geometry geom);

    /**
     * Returns a statement corresponding to the HQL statement:
     * "SELECT id, difference(geom, :polygon) from GeomEntity where srid(geom) = 4326"26"
     *
     * @param geom parameter corresponding to the ':polygon' query parameter
     * @return
     */
    protected abstract NativeSQLStatement createDifferenceStatement(Geometry geom);

    /**
     * Returns a statement corresponding to the HQL statement:
     * "SELECT id, symdifference(geom, :polygon) from GeomEntity where srid(geom) = 4326"26"
     *
     * @param geom parameter corresponding to the ':polygon' query parameter
     * @return
     */
    protected abstract NativeSQLStatement createSymDifferenceStatement(Geometry geom);

    /**
     * Returns a statement corresponding to the HQL statement:
     * "SELECT id, geomunion(geom, :polygon) from GeomEntity where srid(geom) = 4326"26"
     *
     * @param geom parameter corresponding to the ':polygon' query parameter
     * @return
     */
    protected abstract NativeSQLStatement createGeomUnionStatement(Geometry geom);

    /**
     * Returns a statement corresponding to the HQL statement:
     * "select id, astext(geom) from GeomEntity".
     *
     * @return the native SQL Statement
     */
    protected abstract NativeSQLStatement createNativeAsTextStatement();

    /**
     * Returns a statement corresponding to the HQL statement:
     * "select id, srid(geom) from GeomEntity".
     *
     * @return the native SQL Statement
     */
    protected abstract NativeSQLStatement createNativeSridStatement();

    /**
     * Returns a statement corresponding to the HQL statement:
     * "select id, issimple(geom) from GeomEntity".
     *
     * @return the native SQL Statement
     */
    protected abstract NativeSQLStatement createNativeIsSimpleStatement();

    /**
     * Returns a statement corresponding to the HQL statement:
     * "select id, isempty(geom) from GeomEntity".
     *
     * @return the native SQL Statement
     */
    protected abstract NativeSQLStatement createNativeIsEmptyStatement();

    /**
     * Returns a statement corresponding to the HQL statement:
     * "select id, boundary(geom) from GeomEntity".
     *
     * @return the native SQL Statement
     */
    protected abstract NativeSQLStatement createNativeBoundaryStatement();

    /**
     * Returns a statement corresponding to the HQL statement:
     * "select id, envelope(geom) from GeomEntity".
     *
     * @return the native SQL Statement
     */
    protected abstract NativeSQLStatement createNativeEnvelopeStatement();

    /**
     * Returns a statement corresponding to the HQL statement:
     * "select id, asbinary(geom) from GeomEntity".
     *
     * @return the native SQL Statement
     */
    protected abstract NativeSQLStatement createNativeAsBinaryStatement();

    /**
     * Returns a statement corresponding to the HQL statement:
     * "select id, geometrytype(geom) from GeomEntity".
     *
     * @return the SQL String
     */
    protected abstract NativeSQLStatement createGeometryTypeStatement();

    /**
     * Returns a statement corresponding to the HQL statement
     * "SELECT id, within(geom, :filter) from GeomEntity where within(geom, :filter) = true and srid(geom) = 4326"
     *
     * @param testPolygon the geometry corresponding to the ':filter' query parameter
     * @return
     */
    protected abstract NativeSQLStatement createNativeWithinStatement(Geometry testPolygon);

    /**
     * Returns a statement corresponding to the HQL statement
     * "SELECT id, equals(geom, :filter) from GeomEntity where equals(geom, :filter) = true and srid(geom) = 4326"
     *
     * @param geom the geometry corresponding to the ':filter' query parameter
     * @return
     */
    protected abstract NativeSQLStatement createNativeEqualsStatement(Geometry geom);

    /**
     * Returns a statement corresponding to the HQL statement
     * "SELECT id, crosses(geom, :filter) from GeomEntity where crosses(geom, :filter) = true and srid(geom) = 4326"
     *
     * @param geom the geometry corresponding to the ':filter' query parameter
     * @return
     */
    protected abstract NativeSQLStatement createNativeCrossesStatement(Geometry geom);

    /**
     * Returns a statement corresponding to the HQL statement
     * "SELECT id, disjoint(geom, :filter) from GeomEntity where disjoint(geom, :filter) = true and srid(geom) = 4326"
     *
     * @param geom the geometry corresponding to the ':filter' query parameter
     * @return
     */
    protected abstract NativeSQLStatement createNativeDisjointStatement(Geometry geom);

    /**
     * Creates a connection to the database
     *
     * @return a Connection
     * @throws SQLException
     */
    protected abstract Connection createConnection() throws SQLException;

    /**
     * Decodes a native database object to a JTS <code>Geometry</code> instance
     *
     * @param o native database object
     * @return decoded geometry
     */
    protected abstract Geometry decode(Object o);

    /**
     * Return a test polygon (filter, ...)
     *
     * @return a test polygon
     */
    public abstract Polygon getTestPolygon();

    protected <T> Map<Integer, T> retrieveExpected(NativeSQLStatement nativeSQLStatement, boolean expectGeometry) throws SQLException {
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
                    pstmt.setString(i, wkt);
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
