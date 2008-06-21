/**
 * $Id$
 *
 * This file is part of Hibernate Spatial, an extension to the 
 * hibernate ORM solution for geographic data. 
 *  
 * Copyright © 2008 Geovise BVBA
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
package org.hibernatespatial.pojo.reader;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.hibernate.EntityMode;
import org.hibernate.metadata.ClassMetadata;
import org.hibernatespatial.helper.HSClassMetadata;

/**
 * Adapts arbitrary objects to the {@link Feature} interface using dynamic proxying.
 *  
 * @author Karel Maesen
 *
 */
public class FeatureAdapter {
	
	public static Feature adapt(Object o, ClassMetadata cf){
		
		return (Feature) Proxy.newProxyInstance(o.getClass().getClassLoader(),
				new Class[]{Feature.class},
				new FeatureInvocationHandler(o, cf));
	}
	
	static private class FeatureInvocationHandler implements InvocationHandler {
		private final Object target;
		private final HSClassMetadata metadata;
		private Method targetGeomGetter;
		private Method targetIdGetter;

	
		private static final Method geomGetter;
		private static final Method idGetter;
		private static final Method attrGetter;
		
		static {
			Class featureIntf = Feature.class;
			try{
				geomGetter = featureIntf.getDeclaredMethod("getGeometry",new Class[]{});
				idGetter = featureIntf.getDeclaredMethod("getId",new Class[]{});
				attrGetter = featureIntf.getDeclaredMethod("getAttribute", new Class[]{String.class});
			} catch (Exception e){
				throw new RuntimeException("Probable programming Error", e);
			}
		}	
		
		private FeatureInvocationHandler(Object o, ClassMetadata meta){
			//TODO check if this is sufficiently general. What if not a POJO?
			if (meta.getMappedClass(EntityMode.POJO) != o.getClass()){
				throw new RuntimeException("Metadata and POJO class do not cohere");
			}
			this.target = o;
			this.metadata = new HSClassMetadata(meta);
		}

		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			Method m = getTargetGetter(method, args);

			if ( m == null){
				return method.invoke(this.target, args);
			} else {
				return m.invoke(this.target, args);
			}
		}

		private Method getTargetGetter(Method invokedMethod, Object[]args){			
			try{
				if (invokedMethod.equals(geomGetter)){
					if (this.targetGeomGetter == null){
						this.targetGeomGetter = this.metadata.getGeomGetter();								
					}
					return this.targetGeomGetter;
				} else if (invokedMethod.equals(idGetter)){
					if (this.targetIdGetter == null){						
						this.targetIdGetter = this.metadata.getIdGetter();
					}
					return this.targetIdGetter;
				} else if (invokedMethod.equals(attrGetter)){
					String property = (String)args[0];					
					return this.metadata.getGetterFor(property);					
				} else {
					return null; 
				}
			}catch(Exception e){
				throw new RuntimeException("Problem getting suitable target method for method:  " + invokedMethod.getName(), e);
			}			
					
		}
	}
}

