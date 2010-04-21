package org.neo4j.meta.model;

import org.neo4j.graphdb.GraphDatabaseService;

/**
 * The range of a property, i.e. a properties expected value type. F.ex. it
 * could be a String, an integer or a {@link MetaModelClass} (which
 * would refer to a {@link Node} which has a relationship to that class).
 */
public abstract class Range<T>
{
	static final String KEY_RANGE_IMPL = "range_implementation_class";
	
	protected MetaModelRestrictable<T> owner;
	
	protected MetaModelRestrictable<T> getOwner()
	{
		return this.owner;
	}
	
	protected static GraphDatabaseService graphDb( MetaModel meta )
	{
		return ( ( MetaModelImpl ) meta ).graphDb();
	}
	
	protected abstract void internalStore( MetaModelRestrictable<T> owner );
	
	protected abstract void internalRemove( MetaModelRestrictable<T> owner );
	
	protected abstract void internalLoad( MetaModelRestrictable<T> owner );
	
	protected abstract void store( MetaModelRestrictable<T> owner );

	
}
