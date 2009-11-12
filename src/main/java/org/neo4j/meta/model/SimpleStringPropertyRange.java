package org.neo4j.meta.model;

/**
 * Common functionality for {@link PropertyRange} implementations where
 * only one string needs to be stored.
 */
public abstract class SimpleStringPropertyRange extends PropertyRange
{
	private static final String KEY_RANGE_SPEC = "range_specification";
	
	protected abstract String toStringRepresentation(
		MetaModelRestrictable owner );
	
	protected abstract void fromStringRepresentation(
		MetaModelRestrictable owner, String stringRepresentation );
	
	@Override
	protected void internalStore( MetaModelRestrictable owner )
	{
		owner.node().setProperty( KEY_RANGE_SPEC,
			toStringRepresentation( owner ) );
	}
	
	@Override
	protected void internalLoad( MetaModelRestrictable owner )
	{
		fromStringRepresentation( owner,
			( String ) owner.node().getProperty( KEY_RANGE_SPEC ) );
	}
	
	@Override
	protected void internalRemove( MetaModelRestrictable owner )
	{
		owner.node().removeProperty( KEY_RANGE_SPEC );
	}
	
	@Override
	public boolean isDatatype()
	{
		return true;
	}
}
