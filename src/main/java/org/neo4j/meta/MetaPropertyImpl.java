package org.neo4j.meta;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Transaction;

public final class MetaPropertyImpl extends MetaNodeWrapper
	implements MetaProperty
{
	private static final String PROPERTY_KEY_KEY = "key"; // heh :P
	private static final String PROPERTY_KEY_VALUE_TYPE = "value_type";
	
	private static Map<String, Class<?>> primitiveNameToClassMap;
	
	static
	{
		Map<String, Class<?>> primitiveMap = new HashMap<String, Class<?>>();
		primitiveMap.put( "boolean", boolean.class );
		primitiveMap.put( "byte", byte.class );
		primitiveMap.put( "short", short.class );
		primitiveMap.put( "int", int.class );
		primitiveMap.put( "long", long.class );
		primitiveMap.put( "float", float.class );
		primitiveMap.put( "double", double.class );
		primitiveMap.put( "char", char.class );
		primitiveNameToClassMap = Collections.unmodifiableMap( primitiveMap );
	}

	public MetaPropertyImpl( Node underlyingNode, MetaManager metaManager )
	{
		super( underlyingNode, metaManager );
	}
	
	public String getKey()
	{
		Transaction tx = Transaction.begin();
		try
		{
			String key = ( String) getUnderlyingNode().getProperty(
				PROPERTY_KEY_KEY );
			tx.success();
			return key;
		}
		finally
		{
			tx.finish();
		}
	}

	void setKey( String newKey )
	{
		Transaction tx = Transaction.begin();
		try
		{
			getUnderlyingNode().setProperty( PROPERTY_KEY_KEY, newKey );
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}

	public Class<?> getValueType()
	{
		Transaction tx = Transaction.begin();
		try
		{
			String propertyValue = ( String) getUnderlyingNode().getProperty(
				PROPERTY_KEY_VALUE_TYPE, null );
			Class<?> valueType = findClassOrNull( propertyValue );
			if ( valueType == null )
			{
				valueType = primitiveNameToClassMap.get( propertyValue );
			}
			tx.success();
			return valueType;
		}
		finally
		{
			tx.finish();
		}
	}
	
	private Class<?> findClassOrNull( String className )
	{
		try
		{
			return Class.forName( className );
		}
		catch ( Throwable t )
		{
			return null;
		}
	}

	public void setValueType( Class<?> valueTypeOrNullIfItDoesntMatter )
	{
		Transaction tx = Transaction.begin();
		try
		{
			if ( valueTypeOrNullIfItDoesntMatter == null )
			{
				removePropertyIfExists( PROPERTY_KEY_VALUE_TYPE );
			}
			else if ( isSupportedNeoPropertyType(
				valueTypeOrNullIfItDoesntMatter ) )
			{
				getUnderlyingNode().setProperty( PROPERTY_KEY_VALUE_TYPE,
					valueTypeOrNullIfItDoesntMatter.getName() );
			}
			else
			{
				throw new IllegalArgumentException( "Neo does not support " +
					"values of type: " + valueTypeOrNullIfItDoesntMatter );
			}
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	// Assumes in tx
	private void removePropertyIfExists( String key )
	{
		if ( getUnderlyingNode().hasProperty( PROPERTY_KEY_VALUE_TYPE ) )
		{
			getUnderlyingNode().removeProperty(	PROPERTY_KEY_VALUE_TYPE );					
		}
	}
	
	private boolean isSupportedNeoPropertyType( Class<?> type )
	{
		return type.isPrimitive() ||
			type.equals( Boolean.class ) ||
			type.equals( Byte.class ) ||
			type.equals( Short.class ) ||
			type.equals( Integer.class ) ||
			type.equals( Long.class ) ||
			type.equals( Float.class ) ||
			type.equals( Double.class ) ||
			type.equals( Character.class ) ||
			type.equals( String.class );
	}
}
