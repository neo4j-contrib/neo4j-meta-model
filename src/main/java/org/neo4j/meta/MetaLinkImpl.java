package org.neo4j.meta;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.util.LinkImpl;

public class MetaLinkImpl<T extends MetaNodeWrapper> extends LinkImpl<T>
{
	private MetaManager metaManager;
	
	public MetaLinkImpl( MetaManager metaManager, Node underlyingNode,
		RelationshipType type, Direction direction, Class<T> wrapperClass )
	{		
		super( underlyingNode, type, wrapperClass, direction );
		this.metaManager = metaManager;
	}
	
	protected MetaManager getMetaManager()
	{
		return this.metaManager;
	}

	@Override
	protected T newObject( Node node )
	{
		return MetaNodeWrapper.newInstance( classType(), node,
			this.getMetaManager() );
	}
}
