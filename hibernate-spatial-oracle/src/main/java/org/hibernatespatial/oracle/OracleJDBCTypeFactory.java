package org.hibernatespatial.oracle;

import oracle.jdbc.OracleConnection;
import oracle.sql.*;
import org.hibernate.HibernateException;
import org.hibernatespatial.helper.FinderException;

import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Struct;

/**
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Jul 3, 2010
 */
public class OracleJDBCTypeFactory implements SQLTypeFactory {

    private static ConnectionFinder connectionFinder = new DefaultConnectionFinder();

    static ConnectionFinder getConnectionFinder() {
        return connectionFinder;
    }

    static void setConnectionFinder(ConnectionFinder finder) {
        connectionFinder = finder;
    }

    static String getTypeName() {
        return SDOGeometryType.getTypeName();
    }

    public Struct createStruct(SDOGeometry geom, Connection conn) throws SQLException {
        OracleConnection oracleConnection = null;
        try {
            oracleConnection = connectionFinder.find(conn);
        } catch (FinderException e) {
            throw new HibernateException("Problem finding Oracle Connection", e);
        }
        StructDescriptor structDescriptor = StructDescriptor
                .createDescriptor(SDOGeometryType.SQL_TYPE_NAME, oracleConnection);
        Object[] attributes = new Datum[5];
        attributes[0] = new NUMBER(geom.getGType().intValue());
        if (geom.getSRID() > 0) {
            attributes[1] = new NUMBER(geom.getSRID());
        } else {
            attributes[1] = null;
        }
        attributes[3] = createElemInfoArray(geom.getInfo(), oracleConnection);
        attributes[4] = createOrdinatesArray(geom.getOrdinates(), oracleConnection);
        return new STRUCT(structDescriptor, oracleConnection, attributes);
    }

    public Array createElemInfoArray(ElemInfo elemInfo, Connection conn) throws SQLException {
        ArrayDescriptor arrayDescriptor = ArrayDescriptor.createDescriptor(
                ElemInfo.TYPE_NAME, conn);
        return new ARRAY(arrayDescriptor, conn, elemInfo.getElements());
    }

    public Array createOrdinatesArray(Ordinates ordinates, Connection conn) throws SQLException {
        ArrayDescriptor arrayDescriptor = ArrayDescriptor.createDescriptor(Ordinates.TYPE_NAME, conn);
        return new ARRAY(arrayDescriptor, conn, ordinates.getOrdinateArray());

    }
}
