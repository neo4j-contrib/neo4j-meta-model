package org.neo4j.meta;

import java.util.Collection;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;
import org.neo4j.util.NodeWrapperRelationshipSet;

class MetaInfoImpl implements MetaInfo
{
	private Node node;
	
	MetaInfoImpl( Node node )
	{
		this.node = node;
	}
	
	private Node getNode()
	{
		return this.node;
	}

	public Collection<NodeType> types()
	{
		return new NodeWrapperRelationshipSet(
			getNode(),
			MetaRelTypes.META_INSTANCE_OF,
			Direction.INCOMING,
			NodeTypeImpl.class );
	}

//	private Node findMetaNode()
//	{
//		Transaction tx = Transaction.begin();
//		try
//		{
//			Relationship rel = getNode().getSingleRelationship(
//				MetaRelTypes.META_INSTANCE_OF, Direction.INCOMING );
//			if (rel == null)
//			{
//				tx.success();
//				return null;
//			}
//			else
//			{
//				Node metaNode = rel.getOtherNode( this.getNode() );
//				tx.success();
//				return metaNode;
//			}
//		}
//		finally
//		{
//			tx.finish();
//		}
//	}
	
}