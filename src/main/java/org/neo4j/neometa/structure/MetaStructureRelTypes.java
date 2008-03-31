package org.neo4j.neometa.structure;

import org.neo4j.api.core.RelationshipType;

public enum MetaStructureRelTypes implements RelationshipType
{
	META_NAMESPACE,
	META_CLASS,
	META_PROPERTY,
	META_PROPERTY_HAS_RANGE,
	META_CLASS_HAS_PROPERTY,
	META_IS_SUBCLASS_OF,
	META_IS_SUBPROPERTY_OF,
}
