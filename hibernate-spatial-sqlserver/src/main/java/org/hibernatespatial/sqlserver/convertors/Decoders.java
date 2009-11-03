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

import java.util.List;
import java.util.ArrayList;

/**
 * @author Karel Maesen, Geovise BVBA.
 *         Date: Nov 2, 2009
 */
public class Decoders {

    final private static List<Decoder<? extends Geometry>> DECODERS = new ArrayList<Decoder<? extends Geometry>>();
    final private static List<Encoder<? extends Geometry>> ENCODERS = new ArrayList<Encoder<? extends Geometry>>();


    static {
        //Decoders
        DECODERS.add(new PointDecoder());

        //Encoders
        ENCODERS.add(new PointEncoder());

    }


    private static Decoder<? extends Geometry> decoderFor(SqlGeometryV1 object) {
        for (Decoder<? extends Geometry> decoder : DECODERS) {
            if (decoder.accepts(object))
                return decoder;
        }
        throw new IllegalArgumentException("No decoder for type " + object.openGisType());
    }

    private static Encoder<? extends Geometry> encoderFor(Geometry geom) {
        for (Encoder<? extends Geometry> encoder : ENCODERS) {
            if (encoder.accepts(geom))
                return encoder;
        }
        throw new IllegalArgumentException("No encoder for type " + geom.getGeometryType());
    }

    public static Geometry decode(byte[] raw) {
        SqlGeometryV1 sqlGeom = SqlGeometryV1.load(raw);
        Decoder<?> decoder = decoderFor(sqlGeom);
        return decoder.decode(sqlGeom);
    }

    public static <T extends Geometry> SqlGeometryV1 encode(T geom) {
        Encoder<T> encoder = (Encoder<T>) encoderFor(geom);
        return encoder.encode(geom);
    }

}
