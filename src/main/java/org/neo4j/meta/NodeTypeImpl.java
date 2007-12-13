package org.neo4j.meta;

import java.util.Collection;
import java.util.Collections;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.ReturnableEvaluator;
import org.neo4j.api.core.StopEvaluator;
import org.neo4j.api.core.Transaction;
import org.neo4j.api.core.TraversalPosition;
import org.neo4j.api.core.Traverser;
import org.neo4j.meta.MetaWrapperRelationshipSet.AssociationLevel;

public final class NodeTypeImpl extends MetaNodeWrapper implements NodeType
{
	private static final String PROPERTY_KEY_VERSION = "version";
	private static final String PROPERTY_KEY_NAME = "name";
	private static final String PROPERTY_KEY_CAPTION = "neoshell.caption";
	private static final String PROPERTY_KEY_NUMBER_OF_INSTANCES = "count";
	
	public NodeTypeImpl( Node underlyingNode, MetaManager metaManager )
	{
		super( underlyingNode, metaManager );
	}

	public String getName()
	{
		return ( String ) getPropertyFromNode( PROPERTY_KEY_NAME );
	}
	
	void setName( String name )
	{
		Transaction tx = Transaction.begin();
		try
		{
			setPropertyOnNode( PROPERTY_KEY_NAME, name );
			setPropertyOnNode( PROPERTY_KEY_CAPTION, makeCaption( name ) );
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	private String makeCaption( String name )
	{
		return "Node type '" + name + "'";
	}

	public int getVersion()
	{
		return ( Integer ) getPropertyFromNode( PROPERTY_KEY_VERSION, 0 );
	}

	void setVersion( int newVersion )
	{
		setPropertyOnNode( PROPERTY_KEY_VERSION, newVersion );
	}
	
	public Collection<NodeType> directSuperTypes()
	{
		return new MetaWrapperRelationshipSet(
			getMetaManager(),
			getUnderlyingNode(),
			MetaRelTypes.META_NODE_TYPE_TO_SUPER_TYPE,
			Direction.OUTGOING,
			NodeTypeImpl.class,
			AssociationLevel.SOFT );
	}
	
	public Collection<NodeType> directSubTypes()
	{
		return new MetaWrapperRelationshipSet(
			getMetaManager(),
			getUnderlyingNode(),
			MetaRelTypes.META_NODE_TYPE_TO_SUPER_TYPE,
			Direction.INCOMING,
			NodeTypeImpl.class,		
			AssociationLevel.SOFT );
	}
	
	public boolean isSubTypeOf( final NodeType potentialSuperType )
	{
		Transaction tx = Transaction.begin();
		try
		{
			Traverser traverser = getUnderlyingNode().traverse(
			    Traverser.Order.BREADTH_FIRST,
			    StopEvaluator.END_OF_NETWORK,
			    new ReturnableEvaluator()
			    {
				    public boolean isReturnableNode(
				        TraversalPosition currentPosition )
				    {
					    return currentPosition.depth() > 0 &&
					    	currentPosition.currentNode().equals(
					        ( ( NodeTypeImpl ) potentialSuperType )
					            .getUnderlyingNode() );
				    }
			    }, MetaRelTypes.META_NODE_TYPE_TO_SUPER_TYPE,
			    Direction.OUTGOING );
			boolean result = traverser.iterator().hasNext(); 
			tx.success();
			return result;
		}
		finally
		{
			tx.finish();
		}
	}
	
	private Collection<MetaProperty> propertySet()
	{
		return new MetaWrapperRelationshipSet(
			getMetaManager(),
			getUnderlyingNode(),
			MetaRelTypes.META_NODE_TYPE_TO_PROPERTY,
			Direction.OUTGOING,
			MetaPropertyImpl.class,
			AssociationLevel.AGGREGATE );
	}
	
	private Collection<MetaRelationship> relationshipSet( Direction direction )
	{
		return new MetaWrapperRelationshipSet(
			getMetaManager(),
			getUnderlyingNode(),
			MetaRelTypes.META_NODE_TYPE_VIA_REL_TO_NODE_TYPE,
			direction,
			MetaRelationshipImpl.class,
			AssociationLevel.AGGREGATE );
	}

	public MetaProperty addRequiredProperty( String key )
	{
        Transaction tx = Transaction.begin();
		try
		{
			TypeConstraints.addProperty( this, key ).throwExceptionIfInvalid();
			MetaPropertyImpl newProperty = createNewProperty( key );
			propertySet().add( newProperty );
			tx.success();
			return newProperty;
		}
		finally
		{
			tx.finish();
		}		
	}
	
	private MetaPropertyImpl createNewProperty( String key )
	{
		MetaPropertyImpl newProperty = new MetaPropertyImpl(
			getMetaManager().getNeo().createNode(), getMetaManager() );
		newProperty.setKey( key );
		return newProperty;
	}
	
	public boolean removeRequiredProperty( MetaProperty property )
	{
		Transaction tx = Transaction.begin();
		try
		{
			boolean deleteSucceeded = propertySet().remove( property );
			tx.success();
			return deleteSucceeded;
		}
		finally
		{
			tx.finish();
		}
	}
	
	public MetaProperty getRequiredProperty( String key )
	{
		for ( MetaProperty property : getRequiredProperties() )
		{
			if ( property.getKey().equals( key ) )
			{
				return property;
			}
		}
		return null;
	}
	
	public Iterable<MetaProperty> getRequiredProperties()
	{
		return new MetaNodeWrapperTraverser<MetaProperty>(
			MetaPropertyImpl.class,	traverseSuperTypeHierarchy(
				MetaRelTypes.META_NODE_TYPE_TO_PROPERTY, Direction.OUTGOING ),
					getMetaManager() );
	}
	
	public Iterable<MetaProperty> getDirectRequiredProperties()
	{
		return Collections.unmodifiableCollection( propertySet() );
	}	

	public MetaRelationship addAllowedRelationship( String type,
		Direction direction, NodeType targetNodeType )
	{
		Transaction tx = Transaction.begin();
		try
		{
			TypeConstraints.addRelationship( this, type, direction ).
				throwExceptionIfInvalid();
			MetaRelationship newRelationship =
				createNewRelationship( type, targetNodeType );
			relationshipSet( direction ).add( newRelationship );
			return newRelationship;
		}
		finally
		{
			tx.finish();
		}
	}
	
	private MetaRelationship createNewRelationship( String type,
		NodeType targetNodeType )
	{
		MetaRelationshipImpl newRelationship = new MetaRelationshipImpl(
			getMetaManager().getNeo().createNode(), getMetaManager() );
		newRelationship.setNameOfType( type );
		newRelationship.setTargetNodeType( targetNodeType );
		return newRelationship;
	}

	public boolean removeAllowedRelationship( MetaRelationship relationship )
    {
		Transaction tx = Transaction.begin();
		try
		{
			boolean deleteSucceeded =
				relationshipSet( Direction.OUTGOING ).remove( relationship ) ||
				relationshipSet( Direction.INCOMING ).remove( relationship );
			tx.success();
			return deleteSucceeded;
		}
		finally
		{
			tx.finish();
		}
    }

	public Iterable<MetaRelationship> getDirectAllowedRelationships(
		Direction direction )
	{
		return Collections.unmodifiableCollection(
			relationshipSet( direction ) );
	}

	public MetaRelationship getAllowedRelationship( String type,
		Direction direction )
    {
		for ( MetaRelationship relationship :
			getAllowedRelationships( direction ) )
		{
			if ( relationship.getNameOfType().equals( type ) )
			{
				return relationship;
			}
		}
	    return null;
    }
	
	public Iterable<MetaRelationship> getAllowedRelationships(
		Direction direction )
	{
		return new MetaNodeWrapperTraverser<MetaRelationship>(
			MetaRelationshipImpl.class, traverseSuperTypeHierarchy(
				MetaRelTypes.META_NODE_TYPE_VIA_REL_TO_NODE_TYPE, direction ),
					getMetaManager() );
	}
	
	// Traverses the type hierarachy and returns either all property nodes
	// or all relationship nodes that are valid for this type
	private Traverser traverseSuperTypeHierarchy( final MetaRelTypes
		propertyOrRelationshipType, Direction direction )
	{
		return getUnderlyingNode().traverse(
		    Traverser.Order.BREADTH_FIRST,
		    new StopEvaluator()
		    {
			    public boolean isStopNode( TraversalPosition position )
			    {
				    return position.lastRelationshipTraversed() != null &&
				    	position.lastRelationshipTraversed().getType() ==
				    		propertyOrRelationshipType;
			    }
		    },
		    new ReturnableEvaluator()
		    {
			    public boolean isReturnableNode( TraversalPosition position )
			    {
				    return position.lastRelationshipTraversed() != null
				    	&& position.lastRelationshipTraversed().getType() ==
				        	propertyOrRelationshipType;
			    }
		    }, MetaRelTypes.META_NODE_TYPE_TO_SUPER_TYPE, Direction.OUTGOING,
		    propertyOrRelationshipType, direction );
	}

	@Override
    protected void cascadingDelete()
    {
        Transaction tx = Transaction.begin();
		try
		{
			// super/subtypes are ok to clear wildly: they have association
			// level SOFT so clear() will only delete the relationship and
			// leave the node types intact... as it should.
			directSuperTypes().clear();
			directSubTypes().clear();
			
			// property/relationship sets will invoke cascadingDelete() and
			// thus remove the underlying node and all that.
			propertySet().clear();
			relationshipSet( Direction.OUTGOING ).clear();
			relationshipSet( Direction.INCOMING ).clear();
			super.cascadingDelete();
			tx.success();
		}
		finally
		{
			tx.finish();
		}		
    }

	public void delete()
	{
		Transaction tx = Transaction.begin();
		try
		{
			getMetaManager().nodeTypeSet().remove( this );
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	private Relationship findInstanceRelationship( MetaInstance instance )
	{
		Node node = instance.getUnderlyingNode();
		for ( Relationship rel : node.getRelationships(
			MetaRelTypes.META_INSTANCE_OF, Direction.OUTGOING ) )
		{
			if ( rel.getOtherNode( node ).equals( getUnderlyingNode() ) )
			{
				return rel;
			}
		}
		return null;
	}
	
	public void addInstance( MetaInstance instance )
	{
		Transaction tx = Transaction.begin();
		try
		{
			if ( findInstanceRelationship( instance ) != null )
			{
				throw new IllegalArgumentException( instance +
					" already instance of " + this );
			}
			Node node = instance.getUnderlyingNode();
			node.createRelationshipTo( getUnderlyingNode(),
				MetaRelTypes.META_INSTANCE_OF );
			changeNumberOfInstances( 1 );
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	private void changeNumberOfInstances( int delta )
	{
		this.getMetaManager().getNeoUtil().getLockManager().getWriteLock(
			this.getUnderlyingNode() );
		try
		{
			int value = ( Integer ) getUnderlyingNode().getProperty(
				PROPERTY_KEY_NUMBER_OF_INSTANCES, 0 );
			value += delta;
			assert value >= 0;
			getUnderlyingNode().setProperty(
				PROPERTY_KEY_NUMBER_OF_INSTANCES, value );
		}
		finally
		{
			this.getMetaManager().getNeoUtil().getLockManager().
				releaseWriteLock( this.getUnderlyingNode() );
		}
	}
	
	public void removeInstance( MetaInstance instance )
	{
		Transaction tx = Transaction.begin();
		try
		{
			Relationship rel = findInstanceRelationship( instance );
			if ( rel == null )
			{
				throw new IllegalArgumentException( instance +
					" isn't instance of " + this );
			}
			rel.delete();
			changeNumberOfInstances( -1 );
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	public int getNumberOfInstances()
	{
		return ( Integer ) getMetaManager().getNeoUtil().getProperty(
			getUnderlyingNode(), PROPERTY_KEY_NUMBER_OF_INSTANCES, 0 );
	}
	
	public boolean hasInstance( MetaInstance instance )
	{
		Transaction tx = Transaction.begin();
		try
		{
			boolean result = findInstanceRelationship( instance ) != null;
			tx.success();
			return result;
		}
		finally
		{
			tx.finish();
		}
	}
	
	@Override
	public String toString()
	{
		return this.getName() + "[" + this.getUnderlyingNode() + "]";
	}
}