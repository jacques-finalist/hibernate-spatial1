/**
 * $Id$
 *
 * This file is part of Hibernate Spatial, an extension to the 
 * hibernate ORM solution for geographic data. 
 *  
 * Copyright © 2007 Geovise BVBA
 * Copyright © 2007 K.U. Leuven LRD, Spatial Applications Division, Belgium
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
package org.hibernatespatial.mgeom;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Point;

public class EventLocator {

	/**
	 * Returns the point on the specified MGeometry where its measure equals the specified position.
     *
	 * @return a Point Geometry
	 * @throws MGeometryException
	 */
	public static Point getPointGeometry(MGeometry lrs, double position)
			throws MGeometryException {
        if (lrs == null) {
            throw new MGeometryException("Non-null MGeometry parameter is required.");
        }
		Coordinate c = lrs.getCoordinateAtM(position);
        return lrs.getFactory().createPoint(c);

	}

	public static MultiMLineString getLinearGeometry(MGeometry lrs,
			double begin, double end) throws MGeometryException {

         if (lrs == null) {
            throw new MGeometryException("Non-null MGeometry parameter is required.");
        }
		MGeometryFactory factory = (MGeometryFactory) lrs.getFactory();
		CoordinateSequence[] cs = lrs.getCoordinatesBetween(begin, end);
		MLineString[] mlar = new MLineString[cs.length];
		for (int i = 0; i < cs.length; i++) {
            MLineString ml;
            if (cs[i].size() < 2) {
                ml = factory.createMLineString((CoordinateSequence) null);
            } else {
			    ml = factory.createMLineString(cs[i]);
            }
			mlar[i] = ml;
		}
        return factory.createMultiMLineString(mlar);
	}

}
