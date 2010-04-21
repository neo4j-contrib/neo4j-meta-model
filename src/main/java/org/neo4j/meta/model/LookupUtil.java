package org.neo4j.meta.model;

abstract class LookupUtil
{
	static <T> T lookup( MetaModelProperty property,
		PropertyLookerUpper<T> finder, MetaModelClass... inTheseClasses )
	{
		T result = null;
		for ( MetaModelClass cls : inTheseClasses )
		{
			T value = tryLookupFromPropertyRestrictions( property, finder, cls );
			if ( value != null )
			{
				result = value;
				break;
			}
		}
		
		if ( result == null )
		{
			result = tryLookupFromProperty( property, finder );
		}
		return result;
	}
	
	static <T> T lookup( MetaModelRelationship relationshipType,
			RelationshipLookerUpper<T> finder, MetaModelClass... inTheseClasses )
	{
		T result = null;
		for ( MetaModelClass cls : inTheseClasses )
		{
			T value = tryLookupFromRelationshipTypeRestrictions( relationshipType, finder, cls );
			if ( value != null )
			{
				result = value;
				break;
			}
		}
		
		if ( result == null )
		{
			result = tryLookupFromRelationshipType( relationshipType, finder );
		}
		return result;
	}
	
	private static <T> T tryLookupFromProperty(
		MetaModelProperty property, PropertyLookerUpper<T> finder )
	{
		T value = finder.get( property );
		if ( value != null )
		{
			return value;
		}
		for ( MetaModelProperty superProperty : property.getDirectSupers() )
		{
			value = tryLookupFromProperty( superProperty, finder );
			if ( value != null )
			{
				return value;
			}
		}
		return null;
	}

	private static <T> T tryLookupFromRelationshipType(
			MetaModelRelationship relationshipType, RelationshipLookerUpper<T> finder )
	{
		T value = finder.get( relationshipType );
		if ( value != null )
		{
			return value;
		}
		for ( MetaModelRelationship superRelationshipType : relationshipType.getDirectSupers() )
		{
			value = tryLookupFromRelationshipType( superRelationshipType, finder );
			if ( value != null )
			{
				return value;
			}
		}
		return null;
	}
	
	private static <T> T tryLookupFromPropertyRestrictions(
		MetaModelProperty property, PropertyLookerUpper<T> finder,
		MetaModelClass cls )
	{
		MetaModelPropertyRestriction restriction = cls.getRestriction( property,
			false );
		if ( restriction != null )
		{
			T value = finder.get( restriction );
			if ( value != null )
			{
				return value;
			}
		}
		
		for ( MetaModelClass superClass : cls.getDirectSupers() )
		{
			T value =
				tryLookupFromPropertyRestrictions( property, finder, superClass );
			if ( value != null )
			{
				return value;
			}
		}
		return null;
	}
	
	private static <T> T tryLookupFromRelationshipTypeRestrictions(
			MetaModelRelationship relationshipType, RelationshipLookerUpper<T> finder,
			MetaModelClass cls )
	{
		
		MetaModelRelationshipRestriction restriction = cls.getRestriction( relationshipType,
			false );
		if ( restriction != null )
		{
			T value = finder.get( restriction );
			if ( value != null )
			{
				return value;
			}
		}
		
		for ( MetaModelClass superClass : cls.getDirectSupers() )
		{
			T value =
				tryLookupFromRelationshipTypeRestrictions( relationshipType, finder, superClass );
			if ( value != null )
			{
				return value;
			}
		}
		return null;
	}

}
