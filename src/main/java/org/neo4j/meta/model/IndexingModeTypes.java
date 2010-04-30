package org.neo4j.meta.model;

/**
 * Contains all indexing options for a property.
 */

public enum IndexingModeTypes
{
	/**
	 * Indicates that property is not being indexed
	 * 
	 */
	NO_INDEX,
	
	/**
	 * Indicates that property is being indexed
	 */
	INDEX,
	
	/**
	 * Indicates that property is indexed and its value must be unique 
	 */
	UNIQUE_INDEX,
	
	/**
	 * Indicates that property has a full text index
	 */
	FULL_TEXT_INDEX,

}
