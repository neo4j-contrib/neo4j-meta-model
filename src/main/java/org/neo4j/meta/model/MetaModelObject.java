package org.neo4j.meta.model;

import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.api.core.Transaction;
import org.neo4j.util.NeoUtil;

/**
 * A super class for basically all meta structure objects which wraps a
 * {@link Node}.
 */
public abstract class MetaModelObject
{
	static final String KEY_MIN_CARDINALITY = "min_cardinality";
	static final String KEY_MAX_CARDINALITY = "max_cardinality";
	static final String KEY_COLLECTION_CLASS = "collection_class";
	
	/**
	 * The node property for a meta structure objects name.
	 */
	public static final String KEY_NAME = "name";
	
	private MetaModel meta;
	private Node node;
	
	MetaModelObject( MetaModel meta, Node node )
	{
		this.meta = meta;
		this.node = node;
	}
	
	/**
	 * @return the underlying {@link MetaModel}.
	 */
	public MetaModel meta()
	{
		return this.meta;
	}
	
	/**
	 * @return the {@link NeoService} instance used with this instance.
	 */
	public NeoService neo()
	{
		return ( ( MetaModelImpl ) meta() ).neo();
	}
	
	protected NeoUtil neoUtil()
	{
		return ( ( MetaModelImpl ) meta() ).neoUtil();
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
		neoUtil().setProperty( node(), key, value );
	}
	
	protected Object getProperty( String key )
	{
		return neoUtil().getProperty( node(), key );
	}

	protected Object getProperty( String key, Object defaultValue )
	{
		return neoUtil().getProperty( node(), key, defaultValue );
	}
	
	protected Object removeProperty( String key )
	{
		return neoUtil().removeProperty( node(), key );
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
		return neoUtil().getSingleRelationship( node(), type );
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
			( ( MetaModelObject ) o ).node() );
	}
}
