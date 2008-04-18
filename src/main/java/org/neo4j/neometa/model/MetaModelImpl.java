package org.neo4j.neometa.model;

import java.util.Collection;

import org.neo4j.api.core.NeoService;
import org.neo4j.neometa.structure.MetaStructure;
import org.neo4j.neometa.structure.MetaStructureClass;
import org.neo4j.neometa.structure.MetaStructureImpl;

/**
 * An object oriented API to the {@link MetaStructure} interface where
 * properties isn't entities of their own.
 */
public class MetaModelImpl implements MetaModel
{
	private MetaStructure meta;
	
	/**
	 * @param neo the {@link NeoService} to use in this model.
	 */
	public MetaModelImpl( NeoService neo )
	{
		this.meta = new MetaStructureImpl( neo );
	}
	
	protected MetaStructure meta()
	{
		return this.meta;
	}
	
	protected NeoService neo()
	{
		return ( ( MetaStructureImpl ) meta() ).neo();
	}
	
	public MetaClass getMetaClass( String name, boolean allowCreate )
	{
		MetaStructureClass cls = meta().getGlobalNamespace().getMetaClass(
			name, allowCreate );
		if ( cls == null )
		{
			return null;
		}
		return new MetaClass( this, cls );
	}
	
	public Collection<MetaClass> getMetaClasses()
	{
		return new MetaObjectCollection.MetaClassCollection( this,
			meta().getGlobalNamespace().getMetaClasses() );
	}
}
