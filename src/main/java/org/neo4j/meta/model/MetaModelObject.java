package org.neo4j.meta.model;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.util.GraphDatabaseUtil;

/**
 * A super class for basically all meta structure objects which wraps a
 * {@link Node}.
 */
public abstract class MetaModelObject
{
	public static final String KEY_MIN_CARDINALITY = "min_cardinality";
	public static final String KEY_MAX_CARDINALITY = "max_cardinality";
	public static final String KEY_COLLECTION_CLASS = "collection_class";
	public static final String KEY_FUNCTIONALITY = "functionality";
	
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
	public MetaModel model()
	{
		return this.meta;
	}
	
	/**
	 * @return the {@link GraphDatabaseService} instance used with this instance.
	 */
	public GraphDatabaseService graphDb()
	{
		return ( ( MetaModelImpl ) model() ).graphDb();
	}
	
	protected GraphDatabaseUtil graphDbUtil()
	{
		return ( ( MetaModelImpl ) model() ).graphDbUtil();
	}
	
	/**
	 * @return the {@link Node} which this object wraps.
	 */
	public Node node()
	{
		return this.node;
	}
	
	protected void setOrRemoteProperty( String key, Object value )
	{
		if ( value == null )
		{
			node().removeProperty( key );
		}
		else
		{
			node().setProperty( key, value );
		}
	}
	
	protected void setSingleRelationshipOrNull( Node node,
		RelationshipType type )
	{
		Transaction tx = graphDb().beginTx();
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
		return graphDbUtil().getSingleRelationship( node(), type );
	}
	
	void setName( String name )
	{
		node().setProperty( KEY_NAME, name );
	}
	
	/**
	 * @return the name set for this object.
	 */
	public String getName()
	{
		return ( String ) node().getProperty( KEY_NAME, null );
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
