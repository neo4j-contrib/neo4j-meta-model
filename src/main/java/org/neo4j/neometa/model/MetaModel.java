package org.neo4j.neometa.model;

import java.util.Collection;

import org.neo4j.neometa.structure.MetaStructure;

/**
 * An object oriented API to the {@link MetaStructure} interface where
 * properties isn't entities of their own.
 */
public interface MetaModel
{
	/**
	 * Returns (and optionally creates) a {@link MetaClass} instance
	 * with the given {@code name}.
	 * @param name the name of the class.
	 * @param allowCreate if {@code true} and no class be the given {@code name}
	 * exists then it is created.
	 * @return the {@link MetaClass} with the given {@code name}.
	 */
	MetaClass getMetaClass( String name, boolean allowCreate );
	
	/**
	 * @return a modifiable collection of all the classes in this meta model. 
	 */
	Collection<MetaClass> getMetaClasses();
}
