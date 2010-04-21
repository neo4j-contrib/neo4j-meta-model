package org.neo4j.meta.model;

import org.neo4j.graphdb.RelationshipType;

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
	 * {@link MetaModelNamespace} node -->
	 * {@link MetaModelProperty} node.
	 */
	META_RELATIONSHIP,
	
	
	/**
	 * {@link MetaModelRelationship} node --> {@link MetaModelClass} node.
	 * For property ranges which is a {@link MetaModelClass}.
	 */
	META_RELATIONSHIP_HAS_RANGE,
	

	/**
	 * {@link MetaModelRelationship} node --> {@link MetaModelProperty} node.
	 * Tells that a property is in a class' domain.
	 */
	META_HAS_PROPERTY,
	
	
	/**
	 * {@link MetaModelClass} node --> {@link MetaModelRelationship} node.
	 * Tells that a property is in a class' domain.
	 */
	META_CLASS_HAS_RELATIONSHIP,
	
	
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
	 * {@link MetaModelRelationship} node -->
	 * {@link MetaModelRelationship} node.
	 */
	META_IS_SUBRELATIONSHIP_OF,
	
	
	/**
	 * {@link MetaModelClass} node --> node.
	 * 
	 */
	META_HAS_INSTANCE,
	
	
	/**
	 * The OWL construct owl:inverseOf between properties.
	 */
	META_IS_INVERSE_OF,
	
	/**
	 * A {@link MetaModelPropertyRestriction} to its {@link MetaModelPropertyContainer}.
	 */
	META_PROPERTY_RESTRICTION_TO_PROPERTYCONTAINER,

	/**
	 * A {@link MetaModelRelationshipRestriction} to its {@link MetaModelClass}.
	 */
	META_RELATIONSHIP_RESTRICTION_TO_CLASS,

	
	/**
	 * A {@link MetaModelPropertyRestriction} to its {@link MetaModelProperty}.
	 */
	META_RESTRICTION_TO_PROPERTY,
	
	/**
	 * A {@link MetaModelRelationshipRestriction} to its {@link MetaModelProperty}.
	 */
	META_RESTRICTION_TO_RELATIONSHIP,

}
