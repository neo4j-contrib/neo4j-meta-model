package org.neo4j.meta;

import org.neo4j.api.core.RelationshipType;

public enum MetaRelTypes implements RelationshipType
{
	META_SUBREFERENCE_ROOT,
	META_ROOT_TO_NODE_TYPE,
	META_NODE_TYPE_TO_SUPER_TYPE,
	META_NODE_TYPE_TO_PROPERTY,
	META_NODE_TYPE_VIA_REL_TO_NODE_TYPE,
	META_INSTANCE_OF,
}
