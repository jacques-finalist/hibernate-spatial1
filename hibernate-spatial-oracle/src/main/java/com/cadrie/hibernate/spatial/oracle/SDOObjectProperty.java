/**
 * 
 */
package com.cadrie.hibernate.spatial.oracle;

import java.util.List;

import org.hibernate.QueryException;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.engine.Mapping;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.type.Type;

/**
 * @author maesenka
 *
 */
public class SDOObjectProperty implements SQLFunction {

    private final Type type;
    private final String name;
    
    public SDOObjectProperty(String name, Type type){
	this.type = type;
	this.name = name;
    }
    
    /* (non-Javadoc)
     * @see org.hibernate.dialect.function.SQLFunction#getReturnType(org.hibernate.type.Type, org.hibernate.engine.Mapping)
     */
    public Type getReturnType(Type columnType, Mapping mapping)
	    throws QueryException {
	return type == null ? columnType : type;
    }

    /* (non-Javadoc)
     * @see org.hibernate.dialect.function.SQLFunction#hasArguments()
     */
    public boolean hasArguments() {
	return true;
    }

    /* (non-Javadoc)
     * @see org.hibernate.dialect.function.SQLFunction#hasParenthesesIfNoArguments()
     */
    public boolean hasParenthesesIfNoArguments() {
	return false;
    }
    
    public String getName(){
	return this.name;
    }

    /* (non-Javadoc)
     * @see org.hibernate.dialect.function.SQLFunction#render(java.util.List, org.hibernate.engine.SessionFactoryImplementor)
     */
    public String render(List args, SessionFactoryImplementor factory)
	    throws QueryException {
	StringBuffer buf = new StringBuffer();
	if (args.isEmpty())
	    throw new QueryException("First Argument in arglist must be object of which property is queried");
	buf.append(args.get(0)).append(".").append(name);
	return buf.toString();
    }

}
