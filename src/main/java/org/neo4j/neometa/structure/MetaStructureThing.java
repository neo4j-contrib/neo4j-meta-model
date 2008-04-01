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
	private static final String LABEL_KEY = "label";
	private static final String COMMENT_KEY = "comment";
	
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
	
	/**
	 * Sets the label of this thing.
	 * @param label the label.
	 */
	public void setLabel( String label )
	{
		setProperty( LABEL_KEY, label );
	}
	
	/**
	 * @return the label of this thing.
	 */
	public String getLabel()
	{
		return ( String ) getProperty( LABEL_KEY, null );
	}
	
	/**
	 * Sets the comment of this thing.
	 * @param comment
	 */
	public void setComment( String comment )
	{
		setProperty( COMMENT_KEY, comment );
	}
	
	/**
	 * @return the comment of this thing.
	 */
	public String getComment()
	{
		return ( String ) getProperty( COMMENT_KEY, null );
	}
}
