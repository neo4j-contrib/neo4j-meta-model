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
 * of {@link MetaModelClass}.
 */
public class ClassRange extends RelationshipRange
{
	private Set<MetaModelClass> rangeClasses;
	
	/**
	 * @param rangeClasses the classes the value has to comply to.
	 */
	public ClassRange( MetaModelClass... rangeClasses )
	{
		this.rangeClasses = new HashSet<MetaModelClass>(
			Arrays.asList( rangeClasses ) );
	}
	
	/**
	 * Internal usage.
	 */
	public ClassRange()
	{
	}
	
	/**
	 * @return the set classes.
	 */
	public MetaModelClass[] getRangeClasses()
	{
		return this.rangeClasses.toArray(
			new MetaModelClass[ rangeClasses.size() ] );
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
		for ( MetaModelClass cls : this.rangeClasses )
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
		this.rangeClasses = new HashSet<MetaModelClass>();
		for ( Relationship rel : getRelationships( owner ) )
		{
			this.rangeClasses.add( new MetaModelClass( owner.model(),
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
			rangeClasses.toArray(
				new MetaModelClass[ rangeClasses.size() ] ) ) + "]";
	}
}
