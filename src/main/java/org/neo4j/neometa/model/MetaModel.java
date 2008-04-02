package org.neo4j.neometa.model;

import java.util.Collection;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.NeoService;
import org.neo4j.neometa.structure.MetaStructure;
import org.neo4j.neometa.structure.MetaStructureClass;
import org.neo4j.neometa.structure.MetaStructureRelTypes;

/**
 * An object oriented API to the {@link MetaStructure} interface where
 * properties isn't entities of their own.
 */
public class MetaModel
{
	private MetaStructure meta;
	
	/**
	 * @param neo the {@link NeoService} to use in this model.
	 */
	public MetaModel( NeoService neo )
	{
		this.meta = new MetaStructure( neo );
	}
	
	protected MetaStructure meta()
	{
		return this.meta;
	}
	
	/**
	 * Returns (and optionally creates) a {@link MetaClass} instance
	 * with the given {@code name}.
	 * @param name the name of the class.
	 * @param allowCreate if {@code true} and no class be the given {@code name}
	 * exists then it is created.
	 * @return the {@link MetaClass} with the given {@code name}.
	 */
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
	
	/**
	 * @return a modifiable collection of all the classes in this meta model. 
	 */
	public Collection<MetaClass> getMetaClasses()
	{
		return new MetaObjectCollection<MetaClass>(
			meta.getGlobalNamespace().node(),
			MetaStructureRelTypes.META_CLASS, Direction.OUTGOING, this,
			MetaClass.class );
	}
}
