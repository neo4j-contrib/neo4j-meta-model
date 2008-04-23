package org.neo4j.neometa.structure;

abstract class LookupUtil
{
	static <T> T lookup( MetaStructureProperty property,
		LookerUpper<T> finder, MetaStructureClass... inTheseClasses )
	{
		T result = null;
		for ( MetaStructureClass cls : inTheseClasses )
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
		MetaStructureProperty property, LookerUpper<T> finder )
	{
		T value = finder.get( property );
		if ( value != null )
		{
			return value;
		}
		for ( MetaStructureProperty superProperty : property.getDirectSupers() )
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
		MetaStructureProperty property, LookerUpper<T> finder,
		MetaStructureClass cls )
	{
		MetaStructureRestriction restriction = cls.getRestriction( property,
			false );
		if ( restriction != null )
		{
			T value = finder.get( restriction );
			if ( value != null )
			{
				return value;
			}
		}
		
		for ( MetaStructureClass superClass : cls.getDirectSupers() )
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
