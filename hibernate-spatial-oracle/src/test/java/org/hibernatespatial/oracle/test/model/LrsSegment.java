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
package org.hibernatespatial.oracle.test.model;

import java.io.Serializable;

import org.hibernatespatial.mgeom.MCoordinateSequence;

import com.vividsolutions.jts.geom.Geometry;

/**
 * A domain object for Testing the LRS functionality.
 */
public class LrsSegment implements Serializable {
	int id = -1;

	Geometry geometry;

	String description;

	public LrsSegment(Geometry geometry, String description) {
		this.geometry = geometry;
		this.description = description;
	}

	public LrsSegment() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Basic attempt to get segment length from the coordinates. This type of
	 * functionality could be applicable to an LrsLineString type of geometry at
	 * some point.
	 */
	public double getLrsLength() {
		if (geometry == null)
			return Double.NaN;
		// otherwise
		MCoordinateSequence seq = new MCoordinateSequence(geometry
				.getCoordinates());
		if (seq.size() > 1) {
			int lastIndex = seq.size() - 1;
			double begin = seq.getM(0);
			double end = seq.getM(lastIndex);
			if (begin != Double.NaN && end != Double.NaN) {
				return end - begin;
			}
		}
		// for now, I'll return 0 but have to decide what to do
		// with either non-measure values or points (points are generally 0
		// length)
		return 0D;
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		LrsSegment that = (LrsSegment) o;

		if (id != that.id)
			return false;
		if (description != null ? !description.equals(that.description)
				: that.description != null)
			return false;
		if (geometry != null ? !geometry.equals(that.geometry)
				: that.geometry != null)
			return false;

		return true;
	}

	public int hashCode() {
		int result;
		result = id;
		result = 31 * result + (geometry != null ? geometry.hashCode() : 0);
		result = 31 * result
				+ (description != null ? description.hashCode() : 0);
		return result;
	}
}
