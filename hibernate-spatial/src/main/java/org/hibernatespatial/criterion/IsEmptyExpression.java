package org.hibernatespatial.criterion;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Criterion;
import org.hibernate.engine.TypedValue;
import org.hibernatespatial.SpatialDialect;
import org.hibernatespatial.SpatialFunction;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/15/11
 */
public class IsEmptyExpression implements Criterion {

    private final static TypedValue[] NO_VALUES = new TypedValue[0];

    private final String propertyName;
    private final boolean isEmpty;

    public IsEmptyExpression(String propertyName, boolean isEmpty) {
        this.propertyName = propertyName;
        this.isEmpty = isEmpty;
    }

    public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
        String column = ExpressionUtil.findColumn(propertyName, criteria, criteriaQuery);
        SpatialDialect spatialDialect = ExpressionUtil.getSpatialDialect(criteriaQuery, SpatialFunction.isempty);
        return spatialDialect.getIsEmptySQL(column, isEmpty);
    }

    public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
        return NO_VALUES;
    }

}
