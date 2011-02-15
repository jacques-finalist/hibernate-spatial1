package org.hibernatespatial.criterion;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.dialect.Dialect;
import org.hibernatespatial.SpatialDialect;
import org.hibernatespatial.SpatialFunction;

/**
 * This class assists in the formation of a SQL-fragment in the various spatial query expressions.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/15/11
 */
public class ExpressionUtil {

    public static SpatialDialect getSpatialDialect(CriteriaQuery criteriaQuery, SpatialFunction function) {
        Dialect dialect = criteriaQuery.getFactory().getDialect();
        if (!(dialect instanceof SpatialDialect)) {
            throw new HibernateException("A spatial expression requires a spatial dialect.");
        }
        SpatialDialect spatialDialect = (SpatialDialect) dialect;
        if (!spatialDialect.supports(function)) {
            throw new HibernateException(function + " function not supported by this dialect");
        }
        return spatialDialect;
    }

    public static String findColumn(String propertyName, Criteria criteria, CriteriaQuery criteriaQuery) {
        String[] columns = criteriaQuery.findColumns(propertyName, criteria);
        if (columns.length != 1)
            throw new HibernateException("Spatial Expression may only be used with single-column properties");
        return columns[0];
    }
}
