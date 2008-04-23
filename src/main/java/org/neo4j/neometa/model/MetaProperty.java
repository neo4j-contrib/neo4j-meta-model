package org.neo4j.neometa.model;

import org.neo4j.api.core.Node;
import org.neo4j.neometa.structure.DatatypeClassRange;
import org.neo4j.neometa.structure.MetaStructureClassRange;
import org.neo4j.neometa.structure.MetaStructureClass;
import org.neo4j.neometa.structure.MetaStructureProperty;

/**
 * Represents a property in the model.
 */
public class MetaProperty extends MetaObject<MetaStructureProperty>
{
	MetaProperty( MetaModel model, MetaStructureProperty metaProperty )
	{
		super( model, metaProperty );
	}
	
	/**
	 * @return the owner of this property, i.e. the class which defined this
	 * property.
	 */
	public MetaClass getOwner()
	{
		MetaStructureClass metaClass =
			getThing().associatedMetaClasses().iterator().next();
		return new MetaClass( model(), metaClass );
	}
	
	/**
	 * Tells that the value of this property must be of type {@code cls}.
	 * @param cls the value type.
	 */
	public void setFundamentalValueType( Class<?> cls )
	{
		getThing().setRange( new DatatypeClassRange( cls ) );
	}
	
	/**
	 * Tells that the value of this property must be an instance of a
	 * {@link MetaClass}, i.e. an instance {@link Node} complying to
	 * {@code cls}.
	 * @param cls
	 */
	public void setMetaClassValueType( MetaClass cls )
	{
		getThing().setRange( new MetaStructureClassRange( cls.getThing() ) );
	}
}
