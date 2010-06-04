package org.neo4j.meta.model;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.util.RelationshipSet;

class ObjectCollection<T extends MetaModelObject>
	extends RelationshipSet<T>
{
	private MetaModel model;
	private Class<T> cls;
	
	ObjectCollection( Node node,
		RelationshipType relType, Direction direction,
		MetaModel model, Class<T> cls )
	{
		super( node, relType, direction );
		this.model = model;
		this.cls = cls;
	}
	
	protected MetaModel model()
	{
		return this.model;
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
				Node.class ).newInstance( model(), node );
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
