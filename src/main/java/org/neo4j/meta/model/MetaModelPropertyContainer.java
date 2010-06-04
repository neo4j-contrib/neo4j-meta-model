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
 * The super class of {@link MetaModelRelationship} and
 * {@link MetaModelClass}. It contains hierarchical functionality.
 */

public abstract class MetaModelPropertyContainer extends MetaModelThing{

	MetaModelPropertyContainer( MetaModel model, Node node )
	{
		super( model, node );
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
				MetaModelRelTypes.META_IS_SUBRELATIONSHIP_OF ) || same( lastRel.getType(),
						MetaModelRelTypes.META_IS_SUBCLASS_OF ) )
			{
				return false;
			}
			if ( same( lastRel.getType(),
				MetaModelRelTypes.META_IS_SUBPROPERTY_OF ) )
			{
				if ( currentPos.currentNode().hasRelationship(
					MetaModelRelTypes.META_HAS_PROPERTY ) )
				{
					return false;
				}
			}
			return true;
		}
	}
	/**
	 * @return a modifiable collection of properties directly related to
	 * this class.
	 */
	public Collection<MetaModelProperty> getDirectProperties()
	{
		return new ObjectCollection<MetaModelProperty>(
			node(), MetaModelRelTypes.META_HAS_PROPERTY,
			Direction.OUTGOING, model(), MetaModelProperty.class );
	}
	
	/**
	 * @return an unmodifiable collection of all properties related to this
	 * relationshiptype.
	 */
	public Collection<MetaModelProperty> getAllProperties()
	{
		Transaction tx = graphDb().beginTx();
		try
		{
			HashSet<MetaModelProperty> properties =
				new HashSet<MetaModelProperty>();
			for ( Node node : node().traverse( Traverser.Order.BREADTH_FIRST,
				StopEvaluator.END_OF_GRAPH,
				
				// Maybe remove these four lines? They go for subproperties too
				new AllPropertiesRE(),
				MetaModelRelTypes.META_IS_SUBPROPERTY_OF,
					Direction.INCOMING,
				MetaModelRelTypes.META_HAS_PROPERTY,
					Direction.OUTGOING,
				MetaModelRelTypes.META_IS_SUBRELATIONSHIP_OF,
					Direction.OUTGOING,
				MetaModelRelTypes.META_IS_SUBCLASS_OF,
					Direction.OUTGOING ) )
			{
				properties.add( new MetaModelProperty( model(), node ) );
			}
			return Collections.unmodifiableSet( properties );
		}
		finally
		{
			tx.finish();
		}
	}
	
	/**
	 * @return the restrictions for this relationshiptype.
	 */
	public Collection<MetaModelPropertyRestriction> getDirectPropertyRestrictions()
	{
		return new ObjectCollection<MetaModelPropertyRestriction>(
			node(), MetaModelRelTypes.META_PROPERTY_RESTRICTION_TO_PROPERTYCONTAINER,
			Direction.INCOMING, model(), MetaModelPropertyRestriction.class );
	}
	
	
	/**
	 * @param property the {@link MetaModelProperty} to associate with.
	 * @param allowCreate whether to allow creation of the restriction if
	 * it doesn't exist.
	 * @return the restriction for {@code property} or creates a new if
	 * {@code allowCreate} is {@code true}.
	 */
	public MetaModelPropertyRestriction getRestriction(
		MetaModelProperty property, boolean allowCreate )
	{
		Transaction tx = graphDb().beginTx();
		try
		{
			Collection<MetaModelPropertyRestriction> restrictions =
				getDirectPropertyRestrictions();
			for ( MetaModelPropertyRestriction restriction : restrictions )
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
			
//			if ( !getAllProperties().contains( property ) )
//			{
//				throw new RuntimeException( this + " isn't in the domain of " +
//					property + " add it first" );
//			}
			Node node = graphDb().createNode();
			MetaModelPropertyRestriction result = new MetaModelPropertyRestriction(
				model(), node );
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
	
	public Collection<MetaModelPropertyRestriction> getAllPropertyRestrictions()
	{
		Transaction tx = graphDb().beginTx();
		try
		{
			HashSet<MetaModelPropertyRestriction> restrictions =
				new HashSet<MetaModelPropertyRestriction>();
			for ( Node node : node().traverse( Traverser.Order.BREADTH_FIRST,
				StopEvaluator.END_OF_GRAPH,
				new OneOfRelTypesReturnableEvaluator(
					MetaModelRelTypes.META_PROPERTY_RESTRICTION_TO_PROPERTYCONTAINER ),
				MetaModelRelTypes.META_PROPERTY_RESTRICTION_TO_PROPERTYCONTAINER,
					Direction.INCOMING,
				MetaModelRelTypes.META_IS_SUBCLASS_OF,
					Direction.OUTGOING,
				MetaModelRelTypes.META_IS_SUBRELATIONSHIP_OF,
					Direction.OUTGOING ))
			{
				restrictions.add(
					new MetaModelPropertyRestriction( model(), node ) );
			}
			return Collections.unmodifiableSet( restrictions );
		}
		finally
		{
			tx.finish();
		}
	}
	
	public void setCollectionBehaviourClass(
		Class<? extends Collection> collectionClassOrNull )
	{
		setOrRemoveProperty( KEY_COLLECTION_CLASS,
			collectionClassOrNull == null ? null :
			collectionClassOrNull.getName() );
	}
			
	public Class<? extends Collection<?>> getCollectionBehaviourClass()
	{
		try
		{
			String className = ( String ) node().getProperty(
			    KEY_COLLECTION_CLASS, null );
			// Yep generics warning, but what're you going to do?
			return className == null ? null :
				( Class<? extends Collection<?>> ) Class.forName( className ); 
		}
		catch ( Exception e )
		{
			throw new RuntimeException( e );
		}
	}

}
