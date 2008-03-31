package org.neo4j.neometa.structure;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.Transaction;
import org.neo4j.util.NeoRelationshipSet;

public class MetaStructureInstanceCollection extends NeoRelationshipSet<Node>
{
	private static final String KEY_COUNT = "instance_count";
	
	private MetaStructure meta;
	
	public MetaStructureInstanceCollection( Node node, MetaStructure meta )
	{
		super( node, MetaStructureRelTypes.META_INSTANCE_OF,
			Direction.INCOMING );
		this.meta = meta;
	}
	
	@Override
	protected Node getNodeFromItem( Object item )
	{
		return ( Node ) item;
	}
	
	@Override
	protected Node newObject( Node node, Relationship rel )
	{
		return node;
	}
	
	@Override
	public boolean add( Node item )
	{
		Transaction tx = meta.neo().beginTx();
		try
		{
			boolean result = super.add( item );
			if ( result )
			{
				int count = size() + 1;
				getUnderlyingNode().setProperty( KEY_COUNT, count );
			}
			tx.success();
			return result;
		}
		finally
		{
			tx.finish();
		}
	}
	
	@Override
	protected void removeItem( Relationship rel )
	{
		super.removeItem( rel );
		int count = size() - 1;
		getUnderlyingNode().setProperty( KEY_COUNT, count );
	}
	
	@Override
	public int size()
	{
		return ( ( Number ) meta.neoUtil().getProperty(
			getUnderlyingNode(), KEY_COUNT, 0 ) ).intValue();
	}
}
