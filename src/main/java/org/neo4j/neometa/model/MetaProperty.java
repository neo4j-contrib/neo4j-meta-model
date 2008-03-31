package org.neo4j.neometa.model;

import org.neo4j.neometa.structure.DatatypeClassRange;
import org.neo4j.neometa.structure.MetaStructureClassRange;
import org.neo4j.neometa.structure.MetaStructureClass;
import org.neo4j.neometa.structure.MetaStructureProperty;

public class MetaProperty extends MetaObject<MetaStructureProperty>
{
	MetaProperty( MetaModel model, MetaStructureProperty metaProperty )
	{
		super( model, metaProperty );
	}
	
	public MetaClass getOwner()
	{
		MetaStructureClass metaClass =
			getThing().associatedMetaClasses().iterator().next();
		return new MetaClass( model(), metaClass );
	}
	
	public void setFundamentalValueType( Class<?> cls )
	{
		getThing().setRange( new DatatypeClassRange( cls ) );
	}
	
	public void setMetaClassValueType( MetaClass cls )
	{
		getThing().setRange( new MetaStructureClassRange( cls.getThing() ) );
	}
}
