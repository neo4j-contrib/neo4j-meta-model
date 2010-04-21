package org.neo4j.meta.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.TraversalPosition;
import org.neo4j.graphdb.Traverser;
import org.neo4j.util.OneOfRelTypesReturnableEvaluator;

/**
 * Represents a class in the meta model.
 */
public class MetaModelClass extends MetaModelPropertyContainer implements MetaModelRestrictable<InstanceRange>
{
	/**
	 * @param model the {@link MetaModel} instance.
	 * @param node the {@link Node} to wrap.
	 */
	public MetaModelClass( MetaModel model, Node node )
	{
		super( model, node );
	}
	
	private Collection<MetaModelClass> hierarchyCollection(
		Direction direction )
	{
		return new ObjectCollection<MetaModelClass>( graphDb(),
			node(), MetaModelRelTypes.META_IS_SUBCLASS_OF, direction,
			model(), MetaModelClass.class );
	}
	
	@Override
	public Collection<MetaModelClass> getDirectSubs()
	{
		return hierarchyCollection( Direction.INCOMING );
	}
	
	@Override
	public Collection<MetaModelClass> getDirectSupers()
	{
		return hierarchyCollection( Direction.OUTGOING );
	}
	
	@Override
	protected RelationshipType subRelationshipType()
	{
		return MetaModelRelTypes.META_IS_SUBCLASS_OF;
	}
	
	/**
	 * @return a modifiable collection of RelationshipTypes directly related to
	 * this class.
	 */
	public Collection<MetaModelRelationship> getDirectRelationshipTypes()
	{
		return new ObjectCollection<MetaModelRelationship>( graphDb(),
			node(), MetaModelRelTypes.META_CLASS_HAS_RELATIONSHIP,
			Direction.OUTGOING, model(), MetaModelRelationship.class );
	}
	
	/**
	 * @return an unmodifiable collection of all relationships related to this
	 * class.
	 */
	public Collection<MetaModelRelationship> getAllRelationships()
	{
		Transaction tx = graphDb().beginTx();
		try
		{
			HashSet<MetaModelRelationship> relationshipTypes =
				new HashSet<MetaModelRelationship>();
			for ( Node node : node().traverse( Traverser.Order.BREADTH_FIRST,
				StopEvaluator.END_OF_GRAPH,
				
				// Maybe remove these three lines? They go for subrelationships too
				new AllRelationshipTypesRE(),
				MetaModelRelTypes.META_IS_SUBRELATIONSHIP_OF,
					Direction.INCOMING,
					
				MetaModelRelTypes.META_CLASS_HAS_RELATIONSHIP,
					Direction.OUTGOING,
				MetaModelRelTypes.META_IS_SUBCLASS_OF,
					Direction.OUTGOING ) )
			{
				relationshipTypes.add( new MetaModelRelationship( model(), node ) );
			}
			return Collections.unmodifiableSet( relationshipTypes );
		}
		finally
		{
			tx.finish();
		}
	}
	

	/**
	 * @param relationshipType the {@link MetaModelRelationship} to associate with.
	 * @param allowCreate whether to allow creation of the restriction if
	 * it doesn't exist.
	 * @return the restriction for {@code Relationship} or creates a new if
	 * {@code allowCreate} is {@code true}.
	 */
	public MetaModelRelationshipRestriction getRestriction(
		MetaModelRelationship relationshipType, boolean allowCreate )
	{
		Transaction tx = graphDb().beginTx();
		try
		{
			Collection<MetaModelRelationshipRestriction> restrictions =
				getDirectRelationshipTypeRestrictions();
			for ( MetaModelRelationshipRestriction restriction : restrictions )
			{
				if ( restriction.getMetaRelationshipType().equals( relationshipType ) )
				{
					return restriction;
				}
			}
			if ( !allowCreate )
			{
				return null;
			}
			
			Node node = graphDb().createNode();
			MetaModelRelationshipRestriction result = new MetaModelRelationshipRestriction(
				model(), node );
			restrictions.add( result );
			node.createRelationshipTo( relationshipType.node(),
				MetaModelRelTypes.META_RESTRICTION_TO_RELATIONSHIP );
			tx.success();
			return result;
		}
		finally
		{
			tx.finish();
		}
	}
	
	
	/**
	 * @return the restrictions for this class.
	 */
	public Collection<MetaModelRelationshipRestriction> getDirectRelationshipTypeRestrictions()
	{
		return new ObjectCollection<MetaModelRelationshipRestriction>(
			graphDb(), node(), MetaModelRelTypes.META_RELATIONSHIP_RESTRICTION_TO_CLASS,
			Direction.INCOMING, model(), MetaModelRelationshipRestriction.class );
	}
	
