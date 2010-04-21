package org.neo4j.meta.model;

/**
 * A hook for retreiving a specific value from a
 * {@link MetaModelRelationshipRestrictable}.
 * @param <T> the type of the returned value.
 */
public interface RelationshipLookerUpper<T>
{
	/**
	 * @param restrictable the object to get the value from.
	 * @return the looked up value.
	 */
	T get( MetaModelRestrictable<RelationshipRange> restrictable );
}
