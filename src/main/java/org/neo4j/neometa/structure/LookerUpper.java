package org.neo4j.neometa.structure;

/**
 * A hook for retreiving a specific value from a
 * {@link MetaStructureRestrictable}.
 * @param <T> the type of the returned value.
 */
interface LookerUpper<T>
{
	/**
	 * @param restrictable the object to get the value from.
	 * @return the looked up value.
	 */
	T get( MetaStructureRestrictable restrictable );
}
