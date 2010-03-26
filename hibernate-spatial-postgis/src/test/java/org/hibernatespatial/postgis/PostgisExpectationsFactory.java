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

package org.hibernatespatial.postgis;

import com.vividsolutions.jts.geom.Geometry;
import org.hibernatespatial.test.AbstractExpectationsFactory;
import org.hibernatespatial.test.NativeSQLStatement;

/**
 * This class provides the expected return values to the test classes in this package.
 *
 * @author Karel Maesen, Geovise BVBA
 */
public class PostgisExpectationsFactory extends AbstractExpectationsFactory {

    private final PGGeometryUserType decoder = new PGGeometryUserType();

    public PostgisExpectationsFactory() {
        super("hibernate-spatial-postgis-test.properties", new PostgisExpressionTemplate());
    }

    @Override
    protected NativeSQLStatement createNativeTouchesStatement(Geometry geom) {
        return createNativeSQLStatementAllWKTParams(
                "select t.id, touches(t.geom, GeomFromText(?, 4326)) from GeomTest t where touches(t.geom, geomFromText(?, 4326)) = 'true' and srid(t.geom) = 4326",
                geom.toText());
    }

    @Override
    protected NativeSQLStatement createNativeOverlapsStatement(Geometry geom) {
        return createNativeSQLStatementAllWKTParams(
                "select t.id, overlaps(t.geom, GeomFromText(?, 4326)) from GeomTest t where overlaps(t.geom, geomFromText(?, 4326)) = 'true' and srid(t.geom) = 4326",
                geom.toText());
    }

    @Override
    protected NativeSQLStatement createNativeRelateStatement(Geometry geom, String matrix) {
        String sql = "select t.id, relate(t.geom, GeomFromText(?, 4326), '" + matrix + "' ) from GeomTest t where relate(t.geom, GeomFromText(?, 4326), '" + matrix + "') = 'true' and srid(t.geom) = 4326";
        return createNativeSQLStatementAllWKTParams(sql, geom.toText());
    }

    @Override
    protected NativeSQLStatement createNativeIntersectsStatement(Geometry geom) {
        return createNativeSQLStatementAllWKTParams(
                "select t.id, intersects(t.geom, GeomFromText(?, 4326)) from GeomTest t where intersects(t.geom, geomFromText(?, 4326)) = 'true' and srid(t.geom) = 4326",
                geom.toText());
    }

    @Override
    protected NativeSQLStatement createNativeFilterStatement(Geometry geom) {
        return createNativeSQLStatementAllWKTParams(
                "select t.id, t.geom && GeomFromText(?, 4326) from GeomTest t where intersects(t.geom, GeomFromText(?, 4326)) = 'true' and srid(t.geom) = 4326",
                geom.toText());
    }

    @Override
    protected NativeSQLStatement createNativeDistanceStatement(Geometry geom) {
        return createNativeSQLStatementAllWKTParams(
                "select t.id, distance(t.geom, GeomFromText(?, 4326)) from GeomTest t where srid(t.geom) = 4326",
                geom.toText());
    }

    @Override
    protected NativeSQLStatement createNativeDimensionSQL() {
        return createNativeSQLStatement("select id, dimension(geom) from geomtest");
    }

    @Override
    protected NativeSQLStatement createNativeBufferStatement(Double distance) {
        return createNativeSQLStatement("select t.id, buffer(t.geom,?) from GeomTest t where srid(t.geom) = 4326", new Object[]{distance});
    }

    @Override
    protected NativeSQLStatement createNativeConvexHullStatement(Geometry geom) {
        return createNativeSQLStatementAllWKTParams(
                "select t.id, convexhull(geomunion(t.geom, GeomFromText(?, 4326))) from GeomTest t where srid(t.geom) = 4326",
                geom.toText());
    }

    @Override
    protected NativeSQLStatement createNativeIntersectionStatement(Geometry geom) {
        return createNativeSQLStatementAllWKTParams(
                "select t.id, intersection(t.geom, GeomFromText(?, 4326)) from GeomTest t where srid(t.geom) = 4326",
                geom.toText());
    }

