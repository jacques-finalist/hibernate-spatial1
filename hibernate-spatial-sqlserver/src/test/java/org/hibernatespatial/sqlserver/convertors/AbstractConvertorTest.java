/*
 * $Id$
 *
 * This file is part of Hibernate Spatial, an extension to the
 * hibernate ORM solution for geographic data.
 *
 * Copyright © 2009 Geovise BVBA
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
 * For more information, visit: http://www.hibernatespatial.org/
 */

package org.hibernatespatial.sqlserver.convertors;

import org.junit.BeforeClass;
import org.hibernatespatial.sqlserver.DataSourceUtils;

import java.util.Map;
import java.util.HashMap;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author Karel Maesen, Geovise BVBA.
 *         Date: Nov 2, 2009
 */
public class AbstractConvertorTest {

    Map<Integer, Geometry> decodedGeoms;
    Map<Integer, byte[]> rawResults;
    Map<Integer, byte[]> encodedGeoms;

    @BeforeClass
    public static void beforeClass() {
        DataSourceUtils.removeReadTestData();
        DataSourceUtils.loadReadTestData();
    }


    public void doDecoding(OpenGisType type) {
        rawResults = DataSourceUtils.rawByteArrays(type.toString());
        decodedGeoms = new HashMap<Integer, Geometry>();

        for (Integer id : rawResults.keySet()) {
            Geometry geometry = Decoders.decode(rawResults.get(id));
            decodedGeoms.put(id, geometry);
        }
    }

    public void doEncoding() {
        encodedGeoms = new HashMap<Integer, byte[]>();
        for (Integer id : decodedGeoms.keySet()) {
            Geometry geom = decodedGeoms.get(id);
            byte[] bytes = Encoders.encode(geom);
            encodedGeoms.put(id, bytes);
        }
    }
}

