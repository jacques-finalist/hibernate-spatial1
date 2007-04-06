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
public class SDOObjectMethod implements SQLFunction {

    private final Type type;
    private final String name;
    
    public SDOObjectMethod(String name, Type type){
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
	return true;
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
	    throw new QueryException("First Argument in arglist must be object to which method is applied");
	buf.append(args.get(0)).append(".").append( name ).append( '(' );
	for ( int i = 1; i < args.size(); i++ ) {
		buf.append( args.get( i ) );
		if ( i < args.size() - 1 ) {
			buf.append( ", " );
		}
	}
	return buf.append( ')' ).toString();
    }

}
