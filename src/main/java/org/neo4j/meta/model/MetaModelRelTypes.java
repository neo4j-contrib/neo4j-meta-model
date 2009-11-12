package org.neo4j.meta.model;

import org.neo4j.api.core.RelationshipType;

/**
 * Contains all the meta-model relationship types.
 */
public enum MetaModelRelTypes implements RelationshipType
{
	/**
	 * Reference node --> The meta model subref node which is the root of
	 * the entire meta model.
	 */
	REF_TO_META_SUBREF,
	
	/**
	 * Meta model root node --> {@link MetaStructureNamespace} node.
	 */
	META_NAMESPACE,
	
	/**
	 * {@link MetaStructureNamespace} node --> {@link MetaStructureClass} node.
	 */
	META_CLASS,
	
	/**
	 * {@link MetaStructureNamespace} node -->
	 * {@link MetaStructureProperty} node.
	 */
	META_PROPERTY,
	
	/**
	 * {@link MetaStructureProperty} node --> {@link MetaStructureClass} node.
	 * For property ranges which is a {@link MetaStructureClass}.
	 */
	META_PROPERTY_HAS_RANGE,
	
	/**
	 * {@link MetaStructureClass} node --> {@link MetaStructureProperty} node.
	 * Tells that a property is in a class' domain.
	 */
	META_CLASS_HAS_PROPERTY,
	
	/**
	 * {@link MetaStructureClass} node --> {@link MetaStructureClass} node.
	 */
	META_IS_SUBCLASS_OF,
	
	/**
	 * {@link MetaStructureProperty} node -->
	 * {@link MetaStructureProperty} node.
	 */
	META_IS_SUBPROPERTY_OF,
	
	/**
	 * A node representing an object beeing an instance of a class -->
	 * {@link MetaStructureClass} node.
	 */
	META_IS_INSTANCE_OF,
	
	/**
	 * The OWL construct owl:inverseOf between properties.
	 */
	META_IS_INVERSE_OF,
	
	/**
	 * A {@link MetaStructureRestriction} to its {@link MetaStructureClass}.
	 */
	META_RESTRICTION_TO_CLASS,
	
	/**
	 * A {@link MetaStructureRestriction} to its {@link MetaStructureProperty}.
	 */
	META_RESTRICTION_TO_PROPERTY,
}
