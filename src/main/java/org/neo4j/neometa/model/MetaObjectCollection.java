package org.neo4j.neometa.model;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.neometa.structure.MetaStructure;
import org.neo4j.neometa.structure.MetaStructureThing;
import org.neo4j.util.NeoRelationshipSet;

class MetaObjectCollection<T extends MetaObject<? extends MetaStructureThing>>
	extends NeoRelationshipSet<T>
{
	private MetaModel model;
	private Class<T> metaClass;
	
	MetaObjectCollection( Node node, RelationshipType type,
		Direction direction, MetaModel model, Class<T> metaClass )
	{
		super( node, type, direction );
		this.model = model;
		this.metaClass = metaClass;
	}
	
	@Override
	protected Node getNodeFromItem( Object item )
	{
		return ( ( MetaObject<?> ) item ).getThing().node();
	}
	
	@Override
	protected T newObject( Node node, Relationship rel )
	{
		try
		{
			MetaStructureThing thing = MetaClassMapping.getMetaStructureClass(
				metaClass ).getConstructor( MetaStructure.class,
					Node.class ).newInstance( model.meta(), node );
			return metaClass.getConstructor( MetaModel.class,
				MetaStructureThing.class ).newInstance( model, thing );
		}
		catch ( Exception e )
		{
			throw new RuntimeException( e );
		}
	}
	
	@Override
	protected void removeItem( Relationship relationship )
	{
		throw new UnsupportedOperationException( "Not yet" );
	}
}
