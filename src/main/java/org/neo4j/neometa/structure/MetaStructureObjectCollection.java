package org.neo4j.neometa.structure;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.util.NeoRelationshipSet;

class MetaStructureObjectCollection<T extends MetaStructureObject>
	extends NeoRelationshipSet<T>
{
	private MetaStructure meta;
	private Class<T> cls;
	
	MetaStructureObjectCollection( Node node, RelationshipType relType,
		Direction direction, MetaStructure meta, Class<T> cls )
	{
		super( node, relType, direction );
		this.meta = meta;
		this.cls = cls;
	}
	
	protected MetaStructure meta()
	{
		return this.meta;
	}
	
	@Override
	protected Node getNodeFromItem( Object item )
	{
		return ( ( MetaStructureObject ) item ).node();
	}
	
	@Override
	protected T newObject( Node node, Relationship rel )
	{
		try
		{
			return cls.getConstructor( MetaStructure.class,
				Node.class ).newInstance( meta(), node );
		}
		catch ( Exception e )
		{
			throw new RuntimeException( e );
		}
	}
	
	@Override
	protected void removeItem( Relationship rel )
	{
		throw new UnsupportedOperationException( "Not supported, as of yet" );
	}
}
