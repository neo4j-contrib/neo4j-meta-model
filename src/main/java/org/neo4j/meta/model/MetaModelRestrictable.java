package org.neo4j.meta.model;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.neo4j.graphdb.Node;

/**
 * Common functionality for properties and property restrictions (f.ex. cardinality and values).
 */
public interface MetaModelRestrictable<T>
{
	/**
	 * @return the underlying {@link Node}.
	 */
	Node node();
	
	/**
	 * @return the underlying {@link MetaModel}.
	 */
	MetaModel model();
	
	/**
	 * @return the name of the object.
	 */
	String getName();
	
	/**
	 * @return the mimimum cardinality set for this property. Can return
	 * {@code null} which means no restriction.
	 */
	Integer getMinCardinality();
	
	/**
	 * Sets the minimum cardinality of this property. {@code null} means
	 * no restriction.
	 * @param minCardinality the minimum cardinality to set.
	 */
	void setMinCardinality( Integer minCardinality );

	/**
	 * @return the maximum cardinality set for this property. Can return
	 * {@code null} which means no restriction.
	 */
	Integer getMaxCardinality();
	
	/**
	 * Sets the maximum cardinality of this property. {@code null} means
	 * no restriction.
	 * @param maxCardinality the maximum cardinality to set.
	 */
	void setMaxCardinality( Integer maxCardinality );
	
	/**
	 * Convenience method for setting both min and max cardinality.
	 * @param cardinality the min and max cardinality to set.
	 */
	void setCardinality( Integer cardinality );
	
	/**
	 * Sets the range of the expected value(s) for this restriction. F.ex.
	 * a string, a number or a an instance of a {@link MetaModelClass}.
	 * @param range the property range.
	 */
	void setRange(T  range );
	
	/**
	 * @return the {@link Range} set with
	 * {@link #setRange(Range)} or {@code null} if no range is
	 * specifically set for this object.
	 */
	T getRange();
	
	/**
	 * If cardinality is >1 then this will decide the rules of the collection.
	 * F.ex {@link Set} doesn't allow duplicates whereas {@link List} will.
	 * @param collectionClass the collection class type.
	 */
	void setCollectionBehaviourClass(
		Class<? extends Collection> collectionClass );

	/**
	 * @return the collection behaviour set with
	 * {@link #setCollectionBehaviourClass(Class)}.
	 */
	Class<? extends Collection<?>> getCollectionBehaviourClass();
}
