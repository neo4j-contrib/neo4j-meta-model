package org.neo4j.neometa.model;

import java.util.Collection;

import org.neo4j.api.core.Transaction;
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
	 * @return a modifiable collection of the superclasses to this class.
	 */
	public Collection<MetaClass> getDirectSuperClasses()
	{
		return new MetaObjectCollection.MetaClassCollection( model(),
			getThing().getDirectSupers() );
	}
	
	/**
	 * @return a modifiable collection of the subclasses to this class.
	 */
	public Collection<MetaClass> getDirectSubClasses()
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
	public MetaProperty getDeclaredProperty( String name, boolean allowCreate )
	{
		Transaction tx = model().meta().neo().beginTx();
		try
		{
			MetaStructureProperty metaProperty = model().meta().getNamespace(
				getName(), true ).getMetaProperty( name, allowCreate );
			if ( allowCreate )
			{
				getThing().getDirectProperties().add( metaProperty );
			}
			MetaProperty result = metaProperty == null ? null :
				new MetaProperty( model(), metaProperty );
			tx.success();
			return result;
		}
		finally
		{
			tx.finish();
		}
	}
	
	/**
	 * @return a modifiable collection of all declared properties for this
	 * class.
	 */
	public Collection<MetaProperty> getDeclaredProperties()
	{
		return new MetaObjectCollection.MetaPropertyCollection( model(),
			getThing().getDirectProperties() );
	}
	
	/**
	 * @param name the name of the property.
	 * @param allowCreate if {@code true} and no property by the given
	 * {@code name} exists then it is created.
	 * @return the property (declared or inherited) for this class, or
	 * {@code null} if not found.
	 */
	public MetaProperty getProperty( String name, boolean allowCreate )
	{
		Transaction tx = model().meta().neo().beginTx();
		try
		{
			MetaProperty result = getDeclaredProperty( name, false );
			if ( result == null )
			{
				for ( MetaClass cls : getDirectSuperClasses() )
				{
					MetaProperty property = cls.getProperty( name, false );
					if ( property != null )
					{
						result = property;
						break;
					}
				}
			}
			if ( result == null && allowCreate )
			{
				result = getDeclaredProperty( name, allowCreate );
			}
			tx.success();
			return result;
		}
		finally
		{
			tx.finish();
		}
	}
	
	/**
	 * @return an unmodifiable collection of all declared and
	 * inherited properties for this class.
	 */
	public Collection<MetaProperty> getProperties()
	{
		return new MetaObjectCollection.MetaPropertyCollection( model(),
			getThing().getAllProperties() );
	}
}
