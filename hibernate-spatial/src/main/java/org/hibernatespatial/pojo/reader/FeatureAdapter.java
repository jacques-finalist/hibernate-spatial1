package org.hibernatespatial.pojo.reader;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.hibernate.EntityMode;
import org.hibernate.metadata.ClassMetadata;
import org.hibernatespatial.helper.HSClassMetadata;

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

