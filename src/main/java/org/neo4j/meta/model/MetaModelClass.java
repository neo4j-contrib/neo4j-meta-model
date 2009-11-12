package org.neo4j.meta.model;

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
import org.neo4j.util.OneOfRelTypesReturnableEvaluator;

/**
 * Represents a class in the meta model.
 */
public class MetaModelClass extends MetaModelThing
{
	/**
	 * @param meta the {@link MetaModel} instance.
	 * @param node the {@link Node} to wrap.
	 */
	public MetaModelClass( MetaModel meta, Node node )
	{
		super( meta, node );
	}
	
	private Collection<MetaModelClass> hierarchyCollection(
		Direction direction )
	{
		return new ObjectCollection<MetaModelClass>( neo(),
			node(), MetaModelRelTypes.META_IS_SUBCLASS_OF, direction,
			meta(), MetaModelClass.class );
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
	 * @return a modifiable collection of properties directly related to
	 * this class.
	 */
	public Collection<MetaModelProperty> getDirectProperties()
	{
		return new ObjectCollection<MetaModelProperty>( neo(),
			node(), MetaModelRelTypes.META_CLASS_HAS_PROPERTY,
			Direction.OUTGOING, meta(), MetaModelProperty.class );
	}
	
	/**
	 * @return an unmodifiable collection of all properties related to this
	 * class.
	 */
	public Collection<MetaModelProperty> getAllProperties()
	{
		Transaction tx = neo().beginTx();
		try
		{
			HashSet<MetaModelProperty> properties =
				new HashSet<MetaModelProperty>();
			for ( Node node : node().traverse( Traverser.Order.BREADTH_FIRST,
				StopEvaluator.END_OF_NETWORK,
				
				// Maybe remove these three lines? They go for subproperties too
				new AllPropertiesRE(),
				MetaModelRelTypes.META_IS_SUBPROPERTY_OF,
					Direction.INCOMING,
					
				MetaModelRelTypes.META_CLASS_HAS_PROPERTY,
					Direction.OUTGOING,
				MetaModelRelTypes.META_IS_SUBCLASS_OF,
					Direction.OUTGOING ) )
			{
				properties.add( new MetaModelProperty( meta(), node ) );
			}
			return Collections.unmodifiableSet( properties );
		}
		finally
		{
			tx.finish();
		}
	}
	
	/**
	 * @param property the {@link MetaModelProperty} to associate with.
	 * @param allowCreate wether to allow creation of the restriction if
	 * it doesn't exist.
	 * @return the restriction for {@code property} or creates a new if
	 * {@code allowCreate} is {@code true}.
	 */
	public MetaModelRestriction getRestriction(
		MetaModelProperty property, boolean allowCreate )
	{
		Transaction tx = neo().beginTx();
		try
		{
			Collection<MetaModelRestriction> restrictions =
				getDirectRestrictions();
			for ( MetaModelRestriction restriction : restrictions )
			{
				if ( restriction.getMetaProperty().equals( property ) )
				{
					return restriction;
				}
			}
			if ( !allowCreate )
			{
				return null;
			}
			
			if ( !getAllProperties().contains( property ) )
			{
				throw new RuntimeException( this + " isn't in the domain of " +
					property + " add it first" );
			}
			Node node = neo().createNode();
			MetaModelRestriction result = new MetaModelRestriction(
				meta(), node );
			restrictions.add( result );
			node.createRelationshipTo( property.node(),
				MetaModelRelTypes.META_RESTRICTION_TO_PROPERTY );
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
	public Collection<MetaModelRestriction> getDirectRestrictions()
	{
		return new ObjectCollection<MetaModelRestriction>(
			neo(), node(), MetaModelRelTypes.META_RESTRICTION_TO_CLASS,
			Direction.INCOMING, meta(), MetaModelRestriction.class );
	}
	
	/**
	 * @return an unmodifiable collection of all direct restrictions as well
	 * as restrictions for super classes.
	 */
	public Collection<MetaModelRestriction> getAllRestrictions()
	{
		Transaction tx = neo().beginTx();
		try
		{
			HashSet<MetaModelRestriction> restrictions =
				new HashSet<MetaModelRestriction>();
			for ( Node node : node().traverse( Traverser.Order.BREADTH_FIRST,
				StopEvaluator.END_OF_NETWORK,
				new OneOfRelTypesReturnableEvaluator(
					MetaModelRelTypes.META_RESTRICTION_TO_CLASS ),
				MetaModelRelTypes.META_RESTRICTION_TO_CLASS,
					Direction.INCOMING,
				MetaModelRelTypes.META_IS_SUBCLASS_OF,
					Direction.OUTGOING ) )
			{
				restrictions.add(
					new MetaModelRestriction( meta(), node ) );
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
	public Collection<Node> getInstances()
	{
		return new InstanceCollection( neo(), node(), meta() );
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
				MetaModelRelTypes.META_IS_SUBCLASS_OF ) )
			{
				return false;
			}
			if ( same( lastRel.getType(),
				MetaModelRelTypes.META_IS_SUBPROPERTY_OF ) )
			{
				if ( currentPos.currentNode().hasRelationship(
					MetaModelRelTypes.META_CLASS_HAS_PROPERTY ) )
				{
					return false;
				}
			}
			return true;
		}
	}
}
