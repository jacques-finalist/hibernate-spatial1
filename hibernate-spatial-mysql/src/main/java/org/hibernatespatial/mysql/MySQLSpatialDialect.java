/**
 * $Id$
 *
 * This file is part of Hibernate Spatial, an extension to the 
 * hibernate ORM solution for geographic data. 
 *
 * Copyright © 2007 Geovise BVBA
 * Copyright © 2007 K.U. Leuven LRD, Spatial Applications Division, Belgium
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
package org.hibernatespatial.mysql;

import org.hibernate.Hibernate;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.CustomType;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.hibernate.usertype.UserType;
import org.hibernatespatial.SpatialDialect;
import org.hibernatespatial.SpatialFunction;
import org.hibernatespatial.SpatialRelation;

/**
 * Extends the MySQLDialect by also including information on spatial operators,
 * constructors and processing functions.
 *
 * @author Karel Maesen
 */
public class MySQLSpatialDialect extends MySQLDialect implements SpatialDialect {


    private static final Type geometryCustomType = new CustomType(new MySQLGeometryUserType(), new String[]{"mysql_geometry"});

    public MySQLSpatialDialect() {
        super();
        registerColumnType(java.sql.Types.ARRAY, "GEOMETRY");

        // registering OGC functions
        // (spec_simplefeatures_sql_99-04.pdf)

        // section 2.1.1.1
        // Registerfunction calls for registering geometry functions:
        // first argument is the OGC standard functionname, second the name as
        // it occurs in the spatial dialect
        registerFunction("dimension", new StandardSQLFunction("dimension",
                Hibernate.INTEGER));
        registerFunction("geometrytype", new StandardSQLFunction(
                "geometrytype", Hibernate.STRING));
        registerFunction("srid", new StandardSQLFunction("srid",
                StandardBasicTypes.INTEGER));
        registerFunction("envelope", new StandardSQLFunction("envelope",
                new CustomType(new MySQLGeometryUserType(), null)));
        registerFunction("astext", new StandardSQLFunction("astext",
                StandardBasicTypes.STRING));
        registerFunction("asbinary", new StandardSQLFunction("asbinary",
                StandardBasicTypes.BINARY));
        registerFunction("isempty", new StandardSQLFunction("isempty",
                StandardBasicTypes.BOOLEAN));
        registerFunction("issimple", new StandardSQLFunction("issimple",
                StandardBasicTypes.BOOLEAN));
        registerFunction("boundary", new StandardSQLFunction("boundary",
                geometryCustomType));

        // Register functions for spatial relation constructs
        registerFunction("overlaps", new StandardSQLFunction("overlaps",
                StandardBasicTypes.BOOLEAN));
        registerFunction("intersects", new StandardSQLFunction("intersects",
                StandardBasicTypes.BOOLEAN));
        registerFunction("equals", new StandardSQLFunction("equals",
                StandardBasicTypes.BOOLEAN));
        registerFunction("contains", new StandardSQLFunction("contains",
                StandardBasicTypes.BOOLEAN));
        registerFunction("crosses", new StandardSQLFunction("crosses",
                StandardBasicTypes.BOOLEAN));
        registerFunction("disjoint", new StandardSQLFunction("disjoint",
                StandardBasicTypes.BOOLEAN));
        registerFunction("touches", new StandardSQLFunction("touches",
                StandardBasicTypes.BOOLEAN));
        registerFunction("within", new StandardSQLFunction("within",
                StandardBasicTypes.BOOLEAN));
        registerFunction("relate", new StandardSQLFunction("relate",
                StandardBasicTypes.BOOLEAN));

        // register the spatial analysis functions
        registerFunction("distance", new StandardSQLFunction("distance",
                StandardBasicTypes.DOUBLE));
        registerFunction("buffer", new StandardSQLFunction("buffer",
                geometryCustomType));
        registerFunction("convexhull", new StandardSQLFunction("convexhull",
                geometryCustomType));
        registerFunction("difference", new StandardSQLFunction("difference",
                geometryCustomType));
        registerFunction("intersection", new StandardSQLFunction(
                "intersection", geometryCustomType));
        registerFunction("symdifference", new StandardSQLFunction(
                "symdifference", geometryCustomType));
        registerFunction("geomunion", new StandardSQLFunction("union",
                geometryCustomType));

    }

    /**
     * @param columnName      The name of the geometry-typed column to which the relation is
     *                        applied
     * @param spatialRelation The type of spatial relation (as defined in
     *                        <code>SpatialRelation</code>).
     * @return
     */
    public String getSpatialRelateSQL(String columnName, int spatialRelation) {
        switch (spatialRelation) {
            case SpatialRelation.WITHIN:
                return " within(" + columnName + ",?)";
            case SpatialRelation.CONTAINS:
                return " contains(" + columnName + ", ?)";
            case SpatialRelation.CROSSES:
                return " crosses(" + columnName + ", ?)";
            case SpatialRelation.OVERLAPS:
                return " overlaps(" + columnName + ", ?)";
            case SpatialRelation.DISJOINT:
                return " disjoint(" + columnName + ", ?)";
            case SpatialRelation.INTERSECTS:
                return " intersects(" + columnName + ", ?)";
            case SpatialRelation.TOUCHES:
                return " touches(" + columnName + ", ?)";
            case SpatialRelation.EQUALS:
                return " equals(" + columnName + ", ?)";
            default:
                throw new IllegalArgumentException(
                        "Spatial relation is not known by this dialect");
        }

    }

    public String getSpatialFilterExpression(String columnName) {
        return "MBRIntersects(" + columnName + ", ? ) ";
    }

    /*
      * (non-Javadoc)
      *
      * @see org.hibernatespatial.SpatialDialect#getGeometryUserType()
      */

    public UserType getGeometryUserType() {
        return new MySQLGeometryUserType();
    }

    public String getSpatialAggregateSQL(String columnName, int aggregation) {
        throw new UnsupportedOperationException("Mysql has no spatial aggregate SQL functions.");
    }

    public String getDWithinSQL(String columnName) {
        throw new UnsupportedOperationException(String.format("Mysql doesn't support the Dwithin function"));
    }

    public String getHavingSridSQL(String columnName) {
        return " (srid(" + columnName + ") = ?) ";
    }

    public String getIsEmptySQL(String columnName, boolean isEmpty) {
        String emptyExpr = " IsEmpty(" + columnName + ") ";
        return isEmpty ? emptyExpr : "( NOT " + emptyExpr + ")";
    }

    public String getDbGeometryTypeName() {
        return "GEOMETRY";
    }


    public boolean isTwoPhaseFiltering() {
        return false;
    }

    public boolean supportsFiltering() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean supports(SpatialFunction function) {
        switch (function) {
            case boundary:
            case relate:
            case distance:
            case buffer:
            case convexhull:
            case difference:
            case symdifference:
            case intersection:
            case geomunion:
            case dwithin:
            case transform:
                return false;
        }
        return true;
    }

}
