package org.neo4j.meta.model;

import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.RelationshipType;

/**
 * An implementation of {@link RelationshipRange} for values which are instances
 * of {@link MetaModelNamespace}.
 */
public class NamespaceRange extends RelationshipRange
{
	
	public NamespaceRange()
	{
	}
	
	/**
	 */
	
	/**
	 * TODO Explain better!
	 * @return the {@link RelationshipType} which should be created between
	 * a meta instance and the other meta instance.
	 */
	public RelationshipType getRelationshipTypeToUse()
	{
	    return DynamicRelationshipType.withName( getOwner().getName() );
	}

	@Override
	protected void internalLoad( MetaModelRestrictable<RelationshipRange> owner )
	{
	}
	
	@Override
	protected void internalRemove( MetaModelRestrictable<RelationshipRange> owner )
	{
	}

	@Override
	protected void internalStore(MetaModelRestrictable<RelationshipRange> owner) {
	}
}
