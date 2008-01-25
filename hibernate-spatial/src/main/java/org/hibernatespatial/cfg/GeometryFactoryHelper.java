/**
 * $Id$
 *
 * This file is part of Hibernate Spatial, an extension to the 
 * hibernate ORM solution for geographic data. 
 *  
 * Copyright © 2007 Geovise BVBA
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
package org.hibernatespatial.cfg;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernatespatial.mgeom.MGeometryFactory;

import com.vividsolutions.jts.geom.PrecisionModel;

public class GeometryFactoryHelper {

	private static Log logger = LogFactory.getLog(GeometryFactoryHelper.class);

	public static MGeometryFactory createGeometryFactory(Map map) {

		if (map == null) {
			return new MGeometryFactory();
		}
		String precisionModelName = null;
		Double scale = null;
		if (map.containsKey(HSProperty.PRECISION_MODEL.toString())) {
			precisionModelName = (String) map.get(HSProperty.PRECISION_MODEL
					.toString());
		}
		if (map.containsKey(HSProperty.PRECISION_MODEL_SCALE.toString())) {
			scale = Double.parseDouble(((String) map
					.get(HSProperty.PRECISION_MODEL_SCALE.toString())));
		}
		if (scale != null && !scale.isNaN() && precisionModelName != null
				&& precisionModelName.equalsIgnoreCase("FIXED")) {
			return new MGeometryFactory(new PrecisionModel(scale));
		}
		if (precisionModelName == null) {
			return new MGeometryFactory();
		}
		if (precisionModelName.equalsIgnoreCase("FIXED")) {
			return new MGeometryFactory(
					new PrecisionModel(PrecisionModel.FIXED));
		}
		if (precisionModelName.equalsIgnoreCase("FLOATING")) {
			return new MGeometryFactory(new PrecisionModel(
					PrecisionModel.FLOATING));
		}
		if (precisionModelName.equalsIgnoreCase("FLOATING_SINGLE")) {
			return new MGeometryFactory(new PrecisionModel(
					PrecisionModel.FLOATING_SINGLE));
		}
		logger.warn("Configured for PrecisionModel: " + precisionModelName
				+ " but don't know how to instantiate.");
		logger.warn("Reverting to default GeometryModel");
		return new MGeometryFactory();
	}

}
