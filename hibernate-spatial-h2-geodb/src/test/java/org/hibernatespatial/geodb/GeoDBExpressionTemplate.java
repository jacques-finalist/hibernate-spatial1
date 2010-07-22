package org.hibernatespatial.geodb;

import org.hibernatespatial.test.SQLExpressionTemplate;
import org.hibernatespatial.test.TestDataElement;

/**
 * This is the template for insert SQL statements into the geomtest test table
 * for GeoDB.
 * 
 * @author Jan Boonen, Geodan IT b.v.
 */
public class GeoDBExpressionTemplate implements SQLExpressionTemplate {

	final String SQL_TEMPLATE = "insert into GEOMTEST values (%d, '%s', ST_GeomFromText('%s', %d))";

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.hibernatespatial.test.SQLExpressionTemplate#toInsertSql(org.
	 * hibernatespatial.test.TestDataElement)
	 */
	public String toInsertSql(TestDataElement testDataElement) {
		return String
				.format(SQL_TEMPLATE, testDataElement.id, testDataElement.type,
						testDataElement.wkt, testDataElement.srid);
	}

}
