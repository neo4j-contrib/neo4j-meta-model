package org.neo4j.neometa.model;

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
		return this.model.meta();
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
}
