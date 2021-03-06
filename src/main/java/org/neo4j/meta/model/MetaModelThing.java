package org.neo4j.meta.model;

import java.util.Collection;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.Traverser;

// TODO	ContainerMempership:
//		http://www.w3.org/TR/rdf-schema/#ch_containermembershipproperty?
// 		
//		rdfs:seeAlso
//		rdfs:idDefinedBy

/**
 * The super class of {@link MetaModelPropertyContainer} and
 * {@link MetaModelProperty}. It contains hierarchical functionality.
 */
public abstract class MetaModelThing extends MetaModelObject
{
	MetaModelThing( MetaModel model, Node node )
	{
		super( model, node );
	}
	
	/**
	 * (non-recursive) sub "things" (class or property).
	 */
	public abstract Collection<? extends MetaModelThing> getDirectSubs();
	
	/**
	 * @return a modifiable {@link Collection} of directly connected
	 * (non-recursive) super "things" (class or property).
	 */
	public abstract Collection<? extends MetaModelThing> getDirectSupers();
	
	protected abstract RelationshipType subRelationshipType();
	
	/**
	 * @param <T> the type of {@link MetaModelThing}, should match
	 * the type of this instance.
	 * @param thing the {@link MetaModelThing} to check against.
	 * @return {@code true} if this thing is a sub (class or property) of
	 * {@code thing}.
	 */
	public <T extends MetaModelThing> boolean isSubOf( T thing )
	{
		Transaction tx = graphDb().beginTx();
		try
		{
			boolean found = false;
			Node target = thing.node();
			for ( Node node : node().traverse( Traverser.Order.BREADTH_FIRST,
				StopEvaluator.END_OF_GRAPH, ReturnableEvaluator.ALL,
				subRelationshipType(), Direction.OUTGOING ) )
			{
				if ( node.equals( target ) )
				{
					found = true;
					break;
				}
			}
			tx.success();
			return found;
		}
		finally
		{
			tx.finish();
		}
	}
	
	private String additionalPropertyKey( String key )
	{
		return "additional." + key;
	}
	
	/**
	 * Sets an additional property for this thing, f.ex. a label or a comment.
	 * @param key the property key.
	 * @param value the property value.
	 */
	public void setAdditionalProperty( String key, Object value )
	{
		node().setProperty( additionalPropertyKey( key ), value );
	}
	
	/**
	 * Removes an additional property from this thing, f.ex. a label or
	 * a comment.
	 * @param key the property key.
	 */
	public void removeAdditionalProperty( String key )
	{
		node().removeProperty( additionalPropertyKey( key ) );
	}

	/**
	 * Returns an additional property from this thing, f.ex. a label or
	 * a comment.
	 * @param key the property key.
	 * @return the property value for property {@code key} or
	 * {@code null} if no value exists for {@code key}.
	 */
	public Object getAdditionalProperty( String key )
	{
		return ( String ) node().getProperty(
		    additionalPropertyKey( key ), null );
	}
	
	public Object[] getAdditionalProperties( String key )
	{
	    Object value = getAdditionalProperty( key );
	    return value != new Object[ 0 ] ?
	        this.graphDbUtil().propertyValueAsArray( key ) : null;
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + getName() + "]";
	}
}
