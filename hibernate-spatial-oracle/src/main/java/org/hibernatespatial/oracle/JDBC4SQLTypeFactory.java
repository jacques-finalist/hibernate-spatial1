package org.hibernatespatial.oracle;

import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Struct;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Jul 3, 2010
 */
public class JDBC4SQLTypeFactory implements SQLTypeFactory {

    public Struct createStruct(SDOGeometry geom, Connection conn) throws SQLException {
        Object[] attributes = new Object[5];
        attributes[0] = geom.getGType().intValue();  //todo -- avoid autoboxing
        if (geom.getSRID() > 0) {
            attributes[1] = geom.getSRID(); //todo -- avoid autoboxing
        } else {
            attributes[1] = null;
        }
        attributes[3] = createElemInfoArray(geom.getInfo(), conn);
        attributes[4] = createOrdinatesArray(geom.getOrdinates(), conn);
        return conn.createStruct(SDOGeometry.getTypeName(), attributes);
    }

    public Array createElemInfoArray(ElemInfo elemInfo, Connection conn) throws SQLException {
        return conn.createArrayOf(ElemInfo.TYPE_NAME, elemInfo.getElements());
    }

    public Array createOrdinatesArray(Ordinates ordinates, Connection conn) throws SQLException {
        return conn.createArrayOf(Ordinates.TYPE_NAME, ordinates.getOrdinateArray());
    }
}
