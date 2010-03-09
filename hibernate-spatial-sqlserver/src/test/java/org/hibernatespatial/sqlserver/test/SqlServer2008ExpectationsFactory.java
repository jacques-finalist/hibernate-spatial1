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

package org.hibernatespatial.sqlserver.test;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.hibernatespatial.sqlserver.convertors.Decoders;
import org.hibernatespatial.test.AbstractExpectationsFactory;
import org.hibernatespatial.test.NativeSQLStatement;


/**
 * Created by IntelliJ IDEA.
 * User: maesenka
 * Date: Feb 21, 2010
 * Time: 2:06:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class SqlServer2008ExpectationsFactory extends AbstractExpectationsFactory {

    private final static String TEST_POLYGON_WKT = "POLYGON((0 0, 50 0, 100 100, 0 100, 0 0))";


    @Override
    protected NativeSQLStatement getNativeDimensionSQL() {
        return createNativeSQLStatement("select t.id, t.geom.STDimension() from GeomTest t");
    }

    @Override
    protected NativeSQLStatement createNativeBufferStatement(Double distance) {
        return createNativeSQLStatement("select t.id, t.geom.STBuffer(?) from GeomTest t where t.geom.STSrid = 4326", new Object[]{distance});
    }

    @Override
    protected NativeSQLStatement createConvexHullStatement(Geometry geom) {
        return createNativeSQLStatementAllWKTParams("select t.id, t.geom.STUnion(geometry::STGeomFromText(?, 4326)).STConvexHull() from GeomTest t where t.geom.STSrid = 4326", geom.toText());
    }

    @Override
    protected NativeSQLStatement createIntersectionStatement(Geometry geom) {
        return createNativeSQLStatementAllWKTParams("select t.id, t.geom.STIntersection(geometry::STGeomFromText(?, 4326)) from GeomTest t where t.geom.STSrid = 4326", geom.toText());
    }

    @Override
    protected NativeSQLStatement createDifferenceStatement(Geometry geom) {
        return createNativeSQLStatementAllWKTParams("select t.id, t.geom.STDifference(geometry::STGeomFromText(?, 4326)) from GeomTest t where t.geom.STSrid = 4326", geom.toText());
    }

    @Override
    protected NativeSQLStatement createSymDifferenceStatement(Geometry geom) {
        return createNativeSQLStatementAllWKTParams("select t.id, t.geom.STSymDifference(geometry::STGeomFromText(?, 4326)) from GeomTest t where t.geom.STSrid = 4326", geom.toText());
    }

    @Override
    protected NativeSQLStatement createGeomUnionStatement(Geometry geom) {
        return createNativeSQLStatementAllWKTParams("select t.id, t.geom.STUnion(geometry::STGeomFromText(?, 4326)) from GeomTest t where t.geom.STSrid = 4326", geom.toText());
    }

    @Override
    protected NativeSQLStatement getNativeAsTextSQL() {
        return createNativeSQLStatement("select t.id, t.geom.STAsText() from GeomTest t");
    }

    @Override
    protected NativeSQLStatement getNativeSridSQL() {
        return createNativeSQLStatement("select t.id, t.geom.STSrid from GeomTest t");
    }

    @Override
    protected NativeSQLStatement getNativeIsSimpleSQL() {
        return createNativeSQLStatement("select t.id, t.geom.STIsSimple() from GeomTest t");
    }

    @Override
    protected NativeSQLStatement getNativeIsemptyQL() {
        return createNativeSQLStatement("select t.id, t.geom.STIsEmpty() from GeomTest t");
    }

    @Override
    protected NativeSQLStatement getNativeBoundarySQL() {
        return createNativeSQLStatement("select t.id, t.geom.STBoundary() from GeomTest t");
    }

    @Override
    protected NativeSQLStatement getNativeEnvelopeSQL() {
        return createNativeSQLStatement("select t.id, t.geom.STEnvelope() from GeomTest t");
    }

    @Override
    protected NativeSQLStatement getNativeAsBinarySQL() {
        return createNativeSQLStatement("select t.id, t.geom.STAsBinary() from GeomTest t");
    }

    @Override
    protected NativeSQLStatement getGeometryTypeSQL() {
        return createNativeSQLStatement("select t.id, t.geom.STGeometryType() from GeomTest t");
    }

    @Override
    protected Geometry decode(Object o) {
        return Decoders.decode((byte[]) o);
    }

    @Override
    protected NativeSQLStatement createNativeWithinStatement(Geometry geom) {
        return createNativeSQLStatementAllWKTParams(
                "select t.id, t.geom.STWithin(geometry::STGeomFromText(?, 4326)) from GeomTest t where t.geom.STWithin(geometry::STGeomFromText(?, 4326)) = 'true' and t.geom.STSrid = 4326",
                geom.toText());
    }

    @Override
    protected NativeSQLStatement createNativeEqualsStatement(Geometry geom) {
        return createNativeSQLStatementAllWKTParams("select t.id, t.geom.STEquals(geometry::STGeomFromText(?, 4326)) from GeomTest t where t.geom.STEquals(geometry::STGeomFromText(?, 4326)) = 'true' and t.geom.STSrid = 4326",
                geom.toText());
    }

    @Override
    protected NativeSQLStatement createNativeCrossesStatement(Geometry geom) {
        return createNativeSQLStatementAllWKTParams("select t.id, t.geom.STCrosses(geometry::STGeomFromText(?, 4326)) from GeomTest t where t.geom.STCrosses(geometry::STGeomFromText(?, 4326)) = 'true' and t.geom.STSrid = 4326",
                geom.toText());
    }

    @Override
    protected NativeSQLStatement createNativeDisjointStatement(Geometry geom) {
        return createNativeSQLStatementAllWKTParams("select t.id, t.geom.STDisjoint(geometry::STGeomFromText(?, 4326)) from GeomTest t where t.geom.STDisjoint(geometry::STGeomFromText(?, 4326)) = 'true' and t.geom.STSrid = 4326",
                geom.toText());
    }


    @Override
    protected NativeSQLStatement createNativeIntersectsStatement(Geometry geom) {
        return createNativeSQLStatementAllWKTParams("select t.id, t.geom.STIntersects(geometry::STGeomFromText(?, 4326)) from GeomTest t where t.geom.STIntersects(geometry::STGeomFromText(?, 4326)) = 'true' and t.geom.STSrid = 4326",
                geom.toText());
    }

    @Override
    protected NativeSQLStatement createNativeTouchesStatement(Geometry geom) {
        return createNativeSQLStatementAllWKTParams("select t.id, t.geom.STTouches(geometry::STGeomFromText(?, 4326)) from GeomTest t where t.geom.STTouches(geometry::STGeomFromText(?, 4326)) = 'true' and t.geom.STSrid = 4326",
                geom.toText());
    }

    @Override
    protected NativeSQLStatement createNativeOverlapsStatement(Geometry geom) {
        return createNativeSQLStatementAllWKTParams("select t.id, t.geom.STOverlaps(geometry::STGeomFromText(?, 4326)) from GeomTest t where t.geom.STOverlaps(geometry::STGeomFromText(?, 4326)) = 'true' and t.geom.STSrid = 4326",
                geom.toText());
    }

    @Override
    protected NativeSQLStatement createNativeRelatesStatement(Geometry geom, String matrix) {
        String sql = "select t.id, t.geom.STRelate(geometry::STGeomFromText(?, 4326), '" + matrix + "' ) from GeomTest t where t.geom.STRelate(geometry::STGeomFromText(?, 4326), '" + matrix + "') = 'true' and t.geom.STSrid = 4326";
        return createNativeSQLStatementAllWKTParams(sql, geom.toText());
    }

    @Override
    protected NativeSQLStatement createNativeDistanceStatement(Geometry geom) {
        return createNativeSQLStatementAllWKTParams("select t.id, t.geom.STDistance(geometry::STGeomFromText(?, 4326)) from GeomTest t where t.geom.STSrid = 4326", geom.toText());
    }


    @Override
    public Polygon getTestPolygon() {
        WKTReader reader = new WKTReader();
        try {
            Polygon polygon = (Polygon) reader.read(TEST_POLYGON_WKT);
            polygon.setSRID(getTestSrid());
            return polygon;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getTestPolygonWKT() {
        return TEST_POLYGON_WKT;

    }

}
