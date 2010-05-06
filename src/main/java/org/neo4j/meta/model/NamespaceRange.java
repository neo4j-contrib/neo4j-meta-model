package org.neo4j.meta.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

/**
 * An implementation of {@link RelationshipRange} for values which are instances
 * of {@link MetaModelNamespace}.
 */
public class NamespaceRange extends RelationshipRange
{
	private Set<MetaModelNamespace> rangeNamespaces;
	
	/**
	 * @param rangeNamespaces the namespaces the value has to comply with.
	 */
	public NamespaceRange( MetaModelNamespace... rangeNamespaces )
	{
		this.rangeNamespaces = new HashSet<MetaModelNamespace>(
			Arrays.asList( rangeNamespaces ) );
	}
	
	/**
	 * Internal usage.
	 */
	public NamespaceRange()
	{
	}
	
	/**
	 */
	public MetaModelNamespace[] getRangeNamespaces()
	{
		return this.rangeNamespaces.toArray(
			new MetaModelNamespace[ rangeNamespaces.size() ] );
	}
	
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
	protected void internalStore( MetaModelRestrictable<RelationshipRange> owner )
	{
		for ( MetaModelNamespace cls : this.rangeNamespaces )
		{
			owner.node().createRelationshipTo( cls.node(),
				MetaModelRelTypes.META_RELATIONSHIP_HAS_RANGE );
		}
	}
	
	private Iterable<Relationship> getRelationships(
		MetaModelRestrictable<RelationshipRange> owner )
	{
		return owner.node().getRelationships(
			MetaModelRelTypes.META_RELATIONSHIP_HAS_RANGE,
			Direction.OUTGOING );
	}
	
	@Override
	protected void internalLoad( MetaModelRestrictable<RelationshipRange> owner )
	{
		this.rangeNamespaces = new HashSet<MetaModelNamespace>();
		for ( Relationship rel : getRelationships( owner ) )
		{
			this.rangeNamespaces.add( new MetaModelNamespace( owner.model(),
				rel.getEndNode() ) );
		}
	}
	
	@Override
	protected void internalRemove( MetaModelRestrictable<RelationshipRange> owner )
	{
		for ( Relationship rel : getRelationships( owner ) )
		{
			rel.delete();
		}
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + StringUtil.join( ", ",
			rangeNamespaces.toArray(
				new MetaModelNamespace[ rangeNamespaces.size() ] ) ) + "]";
	}
}
