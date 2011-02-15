package org.hibernatespatial.criterion;

import com.vividsolutions.jts.geom.Geometry;
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
 *         creation-date: 2/1/11
 */
public class DWithinExpression implements Criterion {


    private final String propertyName;
    private final Geometry geometry;
    private final double distance;

    public DWithinExpression(String propertyName, Geometry geometry, double distance) {
        this.propertyName = propertyName;
        this.geometry = geometry;
        this.distance = distance;
    }

    public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
        String column = ExpressionUtil.findColumn(propertyName, criteria, criteriaQuery);
        SpatialDialect spatialDialect = ExpressionUtil.getSpatialDialect(criteriaQuery, SpatialFunction.dwithin);
        return spatialDialect.getDWithinSQL(column);

    }


    public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
        return new TypedValue[]{
                criteriaQuery.getTypedValue(criteria, propertyName, geometry),
                new TypedValue(StandardBasicTypes.DOUBLE, Double.valueOf(distance), EntityMode.POJO)
        };
    }
}
