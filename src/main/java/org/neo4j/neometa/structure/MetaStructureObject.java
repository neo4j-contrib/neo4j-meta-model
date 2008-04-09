package org.neo4j.neometa.structure;

import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.api.core.Transaction;

/**
 * A super class for basically all meta structure objects which wraps a
 * {@link Node}.
 */
public abstract class MetaStructureObject
{
	static final String KEY_MIN_CARDINALITY = "min_cardinality";
	static final String KEY_MAX_CARDINALITY = "max_cardinality";
	static final String KEY_COLLECTION_CLASS = "collection_class";
	static final String KEY_NAME = "name";
	
	private MetaStructure meta;
	private Node node;
	
	MetaStructureObject( MetaStructure meta, Node node )
	{
		this.meta = meta;
		this.node = node;
	}
	
	/**
	 * @return the underlying {@link MetaStructure}.
	 */
	public MetaStructure meta()
	{
		return this.meta;
	}
	
	/**
	 * @return the {@link NeoService} instance used with this instance.
	 */
	public NeoService neo()
	{
		return meta().neo();
	}
	
	/**
	 * @return the {@link Node} which this object wraps.
	 */
	public Node node()
	{
		return this.node;
	}
	
	protected void setProperty( String key, Object value )
	{
		meta().neoUtil().setProperty( node(), key, value );
	}
	
	protected Object getProperty( String key )
	{
		return meta().neoUtil().getProperty( node(), key );
	}

	protected Object getProperty( String key, Object defaultValue )
	{
		return meta().neoUtil().getProperty( node(), key, defaultValue );
	}
	
	protected Object removeProperty( String key )
	{
		return meta().neoUtil().removeProperty( node(), key );
	}
	
	protected void setOrRemoteProperty( String key, Object value )
	{
		if ( value == null )
		{
			removeProperty( key );
		}
		else
		{
			setProperty( key, value );
		}
	}
	
	protected void setSingleRelationshipOrNull( Node node,
		RelationshipType type )
	{
		Transaction tx = neo().beginTx();
		try
		{
			Relationship relationship = getSingleRelationshipOrNull( type );
			if ( relationship != null )
			{
				relationship.delete();
			}
			if ( node != null )
			{
				node().createRelationshipTo( node, type );
			}
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	protected Relationship getSingleRelationshipOrNull( RelationshipType type )
	{
		return meta().neoUtil().getSingleRelationship( node(), type );
	}
	
	void setName( String name )
	{
		setProperty( KEY_NAME, name );
	}
	
	/**
	 * @return the name set for this object.
	 */
	public String getName()
	{
		return ( String ) getProperty( KEY_NAME, null );
	}
	
	@Override
	public int hashCode()
	{
		return node().hashCode();
	}
	
	@Override
	public boolean equals( Object o )
	{
		return o != null && getClass().equals( o.getClass() ) && node().equals(
			( ( MetaStructureObject ) o ).node() );
	}
}
