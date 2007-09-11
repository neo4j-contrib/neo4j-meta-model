package org.neo4j.meta;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.ReturnableEvaluator;
import org.neo4j.api.core.StopEvaluator;
import org.neo4j.api.core.Transaction;
import org.neo4j.api.core.Traverser;

class TypeConstraints
{
	private static final TypeConstraints OPERATION_OK_GENERIC_MESSAGE =
		new TypeConstraints( true, "Type constraints ok" );
	private static final TypeConstraints OPERATION_VIOLATED_GENERIC_MESSAGE =
		new TypeConstraints( false, "The operation cannot be completed " +
			"because it would result in invalid type constraints" );
	
	private boolean valid;
	private String message;
	
	private TypeConstraints( boolean valid, String message )
	{
		this.valid = valid;
		this.message = message;
	}
	
	boolean isValid()
	{
		return this.valid;
	}
	
	String getMessage()
	{
		return this.message;
	}
	
	void throwExceptionIfInvalid() 
	{
		if ( !isValid() )
		{
			throw new IllegalArgumentException( getMessage() );
		}
	}
	
	private static Iterable<NodeType> getTransitiveSubTypes( NodeType type )
	{
		NodeTypeImpl typeImpl = ( NodeTypeImpl ) type;
		return new MetaNodeWrapperTraverser<NodeType>(
			NodeTypeImpl.class,
			typeImpl.getUnderlyingNode().traverse(
				Traverser.Order.BREADTH_FIRST,
				StopEvaluator.END_OF_NETWORK,
				ReturnableEvaluator.ALL_BUT_START_NODE,
				MetaRelTypes.META_NODE_TYPE_TO_SUPER_TYPE,
				Direction.INCOMING ),
			typeImpl.getMetaManager() );
	}
	
	static TypeConstraints addProperty( NodeType type, String propertyKey )
	{
		// First check if we have a property with the same key in the transitive
		// property set, i.e., the type's direct properties and the properties
		// of its super types. If it's there, then reject.
		if ( type.getRequiredProperty( propertyKey ) != null )
		{
			return TypeConstraints.OPERATION_VIOLATED_GENERIC_MESSAGE;
		}
		
		// Then check if any of our transitive sub types have a property with
		// the same key. If so, reject as well.
		for ( NodeType subType : getTransitiveSubTypes( type ) )
		{
			for ( MetaProperty property :
				subType.getDirectRequiredProperties() )
			{
				if ( property.getKey().equals( propertyKey ) )
				{
					return TypeConstraints.OPERATION_VIOLATED_GENERIC_MESSAGE;
				}
			}
		}
		
		return TypeConstraints.OPERATION_OK_GENERIC_MESSAGE;
	}
	
	static TypeConstraints addRelationship( NodeType nodeType, String
		relationshipTypeName, Direction direction )
	{
		// First check the transitive closure of allowed relationships
		// (includes super types)
		if ( nodeType.getAllowedRelationship( relationshipTypeName,
			direction ) != null )
		{
			return new TypeConstraints( false, "Adding an allowed " +
				"relationship with name '" + relationshipTypeName + "' to " +
				"node type '" + nodeType.getName() + "' would violate " +
				"type constraints since the node type or one of its " +
				"transitive super types already has a relationship of the " +
				"same type and direction" );
		}
		
		// Then check if adding it would violate a subtype
		for ( NodeType subType : getTransitiveSubTypes( nodeType ) )
		{
			for ( MetaRelationship relationship :
				subType.getDirectAllowedRelationships( direction ) )
			{
				if ( relationship.getNameOfType().equals(
					relationshipTypeName ) )
					
				{
					return new TypeConstraints( false, "Adding an allowed " +
						"relationship with name '" + relationshipTypeName +
						"' to node type '" + nodeType.getName() + "' would " +
						"violate type constraints since the node type's " +
						"subtype '" + subType.getName() + "' already has " +
						"a relationship of the same type and direction" );
				}
			}
		}
		return TypeConstraints.OPERATION_OK_GENERIC_MESSAGE;
	}
	
	static TypeConstraints addSubType( NodeType type, NodeType subType )
	{
		// Adding a subtype is invalid if adding any of the subtype's properties
		// or relationships would be invalid
		Transaction tx = Transaction.begin();
		try
		{
			// Try to add the subtype's properties to the potential supertype
			for ( MetaProperty property : subType.getRequiredProperties() )
			{
				if ( !addProperty( type, property.getKey() ).isValid() )
				{
					tx.success();
					return TypeConstraints.OPERATION_VIOLATED_GENERIC_MESSAGE;
				}
			}
			
			// Then try all relationships
			if ( incompatibleRelationships( type, subType, Direction.OUTGOING )
				|| incompatibleRelationships( type, subType,
					Direction.INCOMING ) )
			{
				tx.success();
				return TypeConstraints.OPERATION_VIOLATED_GENERIC_MESSAGE;
			}
			
			tx.success();
			return TypeConstraints.OPERATION_OK_GENERIC_MESSAGE;
		}
		finally
		{
			tx.finish();
		}
	}
	
	private static boolean incompatibleRelationships( NodeType type, NodeType
		subType, Direction direction )
	{
		for ( MetaRelationship relationship :
			subType.getAllowedRelationships( direction ) )
		{
			if ( !addRelationship( type, relationship.getNameOfType(),
				direction ).isValid() )
			{
				return true;
			}
		}		
		return false;
	}
	
	static TypeConstraints addSuperType( NodeType type, NodeType superType )
	{
		return addSubType( superType, type );
	}
}
