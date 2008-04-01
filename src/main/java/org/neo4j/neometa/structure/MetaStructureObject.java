package org.neo4j.neometa.structure;

import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;

/**
 * A super class for basically all meta structure objects which wraps a
 * {@link Node}.
 */
public class MetaStructureObject
{
	private static final String KEY_NAME = "name";
	
	private MetaStructure meta;
	private Node node;
	
	MetaStructureObject( MetaStructure meta, Node node )
	{
		this.meta = meta;
		this.node = node;
	}
	
	protected MetaStructure meta()
	{
		return this.meta;
	}
	
	/**
	 * @return the {@link NeoService} instance used with this instance.
	 */
	public NeoService neo()
	{
		return meta().neo();
	}
	
	/**
	 * @return the {@link Node} which this object wraps.
	 */
	public Node node()
	{
		return this.node;
	}
	
	protected void setProperty( String key, Object value )
	{
		meta().neoUtil().setProperty( node(), key, value );
	}
	
	protected Object getProperty( String key )
	{
		return meta().neoUtil().getProperty( node(), key );
	}

	protected Object getProperty( String key, Object defaultValue )
	{
		return meta().neoUtil().getProperty( node(), key, defaultValue );
	}
	
	protected Object removeProperty( String key )
	{
		return meta().neoUtil().removeProperty( node(), key );
	}
	
	void setName( String name )
	{
		setProperty( KEY_NAME, name );
	}
	
	/**
	 * @return the name set for this object.
	 */
	public String getName()
	{
		return ( String ) getProperty( KEY_NAME, null );
	}
	
	@Override
	public int hashCode()
	{
		return node().hashCode();
	}
	
	@Override
	public boolean equals( Object o )
	{
		return o != null && getClass().equals( o.getClass() ) && node().equals(
			( ( MetaStructureObject ) o ).node() );
	}
}
