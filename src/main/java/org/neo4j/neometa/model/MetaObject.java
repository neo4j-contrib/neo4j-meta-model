package org.neo4j.neometa.model;

import org.neo4j.api.core.NeoService;
import org.neo4j.neometa.structure.MetaStructure;
import org.neo4j.neometa.structure.MetaStructureThing;

abstract class MetaObject<T extends MetaStructureThing>
{
	private MetaModel model;
	private T metaThing;
	
	MetaObject( MetaModel model, T metaThing )
	{
		this.model = model;
		this.metaThing = metaThing;
	}
	
	protected MetaModel model()
	{
		return this.model;
	}
	
	protected MetaStructure meta()
	{
		return ( ( MetaModelImpl ) this.model ).meta();
	}
	
	protected NeoService neo()
	{
		return ( ( MetaModelImpl ) this.model ).neo();
	}
	
	protected T getThing()
	{
		return this.metaThing;
	}
	
	/**
	 * @return the name of this object.
	 */
	public String getName()
	{
		return getThing().getName();
	}
	
	@Override
	public int hashCode()
	{
		return getThing().hashCode();
	}
	
	@Override
	public boolean equals( Object o )
	{
		return o != null && getThing().equals(
			( ( MetaObject ) o ).getThing() );
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + getName() + "]";
	}
}
