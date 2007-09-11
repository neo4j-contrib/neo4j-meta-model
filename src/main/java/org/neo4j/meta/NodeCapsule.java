package org.neo4j.meta;

import org.neo4j.api.core.Node;

public final class NodeCapsule
{
	private final Node underlyingNode;
	
	NodeCapsule( Node underlyingNode )
	{
		if ( underlyingNode == null )
		{
			throw new NullPointerException( "NodeCapsule cannot wrap null" );
		}
		this.underlyingNode = underlyingNode;
	}
	
	public Node getUnderlyingNode()
	{
		return this.underlyingNode;
	}
	
	public MetaInfo getMetaInfo()
	{
		return null;
	}
	
}
