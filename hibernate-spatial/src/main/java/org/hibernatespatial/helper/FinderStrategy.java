package org.hibernatespatial.helper;

/**
 * A <code>FindStrategy</code> is used to find a specific feature.
 * It is useful in cases where reflection is used to determine
 * some property of a class.   
 * 
 * @author Karel Maesen
 *
 * @param <T> the return type of the <code>find</code> method
 * @param <S> the type of subject
 */
public interface FinderStrategy <T,S> {
	
	/**
	 * Find a feature or property of a subject
	 * @param subject the object that is being searched
	 * @return the object sought
	 * @throws FinderException thrown when the feature is not sought;
	 */
	public T find(S subject) throws FinderException;

}
