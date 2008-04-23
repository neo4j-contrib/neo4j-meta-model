package org.neo4j.neometa.model;

import java.util.Collection;

import org.neo4j.neometa.structure.MetaStructureClass;
import org.neo4j.neometa.structure.MetaStructureProperty;
import org.neo4j.neometa.structure.MetaStructureThing;
import org.neo4j.util.CollectionWrapper;

abstract class MetaObjectCollection<T extends MetaObject<U>,
	U extends MetaStructureThing> extends CollectionWrapper<T, U>
{
	private MetaModel model;
	
	MetaObjectCollection( MetaModel model, Collection<U> collection )
	{
		super( collection );
		this.model = model;
	}
	
	protected MetaModel model()
	{
		return model;
	}
	
	@Override
	protected U objectToUnderlyingObject( T object )
	{
		return object.getThing();
	}

	static class MetaClassCollection
		extends MetaObjectCollection<MetaClass, MetaStructureClass>
	{
		MetaClassCollection( MetaModel model,
			Collection<MetaStructureClass> collection )
		{
			super( model, collection );
		}
		
		@Override
        protected MetaClass underlyingObjectToObject(
        	MetaStructureClass object )
        {
			return new MetaClass( model(), object );
        }
	}

	static class MetaPropertyCollection
		extends MetaObjectCollection<MetaProperty, MetaStructureProperty>
	{
		MetaPropertyCollection( MetaModel model,
			Collection<MetaStructureProperty> collection )
		{
			super( model, collection );
		}
		
		@Override
		protected MetaProperty underlyingObjectToObject(
			MetaStructureProperty object )
		{
			return new MetaProperty( model(), object );
		}
	}
}
