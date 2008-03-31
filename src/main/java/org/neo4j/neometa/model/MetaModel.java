package org.neo4j.neometa.model;

import java.util.Collection;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.neometa.structure.MetaStructure;
import org.neo4j.neometa.structure.MetaStructureClass;
import org.neo4j.neometa.structure.MetaStructureRelTypes;

public class MetaModel
{
	private MetaStructure meta;
	
	public MetaModel( NeoService neo, Node rootNode )
	{
		this.meta = new MetaStructure( neo, rootNode );
	}
	
	protected MetaStructure meta()
	{
		return this.meta;
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
		return new MetaObjectCollection<MetaClass>(
			meta.getGlobalNamespace().node(),
			MetaStructureRelTypes.META_CLASS, Direction.OUTGOING, this,
			MetaClass.class );
	}
}
