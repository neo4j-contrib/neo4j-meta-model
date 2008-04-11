package org.neo4j.neometa.structure;

/**
 * Common functionality for {@link PropertyRange} implementations where
 * only one string needs to be stored.
 */
public abstract class SimpleStringPropertyRange extends PropertyRange
{
	private static final String KEY_RANGE_SPEC = "range_specification";
	
	protected abstract String toStringRepresentation(
		MetaStructureRestrictable owner );
	
	protected abstract void fromStringRepresentation(
		MetaStructureRestrictable owner, String stringRepresentation );
	
	@Override
	protected void internalStore( MetaStructureRestrictable owner )
	{
		owner.node().setProperty( KEY_RANGE_SPEC,
			toStringRepresentation( owner ) );
	}
	
	@Override
	protected void internalLoad( MetaStructureRestrictable owner )
	{
		fromStringRepresentation( owner,
			( String ) owner.node().getProperty( KEY_RANGE_SPEC ) );
	}
	
	@Override
	protected void internalRemove( MetaStructureRestrictable owner )
	{
		owner.node().removeProperty( KEY_RANGE_SPEC );
	}
	
	@Override
	public boolean isDatatype()
	{
		return true;
	}
}
