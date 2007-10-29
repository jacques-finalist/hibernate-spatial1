/**
 * $Id$
 *
 * This file is part of Spatial Hibernate, an extension to the 
 * hibernate ORM solution for geographic data. 
 *  
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
 * For more information, visit: http://www.cadrie.com/
 */

package org.hibernatespatial.oracle;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.QueryException;
import org.hibernate.dialect.Oracle9Dialect;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.type.CustomType;
import org.hibernate.type.Type;
import org.hibernate.usertype.UserType;
import org.hibernatespatial.SpatialAnalysis;
import org.hibernatespatial.SpatialDialect;
import org.hibernatespatial.SpatialRelation;
import org.hibernatespatial.criterion.SpatialAggregation;
import org.hibernatespatial.helper.PropertyFileReader;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Spatial Dialect for Oracle10g databases.
 * 
 * @author Karel Maesen
 */
public class OracleSpatial10gDialect extends Oracle9Dialect implements
		SpatialDialect {

	/**
	 * Implementation of the OGC astext function for HQL.
	 */
	private class AsTextFunction extends StandardSQLFunction {

		public AsTextFunction() {
			super("astext", Hibernate.STRING);
		}

		public String render(final List args,
				final SessionFactoryImplementor factory) {

			StringBuffer buf = new StringBuffer();
			if (args.isEmpty()) {
				throw new IllegalArgumentException(
						"First Argument in arglist must be object "
								+ "to which method is applied");
			}

			buf.append("TO_CHAR(SDO_UTIL.TO_WKTGEOMETRY(").append(args.get(0))
					.append("))");
			return buf.toString();
		}
	}

	/**
	 * Implements OGC function dimension for HQL.
	 */
	private class DimensionFunction extends SDOObjectMethod {

		public DimensionFunction() {
			super("Get_GType", Hibernate.INTEGER);
		}

		public String render(final List args,
				final SessionFactoryImplementor factory) {
			StringBuffer buf = new StringBuffer();
			if (args.isEmpty()) {
				throw new IllegalArgumentException(
						"First Argument in arglist must be object to "
								+ "which method is applied");
			}

			buf.append("CASE ").append(args.get(0)).append(".").append(
					getName()).append("()");
			buf.append(" WHEN 1 THEN 0").append(" WHEN 2 THEN 1").append(
					" WHEN 3 THEN 2").append(" WHEN 5 THEN 0").append(
					" WHEN 6 THEN 1").append(" WHEN 7 THEN 2").append(" END");
			return buf.toString();
		}
	}

	/**
	 * An HQL function that is implemented using Oracle's OGC compliance
	 * package.
	 */
	private class WrappedOGCFunction extends StandardSQLFunction {
		private final boolean[] geomArrays;

		/**
		 * @param name
		 *            function name
		 * @param type
		 *            return type of the function
		 * @param geomArrays
		 *            indicates which argument places are occupied by
		 *            sdo_geometries
		 */
		public WrappedOGCFunction(final String name, final Type type,
				final boolean[] geomArrays) {
			super(name, type);
			this.geomArrays = geomArrays;
		}

		public String render(final List args,
				final SessionFactoryImplementor factory) {

			StringBuffer buf = new StringBuffer();
			buf.append("MDSYS.").append(getName()).append("(");
			for (int i = 0; i < args.size(); i++) {
				if (i > 0) {
					buf.append(",");
				}
				if (geomArrays[i]) {
					buf.append("MDSYS.ST_GEOMETRY.FROM_SDO_GEOM(").append(
							args.get(i)).append(")");
				} else {
					buf.append(args.get(i));
				}

			}
			buf.append(")");
			return (getType().getReturnedClass() == Geometry.class) ? buf
					.append(".geom").toString() : buf.toString();
		}

	}

	/**
	 * HQL Implementation for the geometry ype function.
	 */
	private class GetGeometryTypeFunction extends SDOObjectMethod {

		public GetGeometryTypeFunction() {
			super("Get_GType", Hibernate.STRING);
		}

		public String render(final List args,
				final SessionFactoryImplementor factory) {
			StringBuffer buf = new StringBuffer();
			if (args.isEmpty()) {
				throw new IllegalArgumentException(
						"First Argument in arglist must be object to which"
								+ " method is applied");
			}

			buf.append("CASE ").append(args.get(0)).append(".").append(
					getName()).append("()");
			buf.append(" WHEN 1 THEN 'POINT'").append(
					" WHEN 2 THEN 'LINESTRING'").append(
					" WHEN 3 THEN 'POLYGON'").append(
					" WHEN 5 THEN 'MULTIPOINT'").append(
					" WHEN 6 THEN 'MULTILINE'").append(
					" WHEN 7 THEN 'MULTIPOLGYON'").append(" END");
			return buf.toString();
		}
	}

	/**
	 * HQL Spatial relation function.
	 */
	private class SpatialRelateFunction extends StandardSQLFunction {
		private final int relation;

		public SpatialRelateFunction(final String name, final int relation) {
			super(name, Hibernate.BOOLEAN);
			this.relation = relation;
		}

		public String render(final List args,
				final SessionFactoryImplementor factory) {

			if (args.size() < 2) {
				throw new QueryException(
						"Spatial relate functions require at least two arguments");
			}

			return isOGCStrict() ? getOGCSpatialRelateSQL((String) args.get(0),
					(String) args.get(1), this.relation)
					: getNativeSpatialRelateSQL((String) args.get(0),
							(String) args.get(1), this.relation);
		}

	}

	private class SpatialAnalysisFunction extends StandardSQLFunction {
		private final int analysis;

		public SpatialAnalysisFunction(String name, Type returnType,
				int analysis) {
			super(name, returnType);
			this.analysis = analysis;
		}

		public String render(List args, SessionFactoryImplementor factory) {
			return isOGCStrict() ? getSpatialAnalysisSQL(args, this.analysis,
					false) : getNativeSpatialAnalysisSQL(args, analysis);
		}

	}

	private class SpatialAggregationFunction extends StandardSQLFunction {

		private final int aggregation;

		private final boolean isProjection;

		public SpatialAggregationFunction(String name, Type returnType,
				boolean isProjection, int aggregation) {
			super(name, returnType);
			this.aggregation = aggregation;
			this.isProjection = isProjection;
		}

		public String render(List args, SessionFactoryImplementor factory) {
			return getNativeSpatialAggregateSQL((String) args.get(0),
					this.aggregation, isProjection);
		}
	}

	public final static String SHORT_NAME = "oraclespatial";

	private final static Log log = LogFactory
			.getLog(OracleSpatial10gDialect.class);

	private String OGC_STRICT = "OGC_STRICT";

	private Map<String, Boolean> features = new HashMap<String, Boolean>();

	public OracleSpatial10gDialect() {
		super();

		// initialise features to default
		features.put(OGC_STRICT, new Boolean(true));

		// read configuration information from
		// classpath
		configure();

		// register geometry type
		registerColumnType(java.sql.Types.STRUCT, "geometry");

		// registering OGC functions
		// (spec_simplefeatures_sql_99-04.pdf)

		// section 2.1.1.1
		registerFunction("dimension", new DimensionFunction());
		registerFunction("geometrytype", new GetGeometryTypeFunction());
		registerFunction("srid", new SDOObjectProperty("SDO_SRID",
				Hibernate.INTEGER));
		registerFunction("envelope",
				new StandardSQLFunction("SDO_GEOM.SDO_MBR", new CustomType(
						SDOGeometryType.class, null)));
		registerFunction("astext", new AsTextFunction());
		// Can't get these functions to work on XE
		// registerFunction("asbinary", new
		// StandardSQLFunction("SDO_UTIL.TO_WKBGEOMETRY"));
		registerFunction("isempty", new WrappedOGCFunction("OGC_ISEMPTY",
				Hibernate.BOOLEAN, new boolean[] { true }));
		registerFunction("issimple", new WrappedOGCFunction("OGC_ISSIMPLE",
				Hibernate.BOOLEAN, new boolean[] { true }));
		registerFunction("boundary", new WrappedOGCFunction("OGC_BOUNDARY",
				new CustomType(SDOGeometryType.class, null),
				new boolean[] { true }));

		// registerFunction("area", new AreaFunction());

		// Register functions for spatial relation constructs
		// section 2.1.1.2
		registerFunction("overlaps", new SpatialRelateFunction("overlaps",
				SpatialRelation.OVERLAPS));
		registerFunction("intersects", new SpatialRelateFunction("intersects",
				SpatialRelation.INTERSECTS));
		registerFunction("contains", new SpatialRelateFunction("intersects",
				SpatialRelation.CONTAINS));
		registerFunction("crosses", new SpatialRelateFunction("intersects",
				SpatialRelation.CROSSES));
		registerFunction("disjoint", new SpatialRelateFunction("intersects",
				SpatialRelation.DISJOINT));
		registerFunction("equals", new SpatialRelateFunction("intersects",
				SpatialRelation.EQUALS));
		registerFunction("touches", new SpatialRelateFunction("intersects",
				SpatialRelation.TOUCHES));
		registerFunction("within", new SpatialRelateFunction("intersects",
				SpatialRelation.WITHIN));
		registerFunction("relate", new WrappedOGCFunction("OGC_RELATE",
				Hibernate.BOOLEAN, new boolean[] { true, true, false }));

		// Register spatial analysis functions.
		// Section 2.1.1.3
		registerFunction("distance", new SpatialAnalysisFunction("distance",
				Hibernate.DOUBLE, SpatialAnalysis.DISTANCE));
		registerFunction("buffer", new SpatialAnalysisFunction("buffer",
				new CustomType(SDOGeometryType.class, null),
				SpatialAnalysis.BUFFER));
		registerFunction("convexhull", new SpatialAnalysisFunction(
				"convexhull", new CustomType(SDOGeometryType.class, null),
				SpatialAnalysis.CONVEXHULL));
		registerFunction("difference", new SpatialAnalysisFunction(
				"difference", new CustomType(SDOGeometryType.class, null),
				SpatialAnalysis.DIFFERENCE));
		registerFunction("intersection", new SpatialAnalysisFunction(
				"intersection", new CustomType(SDOGeometryType.class, null),
				SpatialAnalysis.INTERSECTION));
		registerFunction("symdifference", new SpatialAnalysisFunction(
				"symdifference", new CustomType(SDOGeometryType.class, null),
				SpatialAnalysis.SYMDIFFERENCE));
		registerFunction("geomunion", new SpatialAnalysisFunction("union",
				new CustomType(SDOGeometryType.class, null),
				SpatialAnalysis.UNION));
		// we rename OGC union to geomunion because union is a reserved SQL
		// keyword. (See also postgis documentation).

		// Spatial Aggregation
		registerFunction("lrsconcat", new SpatialAggregationFunction(
				"lrsconcat", new CustomType(SDOGeometryType.class, null),
				false, SpatialAggregation.LRS_CONCAT));
	}

	public UserType getGeometryUserType() {
		return new SDOGeometryType();
	}

	public String getNativeSpatialRelateSQL(String arg1, String arg2,
			int spatialRelation) {
		String mask = "";
		boolean negate = false;
		switch (spatialRelation) {
		case SpatialRelation.INTERSECTS:
			mask = "ANYINTERACT"; // OGC Compliance verified
			break;
		case SpatialRelation.CONTAINS:
			mask = "CONTAINS+COVERS";
			break;
		case SpatialRelation.CROSSES:
			throw new UnsupportedOperationException(
					"Oracle Spatial does't have equivalent CROSSES relationship");
		case SpatialRelation.DISJOINT:
			mask = "ANYINTERACT";
			negate = true;
			break;
		case SpatialRelation.EQUALS:
			mask = "EQUAL";
			break;
		case SpatialRelation.OVERLAPS:
			mask = "OVERLAPBDYDISJOINT+OVERLAPBDYINTERSECT";
			break;
		case SpatialRelation.TOUCHES:
			mask = "TOUCH";
			break;
		case SpatialRelation.WITHIN:
			mask = "INSIDE+COVEREDBY";
			break;
		default:
			throw new IllegalArgumentException(
					"undefined SpatialRelation passed (" + spatialRelation
							+ ")");
		}
		// The case formulation is necessary
		// to ensure that the expression can be used in the select and where
		// clauses.
		StringBuffer buffer = new StringBuffer("CASE SDO_RELATE(");
		buffer.append(arg1);
		buffer.append(",").append(arg2).append(",'mask=" + mask + "')");
		if (!negate) {
			buffer.append(" WHEN 'TRUE' THEN 1 ELSE 0 END");
		} else {
			buffer.append(" WHEN 'TRUE' THEN 0 ELSE 1 END");
		}
		return buffer.toString();
	}

	public String getOGCSpatialRelateSQL(String arg1, String arg2,
			int spatialRelation) {

		StringBuffer ogcFunction = new StringBuffer("MDSYS.");
		switch (spatialRelation) {
		case SpatialRelation.INTERSECTS:
			ogcFunction.append("OGC_INTERSECTS");
			break;
		case SpatialRelation.CONTAINS:
			ogcFunction.append("OGC_CONTAINS");
			break;
		case SpatialRelation.CROSSES:
			ogcFunction.append("OGC_CROSS");
			break;
		case SpatialRelation.DISJOINT:
			ogcFunction.append("OGC_DISJOINT");
			break;
		case SpatialRelation.EQUALS:
			ogcFunction.append("OGC_EQUALS");
			break;
		case SpatialRelation.OVERLAPS:
			ogcFunction.append("OGC_OVERLAP");
			break;
		case SpatialRelation.TOUCHES:
			ogcFunction.append("OGC_TOUCH");
			break;
		case SpatialRelation.WITHIN:
			ogcFunction.append("OGC_WITHIN");
			break;
		default:
			throw new IllegalArgumentException("Unknown SpatialRelation ("
					+ spatialRelation + ").");
		}
		ogcFunction.append("(").append("MDSYS.ST_GEOMETRY.FROM_SDO_GEOM(")
				.append(arg1).append("),").append(
						"MDSYS.ST_GEOMETRY.FROM_SDO_GEOM(").append(arg2)
				.append(")").append(")");
		return ogcFunction.toString();

	}

	public String getNativeSpatialAggregateSQL(String arg1, int aggregation,
			boolean isProjection) {

		StringBuffer aggregateFunction = new StringBuffer();

		SpatialAggregate sa = new SpatialAggregate(aggregation);

		if (sa._aggregateSyntax == null) {
			throw new IllegalArgumentException("Unknown Spatial Aggregation ("
					+ aggregation + ").");
		}

		aggregateFunction.append(sa._aggregateSyntax);

		aggregateFunction.append("(");
		if (sa.isAggregateType()) {
			aggregateFunction.append("SDOAGGRTYPE(");
		}
		aggregateFunction.append(arg1);
		if (sa.isAggregateType()) {
			aggregateFunction.append(", ").append(.001).append(")");
		}
		aggregateFunction.append(")");
		if (isProjection) {
			aggregateFunction.append(" as y");
		}
		return aggregateFunction.toString();
	}

	private StringBuffer wrapInSTGeometry(String geomColumn, StringBuffer toAdd) {
		return toAdd.append("MDSYS.ST_GEOMETRY(").append(geomColumn)
				.append(")");
	}

	public String getSpatialFilterExpression(String columnName) {
		StringBuffer buffer = new StringBuffer("SDO_FILTER(");
		// String pureColumnName =
		// columnName.substring(columnName.lastIndexOf(".")+1);
		// buffer.append("\"" + pureColumnName.toUpperCase() + "\"");
		buffer.append(columnName);
		buffer.append(",?) = 'TRUE' ");
		return buffer.toString();
	}

	public String getSpatialRelateSQL(String columnName, int spatialRelation,
			boolean useFilter) {

		String sql = (isOGCStrict() ? getOGCSpatialRelateSQL(columnName, "?",
				spatialRelation) : getNativeSpatialRelateSQL(columnName, "?",
				spatialRelation));
		sql += " = 1 and " + columnName + " is not null";
		return sql;
	}

	public String getSpatialAnalysisSQL(List args, int spatialAnalysisFunction,
			boolean useFilter) {
		return isOGCStrict() ? getOGCSpatialAnalysisSQL(args,
				spatialAnalysisFunction) : getNativeSpatialAnalysisSQL(args,
				spatialAnalysisFunction);
	}

	public String getSpatialAggregateSQL(String columnName,
			int spatialAggregateFunction, boolean isProjection) {
		return getNativeSpatialAggregateSQL(columnName,
				spatialAggregateFunction, isProjection);
	}

	private String getOGCSpatialAnalysisSQL(List args,
			int spatialAnalysisFunction) {
		boolean[] geomArgs;
		StringBuffer ogcFunction = new StringBuffer("MDSYS.");
		boolean isGeomReturn = true;
		switch (spatialAnalysisFunction) {
		case SpatialAnalysis.BUFFER:
			ogcFunction.append("OGC_BUFFER");
			geomArgs = new boolean[] { true, false };
			break;
		case SpatialAnalysis.CONVEXHULL:
			ogcFunction.append("OGC_CONVEXHULL");
			geomArgs = new boolean[] { true };
			break;
		case SpatialAnalysis.DIFFERENCE:
			ogcFunction.append("OGC_DIFFERENCE");
			geomArgs = new boolean[] { true, true };
			break;
		case SpatialAnalysis.DISTANCE:
			ogcFunction.append("OGC_DISTANCE");
			geomArgs = new boolean[] { true, true };
			isGeomReturn = false;
			break;
		case SpatialAnalysis.INTERSECTION:
			ogcFunction.append("OGC_INTERSECTION");
			geomArgs = new boolean[] { true, true };
			break;
		case SpatialAnalysis.SYMDIFFERENCE:
			ogcFunction.append("OGC_SYMMETRICDIFFERENCE");
			geomArgs = new boolean[] { true, true };
			break;
		case SpatialAnalysis.UNION:
			ogcFunction.append("OGC_UNION");
			geomArgs = new boolean[] { true, true };
			break;
		default:
			throw new IllegalArgumentException(
					"Unknown SpatialAnalysisFunction ("
							+ spatialAnalysisFunction + ").");
		}

		if (args.size() < geomArgs.length)
			throw new QueryException(
					"Insufficient arguments for spatial analysis function (function type:  "
							+ spatialAnalysisFunction + ").");

		ogcFunction.append("(");
		for (int i = 0; i < geomArgs.length; i++) {
			if (i > 0)
				ogcFunction.append(",");
			if (geomArgs[i])
				wrapInSTGeometry((String) args.get(i), ogcFunction);
			else
				ogcFunction.append(args.get(i));
		}
		ogcFunction.append(")");
		if (isGeomReturn)
			ogcFunction.append(".geom");
		return ogcFunction.toString();
	}

	private String getNativeSpatialAnalysisSQL(List args, int spatialAnalysis) {
		return getOGCSpatialAnalysisSQL(args, spatialAnalysis);
	}

	private boolean isOGCStrict() {
		return ((Boolean) this.features.get(OGC_STRICT)).booleanValue();
	}

	/**
	 * Returns the features supported by this Dialect.
	 * 
	 * @return Array of Feature names.
	 */
	public String[] getFeatures() {
		return this.features.keySet().toArray(new String[this.features.size()]);
	}

	public boolean getFeature(String name) {
		return this.features.get(name).booleanValue();
	}

	public void setFeature(String name, boolean value) {
		log.info("Setting feature: " + name + " to " + value);
		this.features.put(name, value);
	}

	private void configure() {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		String propfileLoc = getClass().getCanonicalName() + ".properties";
		URL propfile = loader.getResource(propfileLoc);
		if (propfile != null) {
			InputStream is = null;
			log.info("properties file found: " + propfile);
			try {
				loader.getResource(getClass().getCanonicalName());
				is = propfile.openStream();
				PropertyFileReader reader = new PropertyFileReader(is);
				Properties props = reader.getProperties();

				for (String feature : getFeatures()) {
					String newVal = props.getProperty(feature);
					if (newVal != null) {
						setFeature(feature, Boolean.parseBoolean(newVal));
					}

				}

			} catch (IOException e) {
				log.warn("Problem reading properties file " + e);
			} finally {
				try {
					is.close();
				} catch (Exception e) {
				}
			}
		}
	}

	/**
	 * Provides Aggregate type spatial function interpretation
	 */
	private class SpatialAggregate {

		boolean _aggregateType;

		String _aggregateSyntax;

		private final String SDO_AGGR = "SDO_AGGR_";

		protected SpatialAggregate() {
		}

		private SpatialAggregate(int aggregation) {

			String specificAggrSyntax;

			switch (aggregation) {
			case SpatialAggregation.LRS_CONCAT:
				specificAggrSyntax = "LRS_CONCAT";
				_aggregateType = true;
				break;
			case SpatialAggregation.CENTROID:
				specificAggrSyntax = "CENTROID";
				_aggregateType = true;
				break;
			case SpatialAggregation.CONCAT:
				specificAggrSyntax = "CONCAT_LINES";
				_aggregateType = false;
				break;
			default:
				specificAggrSyntax = null;
				break;
			}
			if (specificAggrSyntax != null) {
				_aggregateSyntax = SDO_AGGR + specificAggrSyntax;
			}
		}

		public boolean isAggregateType() {
			return _aggregateType;
		}

		public String getAggregateSyntax() {
			return _aggregateSyntax;
		}

	}
}
