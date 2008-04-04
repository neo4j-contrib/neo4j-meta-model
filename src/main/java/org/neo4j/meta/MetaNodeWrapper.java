package org.neo4j.meta;

import java.lang.reflect.Constructor;

import org.neo4j.api.core.Node;
import org.neo4j.api.core.Transaction;
import org.neo4j.util.NodeWrapperImpl;

public abstract class MetaNodeWrapper extends NodeWrapperImpl
{
	private MetaManager metaManager;
	
	public MetaNodeWrapper( Node underlyingNode, MetaManager metaManager )
	{
		super( underlyingNode );
		this.metaManager = metaManager;
	}
	
	protected MetaManager getMetaManager()
	{
		return this.metaManager;
	}
	
	protected void cascadingDelete()
	{
		getUnderlyingNode().delete();
	}
	
	public static <T extends MetaNodeWrapper> T newInstance(
		Class<T> wrapperClass, Node node, MetaManager metaManager )
	{
		try
		{
			Constructor<T> constructor =
				wrapperClass.getConstructor( Node.class, MetaManager.class );
			T result = constructor.newInstance( node, metaManager );
			return result;
		}
		catch ( RuntimeException e )
		{
			throw e;
		}
		catch ( Exception e )
		{
			throw new RuntimeException( e );
		}
	}
	
	protected void setPropertyOnNode( String key, Object value )
	{
		if ( value == null )
		{
			throw new IllegalArgumentException( "Value for property '" +
				key + "' can't be null" );
		}
		Transaction tx = Transaction.begin();
		try
		{
			getUnderlyingNode().setProperty( key, value );
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	protected Object getPropertyFromNode( String key )
	{
		Transaction tx = Transaction.begin();
		try
		{
			Object value = getUnderlyingNode().getProperty( key );
			tx.success();
			return value;
		}
		finally
		{
			tx.finish();
		}		
	}
	
	protected Object getPropertyFromNode( String key, Object defaultValue )
	{
		Transaction tx = Transaction.begin();
		try
		{
			Object value = getUnderlyingNode().getProperty( key, defaultValue );
			tx.success();
			return value;
		}
		finally
		{
			tx.finish();
		}		
	}

}
