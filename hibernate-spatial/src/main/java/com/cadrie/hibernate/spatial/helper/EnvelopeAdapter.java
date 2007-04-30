package com.cadrie.hibernate.spatial.helper;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

public class EnvelopeAdapter {

    static private GeometryFactory geomFactory = new GeometryFactory();

    static public Polygon toPolygon(Envelope env, int SRID) {
        Coordinate[] coords = new Coordinate[5];

        coords[0] = new Coordinate(env.getMinX(), env.getMinY());
        coords[1] = new Coordinate(env.getMinX(), env.getMaxY());
        coords[2] = new Coordinate(env.getMaxX(), env.getMaxY());
        coords[3] = new Coordinate(env.getMaxX(), env.getMinY());
        coords[4] = new Coordinate(env.getMinX(), env.getMinY());
        LinearRing shell = geomFactory.createLinearRing(coords);

        Polygon pg = geomFactory.createPolygon(shell, null);
        pg.setSRID(SRID);
        return pg;
    }

    public static void setGeometryFactory(GeometryFactory gf) {
        geomFactory = gf;
    }

}
