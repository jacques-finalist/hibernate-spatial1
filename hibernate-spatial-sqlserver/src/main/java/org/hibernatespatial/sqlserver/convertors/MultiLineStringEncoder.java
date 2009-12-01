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

package org.hibernatespatial.sqlserver.convertors;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiLineString;
import org.hibernatespatial.mgeom.MultiMLineString;

public class MultiLineStringEncoder extends AbstractEncoder<MultiLineString> {

    public SqlGeometryV1 encode(MultiLineString geom) {
        SqlGeometryV1 nativeGeom = new SqlGeometryV1();
        nativeGeom.setSrid(geom.getSRID());
        if (geom.isValid()) nativeGeom.setIsValid();
        nativeGeom.setNumberOfPoints(geom.getNumPoints());
        if (geom instanceof MultiMLineString)
            nativeGeom.setHasMValues();
        encodePoints(nativeGeom, geom);
        encodeFigures(nativeGeom, geom);
        encodeShapes(nativeGeom, geom);
        return nativeGeom;
    }

    protected void encodeFigures(SqlGeometryV1 nativeGeom, Geometry geom) {
        nativeGeom.setNumberOfFigures(geom.getNumGeometries());
        int offset = 0;
        for (int i = 0; i < geom.getNumGeometries(); i++) {
            Geometry ls = geom.getGeometryN(i);
            Figure figure = new Figure(FigureAttribute.Stroke, offset);
            nativeGeom.setFigure(i, figure);
            offset += ls.getNumPoints();
        }
    }

    protected void encodeShapes(SqlGeometryV1 nativeGeom, Geometry geom) {
        MultiLineString mls = (MultiLineString) geom;
        //number of shapes is 1 + number of linestrings
        nativeGeom.setNumberOfShapes(geom.getNumGeometries() + 1);
        //first encode the parent
        Shape parent = new Shape(-1, 0, OpenGisType.MULTILINESTRING);
        nativeGeom.setShape(0, parent);
        for (int i = 0; i < geom.getNumGeometries(); i++) {
            Shape shape = new Shape(0, i, OpenGisType.LINESTRING);
            nativeGeom.setShape(1 + i, shape);
        }
    }


    public boolean accepts(Geometry geom) {
        return geom instanceof MultiLineString;
    }
}
