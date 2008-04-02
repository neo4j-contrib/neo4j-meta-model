package org.neo4j.neometa.structure;

import java.util.Collection;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.api.core.ReturnableEvaluator;
import org.neo4j.api.core.StopEvaluator;
import org.neo4j.api.core.Transaction;
import org.neo4j.api.core.Traverser;

// TODO	ContainerMempership:
//		http://www.w3.org/TR/rdf-schema/#ch_containermembershipproperty?
// 		
//		rdfs:seeAlso
//		rdfs:idDefinedBy

/**
 * The super class of {@link MetaStructureClass} and
 * {@link MetaStructureProperty}. It contains hierarchial functionality.
 */
public abstract class MetaStructureThing extends MetaStructureObject
{
	MetaStructureThing( MetaStructure meta, Node node )
	{
		super( meta, node );
	}
	
	/**
	 * @return a modifiable {@link Collection} of directly connected
	 * (non-recursive) sub "things" (class or property).
	 */
	public abstract Collection<? extends MetaStructureThing> getDirectSubs();
	
	/**
	 * @return a modifiable {@link Collection} of directly connected
	 * (non-recursive) super "things" (class or property).
	 */
	public abstract Collection<? extends MetaStructureThing> getDirectSupers();
	
	protected abstract RelationshipType subRelationshipType();
	
	/**
	 * @param <T> the type of {@link MetaStructureThing}, should match
	 * the type of this instance.
	 * @param thing the {@link MetaStructureThing} to check against.
	 * @return {@code true} if this thing is a sub (class or property) of
	 * {@code thing}.
	 */
	public <T extends MetaStructureThing> boolean isSubOf( T thing )
	{
		Transaction tx = neo().beginTx();
		try
		{
			boolean found = false;
			Node target = thing.node();
			for ( Node node : node().traverse( Traverser.Order.BREADTH_FIRST,
				StopEvaluator.END_OF_NETWORK, ReturnableEvaluator.ALL,
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
	public void setAdditionalProperty( String key, String value )
	{
		setProperty( additionalPropertyKey( key ), value );
	}
	
	/**
	 * Removes an additional property from this thing, f.ex. a label or
	 * a comment.
	 * @param key the property key.
	 */
	public void removeAdditionalProperty( String key )
	{
		removeProperty( additionalPropertyKey( key ) );
	}

	/**
	 * Returns an additional property from this thing, f.ex. a label or
	 * a comment.
	 * @param key the property key.
	 * @return the property value for property {@code key} or
	 * {@code null} if no value exists for {@code key}.
	 */
	public String getAdditionalProperty( String key )
	{
		return ( String ) getProperty( additionalPropertyKey( key ), null );
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + getName() + "]";
	}
}
