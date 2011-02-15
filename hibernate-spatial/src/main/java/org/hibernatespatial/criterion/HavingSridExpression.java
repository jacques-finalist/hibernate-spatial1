package org.hibernatespatial.criterion;

import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Criterion;
import org.hibernate.engine.TypedValue;
import org.hibernate.type.StandardBasicTypes;
import org.hibernatespatial.SpatialDialect;
import org.hibernatespatial.SpatialFunction;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: 2/9/11
 */
public class HavingSridExpression implements Criterion {

    private final String propertyName;
    private final int srid;

    public HavingSridExpression(String propertyName, int srid) {
        this.propertyName = propertyName;
        this.srid = srid;
    }

    public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
        String column = ExpressionUtil.findColumn(propertyName, criteria, criteriaQuery);
        SpatialDialect spatialDialect = ExpressionUtil.getSpatialDialect(criteriaQuery, SpatialFunction.srid);
        return spatialDialect.getHavingSridSQL(column);
    }

    public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
        return new TypedValue[]{
                new TypedValue(StandardBasicTypes.INTEGER, Integer.valueOf(srid), EntityMode.POJO)
        };
    }

}
