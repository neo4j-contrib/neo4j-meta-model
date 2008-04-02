package org.neo4j.neometa.structure;

import java.util.Collection;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Transaction;
import org.neo4j.util.NeoUtil;

/**
 * The access point of a meta model. Is given a root node where all the
 * namespaces, properties and classes are stored/read underneath.
 */
public class MetaStructure
{
	private NeoService neo;
	private NeoUtil neoUtil;
	
	/**
	 * @param neo the {@link NeoService} used for this meta model.
	 */
	public MetaStructure( NeoService neo )
	{
		this.neo = neo;
		this.neoUtil = new NeoUtil( neo );
	}
	
	/**
	 * @return the {@link NeoService} given in the constructor.
	 */
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
		return neoUtil().getOrCreateSubReferenceNode(
			MetaStructureRelTypes.REF_TO_META_SUBREF );
	}
	
	/**
	 * Returns (and optionally creates) a {@link MetaStructureNamespace}
	 * instance (with underlying {@link Node}).
	 * @param name the name for the namespace.
	 * @param allowCreate if {@code true} and no namespace by the given
	 * {@code name} exists then it is created.
	 * @return the {@link MetaStructureNamespace} in this namespace with the
	 * given {@code name}.
	 */
	public MetaStructureNamespace getNamespace( String name,
		boolean allowCreate )
	{
		assert name != null;
		return findOrCreateInCollection( getNamespaces(), name, allowCreate,
			MetaStructureNamespace.class );
	}
	
	/**
	 * @return the global namespace (without a name) which always exists.
	 * It's actually created on demand the first time. A call to
	 * {@link MetaStructureNamespace#getName()} will fail for this namespace.
	 */
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
	
	/**
	 * @return a modifiable collection of all {@link MetaStructureNamespace}
	 * instances for this meta model.
	 */
	public Collection<MetaStructureNamespace> getNamespaces()
	{
		return new MetaStructureObjectCollection<MetaStructureNamespace>(
			rootNode(), MetaStructureRelTypes.META_NAMESPACE,
			Direction.OUTGOING, this, MetaStructureNamespace.class );
	}
}
