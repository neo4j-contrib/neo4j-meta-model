package org.neo4j.meta;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.util.NodeWrapperRelationshipSet;

public class MetaWrapperRelationshipSet<T extends MetaNodeWrapper> extends
    NodeWrapperRelationshipSet<T>
{
	static enum AssociationLevel { AGGREGATE, SOFT }
	
	private MetaManager metaManager;
	private AssociationLevel associationLevel;
	
	MetaWrapperRelationshipSet( MetaManager metaManager, Node underlyingNode,
		RelationshipType relationshipType, Direction direction, Class<T> type,
		AssociationLevel associationLevel )
	{
		super( underlyingNode, relationshipType, direction, type );
		this.metaManager = metaManager;
		this.associationLevel = associationLevel;
	}
	
	protected MetaManager getMetaManager()
	{
		return this.metaManager;
	}
	
	protected AssociationLevel getAssociationLevel()
	{
		return this.associationLevel;
	}
	
	@Override
	protected void removeItem( Relationship rel )
	{
		if ( this.getAssociationLevel() == AssociationLevel.AGGREGATE )
		{
			T metaWrapper = newObject( getOtherNode( rel ), rel );
			metaWrapper.cascadingDelete();
		}
		super.removeItem( rel );
	}

	@Override
	protected T newObject( Node node, Relationship relationship )
	{
		return MetaNodeWrapper.newInstance( this.getInstanceClass(), node,
			this.getMetaManager() );
	}
}
