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
public abstract class MetaStructureThing extends MetaStructureObject
{
	private static final String LABEL_KEY = "label";
	private static final String COMMENT_KEY = "comment";
	
	MetaStructureThing( MetaStructure meta, Node node )
	{
		super( meta, node );
	}
	
	public abstract Collection<? extends MetaStructureThing> getDirectSubs();
	
	public abstract Collection<? extends MetaStructureThing> getDirectSupers();
	
	protected abstract RelationshipType subRelationshipType();
	
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
	
	public void setLabel( String label )
	{
		setProperty( LABEL_KEY, label );
	}
	
	public String getLabel()
	{
		return ( String ) getProperty( LABEL_KEY, null );
	}
	
	public void setComment( String comment )
	{
		setProperty( COMMENT_KEY, comment );
	}
	
	public String getComment()
	{
		return ( String ) getProperty( COMMENT_KEY, null );
	}
}
