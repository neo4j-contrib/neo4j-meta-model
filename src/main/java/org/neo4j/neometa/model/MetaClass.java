package org.neo4j.neometa.model;

import java.util.Collection;

import org.neo4j.api.core.Direction;
import org.neo4j.neometa.structure.MetaStructureClass;
import org.neo4j.neometa.structure.MetaStructureProperty;
import org.neo4j.neometa.structure.MetaStructureRelTypes;

public class MetaClass extends MetaObject<MetaStructureClass>
{
	MetaClass( MetaModel model, MetaStructureClass metaClass )
	{
		super( model, metaClass );
	}
	
	public Collection<MetaClass> getChildren()
	{
		return new MetaObjectCollection<MetaClass>( getThing().node(),
			MetaStructureRelTypes.META_IS_SUBCLASS_OF, Direction.INCOMING,
			model(), MetaClass.class );
	}
	
	public MetaProperty getProperty( String name, boolean allowCreate )
	{
		MetaStructureProperty metaProperty = model().meta().getNamespace(
			getName(), true ).getMetaProperty( name, allowCreate );
		getThing().getProperties().add( metaProperty );
		return metaProperty == null ? null : new MetaProperty( model(),
			metaProperty );
	}
	
	public Collection<MetaProperty> getProperties()
	{
		return new MetaObjectCollection<MetaProperty>( getThing().node(),
			MetaStructureRelTypes.META_CLASS_HAS_PROPERTY, Direction.OUTGOING,
			model(), MetaProperty.class );
	}
}
