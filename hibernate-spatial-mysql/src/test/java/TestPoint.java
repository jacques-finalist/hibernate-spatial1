import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ByteOrderValues;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBWriter;
import com.vividsolutions.jts.io.WKTReader;

public class TestPoint {

	public static void main(String[] args) {
		try {

			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection conn = DriverManager
					.getConnection("jdbc:mysql://test.geovise.com/testhbs?"
							+ "user=hibernate&password=hibernate");

			Statement stmt = conn.createStatement();
			ResultSet results = stmt.executeQuery("select * from test");
			results.first();
			byte[] wkb = results.getBytes(1);
			System.out.println("Write the mysql geom bytes out");
			for (int i = 0; i < wkb.length; i++) {
				System.out.print(wkb[i]);
				System.out.print(' ');
			}
			System.out.println();
			// check whether we can input these bytes.
			PreparedStatement pstmt = conn
					.prepareStatement("insert into test values(?)");
			pstmt.setBytes(1, wkb);
			pstmt.execute();
			System.out.println("Saved point in mysql");

			WKTReader reader = new WKTReader();
			WKBWriter writer = new WKBWriter(2, ByteOrderValues.LITTLE_ENDIAN);
			Geometry geom = reader.read("POINT(1 1)");
			byte[] jtswkb = writer.write(geom);

			int srid = 31370;
			byte[] result = new byte[jtswkb.length + 4];

			result[3] = (byte) ((srid >> 24) & 0xFF);
			result[2] = (byte) ((srid >> 16) & 0xFF);
			result[1] = (byte) ((srid >> 8) & 0xFF);
			result[0] = (byte) (srid & 0xFF);

			System.arraycopy(jtswkb, 0, result, 4, jtswkb.length);

			System.out.println("Write the jts wkb geom bytes out");
			for (int i = 0; i < result.length; i++) {
				System.out.print(result[i]);
				System.out.print(' ');
			}
			System.out.println();
			pstmt.setBytes(1, result);
			pstmt.execute();

		} catch (SQLException ex) {
			// handle any errors
			ex.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
