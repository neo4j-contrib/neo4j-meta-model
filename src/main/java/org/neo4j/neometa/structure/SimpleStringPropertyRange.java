package org.neo4j.neometa.structure;

/**
 * Common functionality for {@link PropertyRange} implementations where
 * only one string needs to be stored.
 */
public abstract class SimpleStringPropertyRange extends PropertyRange
{
	private static final String KEY_RANGE_STRING = "range_string";
	
	protected abstract String toStringRepresentation(
		MetaStructureProperty property );
	
	protected abstract void fromStringRepresentation(
		MetaStructureProperty property, String stringRepresentation );
	
	@Override
	protected void internalStore( MetaStructureProperty property )
	{
		property.node().setProperty( KEY_RANGE_STRING,
			toStringRepresentation( property ) );
	}
	
	@Override
	protected void internalLoad( MetaStructureProperty property )
	{
		fromStringRepresentation( property,
			( String ) property.node().getProperty( KEY_RANGE_STRING ) );
	}
	
	@Override
	public boolean isDatatype()
	{
		return true;
	}
}
