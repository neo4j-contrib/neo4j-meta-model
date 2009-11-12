package org.neo4j.meta.model;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.util.NeoRelationshipSet;

class ObjectCollection<T extends MetaModelObject>
	extends NeoRelationshipSet<T>
{
	private MetaModel meta;
	private Class<T> cls;
	
	ObjectCollection( NeoService neo, Node node,
		RelationshipType relType, Direction direction,
		MetaModel meta, Class<T> cls )
	{
		super( neo, node, relType, direction );
		this.meta = meta;
		this.cls = cls;
	}
	
	protected MetaModel meta()
	{
		return this.meta;
	}
	
	@Override
	protected Node getNodeFromItem( Object item )
	{
		return ( ( MetaModelObject ) item ).node();
	}
	
	@Override
	protected T newObject( Node node, Relationship rel )
	{
		try
		{
			return cls.getConstructor( MetaModel.class,
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
