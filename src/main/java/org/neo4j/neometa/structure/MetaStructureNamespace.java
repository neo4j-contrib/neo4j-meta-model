package org.neo4j.neometa.structure;

import java.util.Collection;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;

/**
 * Represents a namespace in the meta model structure. A namespace is useful
 * when there would be conflicting names of properties or classes.
 */
public class MetaStructureNamespace extends MetaStructureObject
{
	/**
	 * @param meta the {@link MetaStructure} instance.
	 * @param node the {@link Node} to wrap.
	 */
	public MetaStructureNamespace( MetaStructure meta, Node node )
	{
		super( meta, node );
	}
	
	/**
	 * Returns (and optionally creates) a {@link MetaStructureClass} instance
	 * (with underlying {@link Node}).
	 * @param name the name of the class.
	 * @param allowCreate if {@code true} and no class by the given {@code name}
	 * exists then it is created.
	 * @return the {@link MetaStructureClass} in this namespace with the given
	 * {@code name}.
	 */
	public MetaStructureClass getMetaClass( String name, boolean allowCreate )
	{
		return meta().findOrCreateInCollection( getMetaClasses(), name,
			allowCreate, MetaStructureClass.class );
	}
	
	/**
	 * @return a modifiable collection of all {@link MetaStructureClass}
	 * instances for this namespace.
	 */
	public Collection<MetaStructureClass> getMetaClasses()
	{
		return new MetaStructureObjectCollection<MetaStructureClass>( node(),
			MetaStructureRelTypes.META_CLASS, Direction.OUTGOING, meta(),
			MetaStructureClass.class );
	}
	
	/**
	 * Returns (and optionally creates) a {@link MetaStructureProperty} instance
	 * (with underlying {@link Node}).
	 * @param name the name of the property.
	 * @param allowCreate if {@code true} and no property by the given
	 * {@code name} exists then it is created.
	 * @return the {@link MetaStructureProperty} in this namespace with the
	 * given {@code name}.
	 */
	public MetaStructureProperty getMetaProperty( String name,
		boolean allowCreate )
	{
		return meta().findOrCreateInCollection( getMetaProperties(), name,
			allowCreate, MetaStructureProperty.class );
	}
	
	/**
	 * @return a modifiable collection of all {@link MetaStructureProperty}
	 * instances for this namespace.
	 */
	public Collection<MetaStructureProperty> getMetaProperties()
	{
		return new MetaStructureObjectCollection<MetaStructureProperty>( node(),
			MetaStructureRelTypes.META_PROPERTY, Direction.OUTGOING, meta(),
			MetaStructureProperty.class );
	}
}
