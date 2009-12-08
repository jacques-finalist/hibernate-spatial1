/*
 * $Id:$
 *
 * This file is part of Hibernate Spatial, an extension to the
 * hibernate ORM solution for geographic data.
 *
 * Copyright © 2009 Geovise BVBA
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

import org.hibernate.Hibernate;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.CustomType;
import org.hibernate.usertype.UserType;
import org.hibernatespatial.SpatialDialect;

/**
 * @author Karel Maesen, Martin Steinwender.
 *         Date: Nov 2, 2009
 */
public class SQLServer2008SpatialDialect extends SQLServerDialect implements SpatialDialect {

    public final static String SHORT_NAME = "sql2008";

    public final static String COLUMN_TYPE = "GEOMETRY";

    public SQLServer2008SpatialDialect() {
        super();
        registerColumnType(java.sql.Types.ARRAY, COLUMN_TYPE);

        // registering OGC functions
        // (spec_simplefeatures_sql_99-04.pdf)

        // CustomType for GeometryUserType
        CustomType geomType = new CustomType(SQLServer2008GeometryUserType.class, null);

        // section 2.1.1.1
        // Registerfunction calls for registering geometry functions:
        // first argument is the OGC standard functionname, 
        // second the Function as it occurs in the spatial dialect
        registerFunction("dimension", new SQLFunctionTemplate(Hibernate.INTEGER, "?1.STDimension()"));
        registerFunction("geometrytype", new SQLFunctionTemplate(Hibernate.STRING, "?1.STGeometryType()"));
        registerFunction("srid", new SQLFunctionTemplate(Hibernate.INTEGER, "?1.STSrid"));
        registerFunction("envelope", new SQLFunctionTemplate(geomType, "?1.STEnvelope()"));
        registerFunction("astext", new SQLFunctionTemplate(Hibernate.STRING, "?1.STAsText()"));
        registerFunction("asbinary", new SQLFunctionTemplate(Hibernate.BINARY, "?1.STAsBinary()"));

        registerFunction("isempty", new SQLFunctionTemplate(Hibernate.BOOLEAN, "?1.STIsEmpty()"));
        registerFunction("issimple", new SQLFunctionTemplate(Hibernate.BOOLEAN, "?1.STIsSimple()"));
        registerFunction("boundary", new SQLFunctionTemplate(geomType, "?1.STBoundary()"));

        // Register functions for spatial relation constructs
        registerFunction("contains", new SQLFunctionTemplate(Hibernate.BOOLEAN, "?1.STContains(?2)"));
        registerFunction("crosses", new SQLFunctionTemplate(Hibernate.BOOLEAN, "?1.STCrosses(?2)"));
        registerFunction("disjoint", new SQLFunctionTemplate(Hibernate.BOOLEAN, "?1.STDisjoint(?2)"));
        registerFunction("equals", new SQLFunctionTemplate(Hibernate.BOOLEAN, "?1.STEquals(?2)"));
        registerFunction("intersects", new SQLFunctionTemplate(Hibernate.BOOLEAN, "?1.STIntersects(?2)"));
        registerFunction("overlaps", new SQLFunctionTemplate(Hibernate.BOOLEAN, "?1.STOverlaps(?2)"));
        registerFunction("touches", new SQLFunctionTemplate(Hibernate.BOOLEAN, "?1.STTouches(?2)"));
        registerFunction("within", new SQLFunctionTemplate(Hibernate.BOOLEAN, "?1.STWithin(?2)"));
        registerFunction("relate", new SQLFunctionTemplate(Hibernate.BOOLEAN, "?1.STRelate(?2,?3)"));

    }

    public String getSpatialRelateSQL(String s, int i, boolean b) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getSpatialFilterExpression(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public UserType getGeometryUserType() {
        return new SQLServer2008GeometryUserType();
    }

    public String getSpatialAggregateSQL(String s, int i) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getDbGeometryTypeName() {
        return COLUMN_TYPE;
    }

    public boolean isTwoPhaseFiltering() {
        return false;
    }
}
