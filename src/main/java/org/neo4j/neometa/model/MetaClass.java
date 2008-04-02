package org.neo4j.neometa.model;

import java.util.Collection;

import org.neo4j.neometa.structure.MetaStructureClass;
import org.neo4j.neometa.structure.MetaStructureProperty;

/**
 * Represents a class in the model.
 */
public class MetaClass extends MetaObject<MetaStructureClass>
{
	MetaClass( MetaModel model, MetaStructureClass metaClass )
	{
		super( model, metaClass );
	}
	
	/**
	 * @return a modifiable collection of the extending classes to this class.
	 */
	public Collection<MetaClass> getExtendingClasses()
	{
		return new MetaObjectCollection.MetaClassCollection( model(),
			getThing().getDirectSubs() );
	}
	
	/**
	 * @param name the name of the property.
	 * @param allowCreate if {@code true} and no property by the given
	 * {@code name} exists then it is created.
	 * @return the {@link MetaProperty} by the name {@code name} for this class.
	 */
	public MetaProperty getProperty( String name, boolean allowCreate )
	{
		MetaStructureProperty metaProperty = model().meta().getNamespace(
			getName(), true ).getMetaProperty( name, allowCreate );
		getThing().getProperties().add( metaProperty );
		return metaProperty == null ? null : new MetaProperty( model(),
			metaProperty );
	}
	
	/**
	 * @return a modifiable collection of all the properties defined for this
	 * class.
	 */
	public Collection<MetaProperty> getProperties()
	{
		return new MetaObjectCollection.MetaPropertyCollection( model(),
			getThing().getProperties() );
	}
}
