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
import org.hibernate.usertype.UserType;
import org.hibernatespatial.SpatialDialect;
import org.hibernatespatial.SpatialRelation;

/**
 * Extends the MySQLDialect by also including information on spatial operators,
 * constructors and processing functions.
 *
 * @author Karel Maesen
 */
public class MySQLSpatialDialect extends MySQLDialect implements SpatialDialect {

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
                Hibernate.INTEGER));
        registerFunction("envelope", new StandardSQLFunction("envelope",
                new CustomType(MySQLGeometryUserType.class, null)));
        registerFunction("astext", new StandardSQLFunction("astext",
                Hibernate.STRING));
        registerFunction("asbinary", new StandardSQLFunction("asbinary",
                Hibernate.BINARY));
        registerFunction("isempty", new StandardSQLFunction("isempty",
                Hibernate.BOOLEAN));
        registerFunction("issimple", new StandardSQLFunction("issimple",
                Hibernate.BOOLEAN));
        registerFunction("boundary", new StandardSQLFunction("boundary",
                new CustomType(MySQLGeometryUserType.class, null)));

        // Register functions for spatial relation constructs
        registerFunction("overlaps", new StandardSQLFunction("overlaps",
                Hibernate.BOOLEAN));
        registerFunction("intersects", new StandardSQLFunction("intersects",
                Hibernate.BOOLEAN));
        registerFunction("equals", new StandardSQLFunction("equals",
                Hibernate.BOOLEAN));
        registerFunction("contains", new StandardSQLFunction("contains",
                Hibernate.BOOLEAN));
        registerFunction("crosses", new StandardSQLFunction("crosses",
                Hibernate.BOOLEAN));
        registerFunction("disjoint", new StandardSQLFunction("disjoint",
                Hibernate.BOOLEAN));
        registerFunction("touches", new StandardSQLFunction("touches",
                Hibernate.BOOLEAN));
        registerFunction("within", new StandardSQLFunction("within",
                Hibernate.BOOLEAN));
        registerFunction("relate", new StandardSQLFunction("relate",
                Hibernate.BOOLEAN));

        // register the spatial analysis functions
        registerFunction("distance", new StandardSQLFunction("distance",
                Hibernate.DOUBLE));
        registerFunction("buffer", new StandardSQLFunction("buffer",
                new CustomType(MySQLGeometryUserType.class, null)));
        registerFunction("convexhull", new StandardSQLFunction("convexhull",
                new CustomType(MySQLGeometryUserType.class, null)));
        registerFunction("difference", new StandardSQLFunction("difference",
                new CustomType(MySQLGeometryUserType.class, null)));
        registerFunction("intersection", new StandardSQLFunction(
                "intersection", new CustomType(MySQLGeometryUserType.class,
                        null)));
        registerFunction("symdifference", new StandardSQLFunction(
                "symdifference", new CustomType(MySQLGeometryUserType.class,
                        null)));
        registerFunction("geomunion", new StandardSQLFunction("union",
                new CustomType(MySQLGeometryUserType.class, null)));

    }

    /*
      * (non-Javadoc)
      *
      * @see org.walkonweb.spatial.dialect.SpatialEnabledDialect#getSpatialRelateExpression(java.lang.String,
      *      int, boolean)
      */

    public String getSpatialRelateSQL(String columnName, int spatialRelation,
                                      boolean hasFilter) {
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

    /*
      * (non-Javadoc)
      *
      * @see org.walkonweb.spatial.dialect.SpatialEnabledDialect#getSpatialFilterExpression(java.lang.String)
      */

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

    public String getDbGeometryTypeName() {
        return "GEOMETRY";
    }


    public boolean isTwoPhaseFiltering() {
        return false;
    }

}
