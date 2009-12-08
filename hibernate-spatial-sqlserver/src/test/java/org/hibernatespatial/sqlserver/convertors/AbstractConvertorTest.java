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
import org.hibernatespatial.sqlserver.DataSourceUtils;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Karel Maesen, Geovise BVBA.
 *         Date: Nov 2, 2009
 */
public class AbstractConvertorTest {

    Map<Integer, Geometry> decodedGeoms;
    Map<Integer, byte[]> rawResults;
    Map<Integer, byte[]> encodedGeoms;
    Map<Integer, Geometry> expectedGeoms;

    @BeforeClass
    public static void beforeClass() {
        DataSourceUtils.removeReadTestData();
        DataSourceUtils.loadReadTestData();
    }


    public void doDecoding(OpenGisType type) {
        rawResults = DataSourceUtils.rawByteArrays(type.toString());
        expectedGeoms = DataSourceUtils.expectedGeoms(type.toString());
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

    public void test_encoding() {
        for (Integer id : encodedGeoms.keySet()) {
            assertTrue("Wrong encoding for case " + id, Arrays.equals(rawResults.get(id), encodedGeoms.get(id)));
        }
    }

    public void test_decoding() {
        for (Integer id : decodedGeoms.keySet()) {
            Geometry expected = expectedGeoms.get(id);
            Geometry received = decodedGeoms.get(id);
            if (expected == null) continue; //not all geometries can currently be parsed by WKTReader
            assertTrue("Wrong decoding for case " + id, expected.equalsExact(received));
        }
    }
}
