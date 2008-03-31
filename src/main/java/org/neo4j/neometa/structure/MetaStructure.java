package org.neo4j.neometa.structure;

import java.util.Collection;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Transaction;
import org.neo4j.util.NeoUtil;

public class MetaStructure
{
	private NeoService neo;
	private NeoUtil neoUtil;
	private Node rootNode;
	
	public MetaStructure( NeoService neo, Node rootNode )
	{
		this.neo = neo;
		this.rootNode = rootNode;
		this.neoUtil = new NeoUtil( neo );
	}
	
	public NeoService neo()
	{
		return this.neo;
	}
	
	protected NeoUtil neoUtil()
	{
		return this.neoUtil;
	}
	
	protected Node rootNode()
	{
		return this.rootNode;
	}
	
	public MetaStructureNamespace getNamespace( String name,
		boolean allowCreate )
	{
		assert name != null;
		return findOrCreateInCollection( getNamespaces(), name, allowCreate,
			MetaStructureNamespace.class );
	}
	
	public MetaStructureNamespace getGlobalNamespace()
	{
		return findOrCreateInCollection( getNamespaces(), null, true,
			MetaStructureNamespace.class );
	}
	
	protected <T extends MetaStructureObject> T findOrCreateInCollection(
		Collection<T> collection, String nameOrNullForGlobal,
		boolean allowCreate, Class<T> theClass )
	{
		Transaction tx = neo().beginTx();
		try
		{
			for ( T item : collection )
			{
				String theName = item.getName();
				if ( nameOrNullForGlobal == null || theName == null )
				{
					if ( nameOrNullForGlobal == null && theName == null )
					{
						return item;
					}
				}
				else if ( theName.equals( nameOrNullForGlobal ) )
				{
					return item;
				}
			}
			
			if ( !allowCreate )
			{
				return null;
			}
			Node node = neo().createNode();
			T item = null;
			try
			{
				item = theClass.getConstructor( MetaStructure.class,
					Node.class ).newInstance( this, node );
			}
			catch ( Exception e )
			{
				throw new RuntimeException( e );
			}
			if ( nameOrNullForGlobal != null )
			{
				item.setName( nameOrNullForGlobal );
			}
			collection.add( item );
			tx.success();
			return item;
		}
		finally
		{
			tx.finish();
		}
	}
	
	public Collection<MetaStructureNamespace> getNamespaces()
	{
		return new MetaStructureObjectCollection<MetaStructureNamespace>(
			rootNode, MetaStructureRelTypes.META_NAMESPACE, Direction.OUTGOING,
			this, MetaStructureNamespace.class );
	}
}