	/**
	 * @return an unmodifiable collection of all direct restrictions as well
	 * as restrictions for super classes.
	 */

	/**
	 * @return an unmodifiable collection of all direct restrictions as well
	 * as restrictions for super classes.
	 */
	public Collection<MetaModelRelationshipRestriction> getAllRelationshipTypeRestrictions()
	{
		Transaction tx = graphDb().beginTx();
		try
		{
			HashSet<MetaModelRelationshipRestriction> restrictions =
				new HashSet<MetaModelRelationshipRestriction>();
			for ( Node node : node().traverse( Traverser.Order.BREADTH_FIRST,
				StopEvaluator.END_OF_GRAPH,
				new OneOfRelTypesReturnableEvaluator(
					MetaModelRelTypes.META_RELATIONSHIP_RESTRICTION_TO_CLASS ),
				MetaModelRelTypes.META_RELATIONSHIP_RESTRICTION_TO_CLASS,
					Direction.INCOMING,
				MetaModelRelTypes.META_IS_SUBCLASS_OF,
					Direction.OUTGOING ) )
			{
				restrictions.add(
					new MetaModelRelationshipRestriction( model(), node ) );
			}
			return Collections.unmodifiableSet( restrictions );
		}
		finally
		{
			tx.finish();
		}
	}
	
	
	/**
	 * @return a modifiable collection of instances of this class.
	 */
	public Collection<Node> getDirectInstances()
	{
		return new InstanceCollection( graphDb(), node(), model() );
	}
	
	/**
	 * @return all instances of this class, including instances of subclasses
	 */
	public Iterable<Node> getAllInstances()
	{
	    return new RecursiveInstanceTraverser( graphDb(), node(), model() );
	}

	public void setMinCardinality( Integer cardinalityOrNull )
	{
		setOrRemoveProperty( KEY_MIN_CARDINALITY, cardinalityOrNull );
	}
	
	public Integer getMinCardinality()
	{
		return ( Integer ) node().getProperty( KEY_MIN_CARDINALITY, null );
	}
	
	public void setMaxCardinality( Integer cardinalityOrNull )
	{
		setOrRemoveProperty( KEY_MAX_CARDINALITY, cardinalityOrNull );
	}
	
	public Integer getMaxCardinality()
	{
	    return ( Integer ) node().getProperty( KEY_MAX_CARDINALITY, null );
	}
	
	public void setCardinality( Integer cardinality )
	{
		Transaction tx = graphDb().beginTx();
		try
		{
			setMinCardinality( cardinality );
			setMaxCardinality( cardinality );
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}

	public void setRange( InstanceRange range )
	{
		InstanceRange.setOrRemoveRange( this, range );
	}
	
	public InstanceRange getRange()
	{
		return InstanceRange.loadRange( this );
	}
	
	
	private class AllRelationshipTypesRE implements ReturnableEvaluator
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
				MetaModelRelTypes.META_IS_SUBCLASS_OF ) )
			{
				return false;
			}
			if ( same( lastRel.getType(),
				MetaModelRelTypes.META_IS_SUBRELATIONSHIP_OF ) )
			{
				if ( currentPos.currentNode().hasRelationship(
					MetaModelRelTypes.META_CLASS_HAS_RELATIONSHIP ) )
				{
					return false;
				}
			}
			return true;
		}
	}

}
