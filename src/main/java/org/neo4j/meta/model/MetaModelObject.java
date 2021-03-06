package org.neo4j.meta.model;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.index.IndexService;
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
	public static final String KEY_NAME = "meta_model_name";
	
	private MetaModel model;
	private Node node;
	
	MetaModelObject( MetaModel model, Node node )
	{
		this.model = model;
		this.node = node;
	}
	
	/**
	 * @return the underlying {@link MetaModel}.
	 */
	public MetaModel model()
	{
		return this.model;
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

	protected IndexService indexService()
	{
		return ( ( MetaModelImpl ) model() ).indexService();
	}

	
	/**
	 * @return the {@link Node} which this object wraps.
	 */
	public Node node()
	{
		return this.node;
	}
	
	protected void setOrRemoveProperty( String key, Object value )
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
		return IteratorUtil.singleValueOrNull(
		        node().getRelationships( type ).iterator() );
	}
	
	void setName( String name ) throws DuplicateNameException
	{
		node().setProperty( KEY_NAME, name );
		if(indexService().getSingleNode(KEY_NAME, name) != null){
			throw new DuplicateNameException();
		}else{
			indexService().index(node(), KEY_NAME, name);
		}
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
