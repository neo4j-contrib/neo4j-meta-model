package org.neo4j.neometa.structure;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.RelationshipType;

/**
 * An implementation of {@link PropertyRange} for values which are instances
 * of {@link MetaStructureClass}.
 */
public class MetaStructureClassRange extends PropertyRange
{
	private Set<MetaStructureClass> rangeClasses;
	
	/**
	 * @param rangeClasses the classes the value has to comply to.
	 */
	public MetaStructureClassRange( MetaStructureClass... rangeClasses )
	{
		this.rangeClasses = new HashSet<MetaStructureClass>(
			Arrays.asList( rangeClasses ) );
	}
	
	/**
	 * Internal usage.
	 */
	public MetaStructureClassRange()
	{
	}
	
	/**
	 * @return the set classes.
	 */
	public MetaStructureClass[] getRangeClasses()
	{
		return this.rangeClasses.toArray(
			new MetaStructureClass[ rangeClasses.size() ] );
	}
	
	/**
	 * TODO Explain better!
	 * @return the {@link RelationshipType} which should be created between
	 * a meta instance and the other meta instance.
	 */
	public RelationshipType getRelationshipTypeToUse()
	{
		return getOwner().meta().dynamicRelTypes().getOrCreateType(
			getOwner().getName() );
	}
	
	@Override
	protected void internalStore( MetaStructureRestrictable owner )
	{
		for ( MetaStructureClass cls : this.rangeClasses )
		{
			owner.node().createRelationshipTo( cls.node(),
				MetaStructureRelTypes.META_PROPERTY_HAS_RANGE );
		}
	}
	
	private Iterable<Relationship> getRelationships(
		MetaStructureRestrictable owner )
	{
		return owner.node().getRelationships(
			MetaStructureRelTypes.META_PROPERTY_HAS_RANGE,
			Direction.OUTGOING );
	}
	
	@Override
	protected void internalLoad( MetaStructureRestrictable owner )
	{
		this.rangeClasses = new HashSet<MetaStructureClass>();
		for ( Relationship rel : getRelationships( owner ) )
		{
			this.rangeClasses.add( new MetaStructureClass( owner.meta(),
				rel.getEndNode() ) );
		}
	}
	
	@Override
	protected void internalRemove( MetaStructureRestrictable owner )
	{
		for ( Relationship rel : getRelationships( owner ) )
		{
			rel.delete();
		}
	}

	@Override
	public Object rdfLiteralToJavaObject( String value )
	{
		throw new UnsupportedOperationException( "Should never be called" );
	}
	
	@Override
	public String javaObjectToRdfLiteral( Object value )
	{
		throw new UnsupportedOperationException( "Should never be called" );
	}
	
	@Override
	public boolean isDatatype()
	{
		return false;
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + StringUtil.join( ", ",
			rangeClasses.toArray(
				new MetaStructureClass[ rangeClasses.size() ] ) ) + "]";
	}
}
