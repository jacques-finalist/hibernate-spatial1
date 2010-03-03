/*
 * $Id$
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
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import org.hibernatespatial.mgeom.MLineString;

import java.util.List;

class MultiLineStringDecoder extends AbstractGeometryCollectionDecoder<MultiLineString> {

    @Override
    protected OpenGisType getOpenGisType() {
        return OpenGisType.MULTILINESTRING;
    }


    @Override
    protected MultiLineString createGeometry(SqlGeometryV1 nativeGeom, List<Geometry> geometries) {
        if (nativeGeom.hasMValues()) {
            return getGeometryFactory().createMultiMLineString(geometries.toArray(new MLineString[geometries.size()]));
        }
        return getGeometryFactory().createMultiLineString(geometries.toArray(new LineString[geometries.size()]));
    }

}
