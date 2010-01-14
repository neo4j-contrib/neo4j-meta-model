package org.neo4j.meta.model;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.RelationshipType;

/**
 * A utility for creating dynamic {@link RelationshipType}s.
 */
public class DynamicMetaRelTypes
{
	private Map<String, RelationshipType> types =
		new HashMap<String, RelationshipType>();
	
	/**
	 * @param name the name of the relationship type.
	 * @return a {@link RelationshipType} instance with the name {@code name}.
	 * If no instance with the given {@code name} is found then it is created.
	 */
	public synchronized RelationshipType getOrCreateType( String name )
	{
		RelationshipType type = getType( name );
		if ( type == null )
		{
			type = new MetaRelType( name );
			types.put( name, type );
		}
		return type;
	}
	
	/**
	 * @param name the name of the relationship type.
	 * @return a {@link RelationshipType} instance with the name {@code name}.
	 * If no instance with the given {@code name} is found, {@code null}
	 * is returned.
	 */
	public synchronized RelationshipType getType( String name )
	{
		return types.get( name );
	}
	
	private static class MetaRelType implements RelationshipType
	{
		private String name;
		
		MetaRelType( String name )
		{
			this.name = name;
		}
		
		public String name()
		{
			return this.name;
		}
	}
}
