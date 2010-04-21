package org.neo4j.meta.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Relationship;

/**
 * An implementation of {@link InstanceRange} for values which are instances
 * of {@link MetaModelClass}.
 */
public class InstanceEnumerationRange extends InstanceRange
{
	private Set<Node> rangeNodes;
	
	/**
	 * @param rangeNodes the classes the value has to comply to.
	 */
	public InstanceEnumerationRange( Node... rangeNodes )
	{
		this.rangeNodes = new HashSet<Node>(
			Arrays.asList( rangeNodes ) );
	}
	
	/**
	 * Internal usage.
	 */
	public InstanceEnumerationRange()
	{
	}
	
	/**
	 * @return the set nodes.
	 */
	public Node[] getRangeNodes()
	{
		return this.rangeNodes.toArray(new Node[ rangeNodes.size() ] );
	}
	
	
	@Override
	protected void internalStore( MetaModelRestrictable<InstanceRange> owner )
	{
		for ( Node instance : this.rangeNodes )
		{
			owner.node().createRelationshipTo( instance,
				MetaModelRelTypes.META_HAS_INSTANCE );
		}
	}
	
	private Iterable<Relationship> getRelationships(
			MetaModelRestrictable<InstanceRange> owner )
	{
		return owner.node().getRelationships(
			MetaModelRelTypes.META_HAS_INSTANCE,
			Direction.OUTGOING );
	}
	
	@Override
	protected void internalLoad( MetaModelRestrictable<InstanceRange> owner )
	{
		this.rangeNodes = new HashSet<Node>();
		for ( Relationship rel : getRelationships( owner ) )
		{
			this.rangeNodes.add( rel.getEndNode() ) ;
		}
	}
	
	@Override
	protected void internalRemove( MetaModelRestrictable<InstanceRange> owner )
	{
		for ( Relationship rel : getRelationships( owner ) )
		{
			rel.delete();
		}
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + StringUtil.join( ", ",
			rangeNodes.toArray(
				new Node[ rangeNodes.size() ] ) ) + "]";
	}
}
