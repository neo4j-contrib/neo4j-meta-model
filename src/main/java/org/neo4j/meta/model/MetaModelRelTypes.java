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
	 * Meta model root node --> {@link MetaModelNamespace} node.
	 */
	META_NAMESPACE,
	
	/**
	 * {@link MetaModelNamespace} node --> {@link MetaModelClass} node.
	 */
	META_CLASS,
	
	/**
	 * {@link MetaModelNamespace} node -->
	 * {@link MetaModelProperty} node.
	 */
	META_PROPERTY,
	
	/**
	 * {@link MetaModelProperty} node --> {@link MetaModelClass} node.
	 * For property ranges which is a {@link MetaModelClass}.
	 */
	META_PROPERTY_HAS_RANGE,
	
	/**
	 * {@link MetaModelClass} node --> {@link MetaModelProperty} node.
	 * Tells that a property is in a class' domain.
	 */
	META_CLASS_HAS_PROPERTY,
	
	/**
	 * {@link MetaModelClass} node --> {@link MetaModelClass} node.
	 */
	META_IS_SUBCLASS_OF,
	
	/**
	 * {@link MetaModelProperty} node -->
	 * {@link MetaModelProperty} node.
	 */
	META_IS_SUBPROPERTY_OF,
	
	/**
	 * A node representing an object beeing an instance of a class -->
	 * {@link MetaModelClass} node.
	 */
	META_IS_INSTANCE_OF,
	
	/**
	 * The OWL construct owl:inverseOf between properties.
	 */
	META_IS_INVERSE_OF,
	
	/**
	 * A {@link MetaModelRestriction} to its {@link MetaModelClass}.
	 */
	META_RESTRICTION_TO_CLASS,
	
	/**
	 * A {@link MetaModelRestriction} to its {@link MetaModelProperty}.
	 */
	META_RESTRICTION_TO_PROPERTY,
}
