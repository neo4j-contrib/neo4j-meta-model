package org.neo4j.meta;

import org.neo4j.api.core.Node;

/**
 * Instances of objects represented in the meta model should implement this
 * interface.
 */
public interface MetaInstance
{
	/**
	 * @return the underlying neo node.
	 */
	Node getMetaNode();
}
