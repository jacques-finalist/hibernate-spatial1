/**
 * Copyright 2010 Geodan IT b.v.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.geodan.hibernatespatial.geodb;

import geodb.GeoDB;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.Types;

import org.hibernatespatial.AbstractDBGeometryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author janb
 * 
 */
public class GeoDBGeometryUserType extends AbstractDBGeometryType {

	private static final Logger LOGGER = LoggerFactory.getLogger(GeoDBGeometryUserType.class);
	
	private static final int[] geometryTypes = new int[] { Types.ARRAY };

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hibernatespatial.AbstractDBGeometryType#sqlTypes()
	 */
	public int[] sqlTypes() {
		return geometryTypes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hibernatespatial.AbstractDBGeometryType#convert2JTS(java.lang.Object)
	 */
	@Override
	public Geometry convert2JTS(Object object) {
		if (object == null)
			return null;
		try {
			if (object instanceof Blob) {
				return GeoDB.gFromWKB(toByteArray((Blob) object));
			} else {
				throw new IllegalArgumentException(
						"Can't convert database object of type "
								+ object.getClass().getCanonicalName());
			}
		} catch (Exception e) {
			LOGGER.warn("Could not convert databae object to a JTS Geometry.");
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.hibernatespatial.AbstractDBGeometryType#conv2DBGeometry(com.
	 * vividsolutions.jts.geom.Geometry, java.sql.Connection)
	 */
	@Override
	public Object conv2DBGeometry(Geometry jtsGeom, Connection connection) {
        try
        {
            return GeoDB.gToWKB(jtsGeom);
        }
        catch (Exception e)
        {
        	LOGGER.warn("Could not convert JTS Geometry to a databae object.");
            e.printStackTrace();
            return null;
        }
	}
	
	private byte[] toByteArray(Blob blob) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];

		InputStream in = null;
		try {
			in = blob.getBinaryStream();
			int n = 0;
			while ((n=in.read(buf))>=0)
			{
			   baos.write(buf, 0, n);

			}
		} catch (Exception e) {
			LOGGER.warn("Could not convert database BLOB object to binary stream.");
			e.printStackTrace();
		} 
		finally {
			try {
				if(in != null) {
					in.close();
				}
			} catch (IOException e) {
				LOGGER.warn("Could not close binary stream.");
				e.printStackTrace();
			}
		}

		return baos.toByteArray(); 
	}

}