    @Override
    protected NativeSQLStatement createNativeDifferenceStatement(Geometry geom) {
        return createNativeSQLStatementAllWKTParams(
                "select t.id, difference(t.geom, GeomFromText(?, 4326)) from GeomTest t where srid(t.geom) = 4326",
                geom.toText());
    }

    @Override
    protected NativeSQLStatement createNativeSymDifferenceStatement(Geometry geom) {
        return createNativeSQLStatementAllWKTParams(
                "select t.id, symdifference(t.geom, GeomFromText(?, 4326)) from GeomTest t where srid(t.geom) = 4326",
                geom.toText());
    }

    @Override
    protected NativeSQLStatement createNativeGeomUnionStatement(Geometry geom) {
        return createNativeSQLStatementAllWKTParams(
                "select t.id, geomunion(t.geom, GeomFromText(?, 4326)) from GeomTest t where srid(t.geom) = 4326",
                geom.toText());
    }

    @Override
    protected NativeSQLStatement createNativeAsTextStatement() {
        return createNativeSQLStatement("select id, astext(geom) from geomtest");
    }

    @Override
    protected NativeSQLStatement createNativeSridStatement() {
        return createNativeSQLStatement("select id, srid(geom) from geomtest");
    }

    @Override
    protected NativeSQLStatement createNativeIsSimpleStatement() {
        return createNativeSQLStatement("select id, issimple(geom) from geomtest");
    }

    @Override
    protected NativeSQLStatement createNativeIsEmptyStatement() {
        return createNativeSQLStatement("select id, isempty(geom) from geomtest");
    }

    @Override
    protected NativeSQLStatement createNativeBoundaryStatement() {
        return createNativeSQLStatement("select id, boundary(geom) from geomtest");
    }

    @Override
    protected NativeSQLStatement createNativeEnvelopeStatement() {
        return createNativeSQLStatement("select id, envelope(geom) from geomtest");
    }

    @Override
    protected NativeSQLStatement createNativeAsBinaryStatement() {
        return createNativeSQLStatement("select id, asbinary(geom) from geomtest");
    }

    @Override
    protected NativeSQLStatement createNativeGeometryTypeStatement() {
        return createNativeSQLStatement("select id, GeometryType(geom) from geomtest");
    }

    @Override
    protected NativeSQLStatement createNativeWithinStatement(Geometry geom) {
        return createNativeSQLStatementAllWKTParams(
                "select t.id, within(t.geom, GeomFromText(?, 4326)) from GeomTest t where within(t.geom, geomFromText(?, 4326)) = 'true' and srid(t.geom) = 4326",
                geom.toText());
    }

    @Override
    protected NativeSQLStatement createNativeEqualsStatement(Geometry geom) {
        return createNativeSQLStatementAllWKTParams(
                "select t.id, equals(t.geom, GeomFromText(?, 4326)) from GeomTest t where equals(t.geom, geomFromText(?, 4326)) = 'true' and srid(t.geom) = 4326",
                geom.toText());
    }

    @Override
    protected NativeSQLStatement createNativeCrossesStatement(Geometry geom) {
        return createNativeSQLStatementAllWKTParams(
                "select t.id, crosses(t.geom, GeomFromText(?, 4326)) from GeomTest t where crosses(t.geom, geomFromText(?, 4326)) = 'true' and srid(t.geom) = 4326",
                geom.toText());
    }

    @Override
    protected NativeSQLStatement createNativeContainsStatement(Geometry geom) {
        return createNativeSQLStatementAllWKTParams(
                "select t.id, contains(t.geom, GeomFromText(?, 4326)) from GeomTest t where contains(t.geom, geomFromText(?, 4326)) = 'true' and srid(t.geom) = 4326",
                geom.toText());
    }

    @Override
    protected NativeSQLStatement createNativeDisjointStatement(Geometry geom) {
        return createNativeSQLStatementAllWKTParams(
                "select t.id, disjoint(t.geom, GeomFromText(?, 4326)) from GeomTest t where disjoint(t.geom, geomFromText(?, 4326)) = 'true' and srid(t.geom) = 4326",
                geom.toText());
    }

    @Override
    protected Geometry decode(Object o) {
        return decoder.convert2JTS(o);
    }

}
