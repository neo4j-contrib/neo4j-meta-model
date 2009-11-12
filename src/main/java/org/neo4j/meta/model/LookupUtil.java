package org.neo4j.meta.model;

abstract class LookupUtil
{
	static <T> T lookup( MetaModelProperty property,
		LookerUpper<T> finder, MetaModelClass... inTheseClasses )
	{
		T result = null;
		for ( MetaModelClass cls : inTheseClasses )
		{
			T value = tryLookupFromRestrictions( property, finder, cls );
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
	
	private static <T> T tryLookupFromProperty(
		MetaModelProperty property, LookerUpper<T> finder )
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
	
	private static <T> T tryLookupFromRestrictions(
		MetaModelProperty property, LookerUpper<T> finder,
		MetaModelClass cls )
	{
		MetaModelRestriction restriction = cls.getRestriction( property,
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
				tryLookupFromRestrictions( property, finder, superClass );
			if ( value != null )
			{
				return value;
			}
		}
		return null;
	}
}
