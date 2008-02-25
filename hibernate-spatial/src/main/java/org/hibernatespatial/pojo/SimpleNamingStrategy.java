package org.hibernatespatial.pojo;

/**
 * This is the default implementation for a <code>NamingStrategy</code>.
 * 
 * @author Karel Maesen, Geovise BVBA (http://www.geovise.com/)
 */
public class SimpleNamingStrategy implements NamingStrategy {
	


	public String createClassName(String base) {
		String cleaned = toJavaName(base);
		cleaned = cleaned.toLowerCase();
		return capitalize(cleaned);
	}

	public String createGetterName(String fieldName) {		
		return "get" + capitalize(fieldName);
	}

	public String createPropertyName(String base) {
		String cleaned = toJavaName(base);
		cleaned = cleaned.toLowerCase();
		return cleaned;
	}

	public String createSetterName(String fieldName) {
		return "set" + capitalize(fieldName);

	}


	/**
	 * 
	 * Turns the name into a valid, simplified Java Identifier.
	 * @param name
	 * @return
	 */
	private String toJavaName(String name ){
		StringBuilder stb = new StringBuilder();
		char[] namechars = name.toCharArray();
		if (!Character.isJavaIdentifierStart(namechars[0])){
			stb.append("__");
		} else {
			stb.append(namechars[0]);
		}
		for(int i = 1; i < namechars.length; i++){
			if ( !Character.isJavaIdentifierPart(namechars[i])){
				 stb.append("__");
			} else {
				stb.append(namechars[i]);
			}
		}
		
		return stb.toString();
	}
	
	private String capitalize(String s){
		char[] ca = s.toCharArray();
		ca[0] = Character.toUpperCase(ca[0]);
		return new String(ca);		
	}
	
	@SuppressWarnings("unused")
	private String uncapitalize(final String s){
		final char[] ca = s.toCharArray();
		ca[0] = Character.toLowerCase(ca[0]);
		return new String(ca);		
	}
}
