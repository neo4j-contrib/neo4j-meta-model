package org.neo4j.neometa.structure;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.NotFoundException;
import org.neo4j.api.core.Transaction;
import org.neo4j.util.NeoUtil;

/**
 * The access point of a meta model. Is given a root node where all the
 * namespaces, properties and classes are stored/read underneath.
 */
public class MetaStructureImpl implements MetaStructure
{
	private NeoService neo;
	private NeoUtil neoUtil;
	private DynamicMetaRelTypes dynamicRelTypes = new DynamicMetaRelTypes();
	
	private Map<String, MetaStructureNamespace> namespaceCache =
		Collections.synchronizedMap(
			new HashMap<String, MetaStructureNamespace>() );
	
	/**
	 * @param neo the {@link NeoService} used for this meta model.
	 */
	public MetaStructureImpl( NeoService neo )
	{
		this.neo = neo;
		this.neoUtil = new NeoUtil( neo );
		this.dynamicRelTypes = new DynamicMetaRelTypes();
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
	
	protected DynamicMetaRelTypes dynamicRelTypes()
	{
		return this.dynamicRelTypes;
	}
	
	public MetaStructureNamespace getNamespace( String name,
		boolean allowCreate )
	{
		assert name != null;
		return findOrCreateInCollection( getNamespaces(), name, allowCreate,
			MetaStructureNamespace.class, namespaceCache );
	}
	
	public MetaStructureNamespace getGlobalNamespace()
	{
		return findOrCreateInCollection( getNamespaces(), null, true,
			MetaStructureNamespace.class, namespaceCache );
	}
	
	protected <T extends MetaStructureObject> T findOrCreateInCollection(
		Collection<T> collection, String nameOrNullForGlobal,
		boolean allowCreate, Class<T> theClass, Map<String, T> cacheOrNull )
	{
		Transaction tx = neo().beginTx();
		try
		{
			T foundItem = safeGetFromCache( cacheOrNull, nameOrNullForGlobal );
			if ( foundItem != null )
			{
				tx.success();
				return foundItem;
			}
			
			for ( T item : collection )
			{
				String theName = item.getName();
				if ( nameOrNullForGlobal == null || theName == null )
				{
					if ( nameOrNullForGlobal == null && theName == null )
					{
						foundItem = item;
						break;
					}
				}
				else if ( theName.equals( nameOrNullForGlobal ) )
				{
					foundItem = item;
					break;
				}
			}
			if ( foundItem != null )
			{
				if ( cacheOrNull != null )
				{
					cacheOrNull.put( nameOrNullForGlobal, foundItem );
				}
				return foundItem;
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
	
	private <T extends MetaStructureObject> T safeGetFromCache(
		Map<String, T> cacheOrNull, String key )
	{
		T result = null;
		if ( cacheOrNull != null )
		{
			result = cacheOrNull.get( key );
			if ( result != null )
			{
				try
				{
					neo().getNodeById( result.node().getId() );
				}
				catch ( NotFoundException e )
				{
					cacheOrNull.remove( result );
					result = null;
				}
			}
		}
		return result;
	}
	
	public Collection<MetaStructureNamespace> getNamespaces()
	{
		return new MetaStructureObjectCollection<MetaStructureNamespace>(
			neo(), rootNode(), MetaStructureRelTypes.META_NAMESPACE,
			Direction.OUTGOING, this, MetaStructureNamespace.class );
	}
	
	public <T> T lookup( MetaStructureProperty property, LookerUpper<T> finder,
		MetaStructureClass... classes )
	{
		// TODO Maybe add some form of caching here since this method will be
		// HEAVILY used. It's the main way of looking things up in the meta
		// model, f.ex. validation and conversion of values a.s.o.
		
		Transaction tx = neo().beginTx();
		try
		{
			T result = LookupUtil.lookup( property, finder, classes );
			tx.success();
			return result;
		}
		finally
		{
			tx.finish();
		}
	}
}
