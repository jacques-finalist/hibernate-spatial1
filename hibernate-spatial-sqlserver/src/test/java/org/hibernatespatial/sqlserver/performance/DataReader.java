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

package org.hibernatespatial.sqlserver.performance;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import org.hibernate.*;
import org.hibernate.cfg.Configuration;
import org.hibernatespatial.test.GeomEntity;

/**
 * Created by IntelliJ IDEA.
 * User: maesenka
 * Date: Mar 3, 2010
 * Time: 10:58:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class DataReader {


    private static GeometryFactory geomFactory;
    private static SessionFactory sessionFactory;

    public static void main(String[] args) {
        Configuration config = new Configuration();
        config.configure();
        config.addClass(GeomEntity.class);
        sessionFactory = config.buildSessionFactory();
        geomFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);

        try {
            Transaction tx = sessionFactory.getCurrentSession().beginTransaction();
            Criteria criteria = sessionFactory.getCurrentSession().createCriteria(GeomEntity.class);
            ScrollableResults results = criteria.scroll(ScrollMode.FORWARD_ONLY);
            while (results.next()) {
                GeomEntity entity = (GeomEntity) results.get()[0];
                sessionFactory.getCurrentSession().evict(entity);
                if (entity.getId() % 1000 == 0)
                    System.out.println("Reading entity with id: " + entity.getId());
            }
            results.close();
            tx.commit();
        } finally {

        }
    }
}
