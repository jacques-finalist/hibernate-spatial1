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

import com.vividsolutions.jts.geom.*;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernatespatial.test.GeomEntity;

/**
 * Created by IntelliJ IDEA.
 * User: maesenka
 * Date: Mar 3, 2010
 * Time: 10:22:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class DataGenerator {


    private static GeometryFactory geomFactory;
    private static SessionFactory sessionFactory;

    public static void main(String[] args) {

        Configuration config = new Configuration();
        config.configure();
        config.addClass(GeomEntity.class);
        sessionFactory = config.buildSessionFactory();
        geomFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
        Session session = null;

        //generate 100.000 polygons
        for (int i = 0; i < 100; i++) {
            makeTransaction(1000);
        }
    }

    private static void makeTransaction(int count) {
        try {
            sessionFactory.getCurrentSession().beginTransaction();
            for (int i = 0; i < count; i++) {
                Polygon poly = createPolygon();
                GeomEntity entity = new GeomEntity();
                entity.setGeom(poly);
                sessionFactory.getCurrentSession().save(entity);
            }
            sessionFactory.getCurrentSession().getTransaction().commit();
        } catch (HibernateException he) {
            he.printStackTrace();
            sessionFactory.getCurrentSession().getTransaction().rollback();
        } finally {
            sessionFactory.getCurrentSession().close();
        }

    }

    private static Polygon createPolygon() {

        double centerX = -90.0 + Math.random() * (180.0);
        double centerY = -90.0 + Math.random() * (180.0);
        double radius = 10.0;
        Coordinate[] coordinates = new Coordinate[1000];
        for (int i = 0; i < 1000; i++) {
            double fi = i * 2 * Math.PI / 1000.0;
            Coordinate coordinate = new Coordinate(Math.cos(centerX + fi * radius), Math.sin(centerY + fi * radius));
            coordinates[i] = coordinate;
        }
        //protected against rouding errors
        coordinates[coordinates.length - 1] = coordinates[0];
        LinearRing shell = geomFactory.createLinearRing(coordinates);
        return geomFactory.createPolygon(shell, null);
    }

}



