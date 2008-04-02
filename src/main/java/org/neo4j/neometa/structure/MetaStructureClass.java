package org.neo4j.neometa.structure;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.api.core.ReturnableEvaluator;
import org.neo4j.api.core.StopEvaluator;
import org.neo4j.api.core.Transaction;
import org.neo4j.api.core.TraversalPosition;
import org.neo4j.api.core.Traverser;

/**
 * Represents a class in the meta model.
 */
public class MetaStructureClass extends MetaStructureThing
{
	/**
	 * @param meta the {@link MetaStructure} instance.
	 * @param node the {@link Node} to wrap.
	 */
	public MetaStructureClass( MetaStructure meta, Node node )
	{
		super( meta, node );
	}
	
	private Collection<MetaStructureClass> hierarchyCollection(
		Direction direction )
	{
		return new MetaStructureObjectCollection<MetaStructureClass>( node(),
			MetaStructureRelTypes.META_IS_SUBCLASS_OF, direction, meta(),
			MetaStructureClass.class );
	}
	
	@Override
	public Collection<MetaStructureClass> getDirectSubs()
	{
		return hierarchyCollection( Direction.INCOMING );
	}
	
	@Override
	public Collection<MetaStructureClass> getDirectSupers()
	{
		return hierarchyCollection( Direction.OUTGOING );
	}
	
	@Override
	protected RelationshipType subRelationshipType()
	{
		return MetaStructureRelTypes.META_IS_SUBCLASS_OF;
	}
	
	/**
	 * @return a modifiable collection of properties directly related to
	 * this class.
	 */
	public Collection<MetaStructureProperty> getDirectProperties()
	{
		return new MetaStructureObjectCollection<MetaStructureProperty>( node(),
			MetaStructureRelTypes.META_CLASS_HAS_PROPERTY,
			Direction.OUTGOING, meta(), MetaStructureProperty.class );
	}
	
	/**
	 * @return an unmodifiable collection of all properties related to this
	 * class.
	 */
	public Collection<MetaStructureProperty> getAllProperties()
	{
		Transaction tx = neo().beginTx();
		try
		{
			HashSet<MetaStructureProperty> properties =
				new HashSet<MetaStructureProperty>();
			for ( Node node : node().traverse( Traverser.Order.BREADTH_FIRST,
				StopEvaluator.END_OF_NETWORK,
				
				// Maybe remove these three lines? They go for subproperties too
				new AllPropertiesRE(),
				MetaStructureRelTypes.META_IS_SUBPROPERTY_OF,
					Direction.INCOMING,
					
				MetaStructureRelTypes.META_CLASS_HAS_PROPERTY,
					Direction.OUTGOING,
				MetaStructureRelTypes.META_IS_SUBCLASS_OF,
					Direction.OUTGOING ) )
			{
				properties.add( new MetaStructureProperty( meta(), node ) );
			}
			return Collections.unmodifiableSet( properties );
		}
		finally
		{
			tx.finish();
		}
	}
	
	/**
	 * @return a modifiable collection of instances of this class.
	 */
	public Collection<Node> getInstances()
	{
		return new MetaStructureInstanceCollection( node(), meta() );
	}
	
	private class AllPropertiesRE implements ReturnableEvaluator
	{
		private boolean same( RelationshipType r1,
			RelationshipType r2 )
		{
			return r1.name().equals( r2.name() );
		}
		
		public boolean isReturnableNode( TraversalPosition currentPos )
		{
			Relationship lastRel =
				currentPos.lastRelationshipTraversed();
			if ( lastRel == null || same( lastRel.getType(),
				MetaStructureRelTypes.META_IS_SUBCLASS_OF ) )
			{
				return false;
			}
			if ( same( lastRel.getType(),
				MetaStructureRelTypes.META_IS_SUBPROPERTY_OF ) )
			{
				if ( currentPos.currentNode().hasRelationship(
					MetaStructureRelTypes.META_CLASS_HAS_PROPERTY ) )
				{
					return false;
				}
			}
			return true;
		}
	}
}
