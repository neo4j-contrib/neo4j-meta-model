package org.neo4j.neometa.structure;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.Relationship;
import org.neo4j.api.core.RelationshipType;
import org.neo4j.api.core.Transaction;

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
	protected void internalStore( MetaStructureProperty property )
	{
		Transaction tx = property.neo().beginTx();
		try
		{
			for ( MetaStructureClass cls : this.rangeClasses )
			{
				property.node().createRelationshipTo( cls.node(),
					MetaStructureRelTypes.META_PROPERTY_HAS_RANGE );
			}
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	@Override
	protected void internalLoad( MetaStructureProperty property )
	{
		Transaction tx = property.neo().beginTx();
		try
		{
			this.rangeClasses = new HashSet<MetaStructureClass>();
			for ( Relationship rel : property.node().getRelationships(
				MetaStructureRelTypes.META_PROPERTY_HAS_RANGE,
				Direction.OUTGOING ) )
			{
				this.rangeClasses.add( new MetaStructureClass( property.meta(),
					rel.getEndNode() ) );
			}
			tx.success();
		}
		finally
		{
			tx.finish();
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
