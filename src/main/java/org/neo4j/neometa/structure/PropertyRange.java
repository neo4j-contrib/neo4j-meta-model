package org.neo4j.neometa.structure;

import java.text.ParseException;

import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Transaction;

/**
 * The range of a property, i.e. a properties expected value type. F.ex. it
 * could be a String, an integer or a {@link MetaStructureClass} (which
 * would refer to a {@link Node} which has a relationship to that class).
 */
public abstract class PropertyRange
{
	static final String KEY_RANGE_IMPL = "range_implementation_class";
	
	private MetaStructureRestrictable owner;
	
	protected MetaStructureRestrictable getOwner()
	{
		return this.owner;
	}
	
	private static NeoService neo( MetaStructure meta )
	{
		return ( ( MetaStructureImpl ) meta ).neo();
	}
	
	protected void store( MetaStructureRestrictable owner )
	{
		// MP: This isn't very good, should be in the constructor, but we can't
		// really trust the developer to supply gthe correct property instance.
		// So we do this internally when the MetaStructureProperty#setRange
		// method is called. Possible cause of bugs/errors.
		this.owner = owner;
		
		Transaction tx = neo( owner.meta() ).beginTx();
		try
		{
			removeRange( owner );
			owner.node().setProperty( KEY_RANGE_IMPL, getClass().getName() );
			internalStore( owner );
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	protected static void removeRange( MetaStructureRestrictable owner )
	{
		PropertyRange range = loadRange( owner );
		if ( range != null )
		{
			owner.node().removeProperty( KEY_RANGE_IMPL );
			range.internalRemove( owner );
		}
	}
	
	protected abstract void internalStore( MetaStructureRestrictable owner );
	
	protected abstract void internalRemove( MetaStructureRestrictable owner );
	
	protected abstract void internalLoad( MetaStructureRestrictable owner );
	
	protected static PropertyRange loadRange( MetaStructureRestrictable owner )
	{
		Transaction tx = neo( owner.meta() ).beginTx();
		try
		{
			String rangeType = ( String ) owner.node().getProperty(
				KEY_RANGE_IMPL, null );
			if ( rangeType == null )
			{
				return null;
			}
			Class<?> cls = Class.forName( rangeType );
			PropertyRange result = ( PropertyRange ) cls.newInstance();
			result.owner = owner;
			result.internalLoad( owner );
			tx.success();
			return result;
		}
		catch ( Exception e )
		{
			throw new RuntimeException( e );
		}
		finally
		{
			tx.finish();
		}
	}
	
	protected static void setOrRemoveRange( MetaStructureRestrictable owner,
		PropertyRange range )
	{
		Transaction tx = neo( owner.meta() ).beginTx();
		try
		{
			PropertyRange.removeRange( owner );
			if ( range != null )
			{
				range.store( owner );
			}
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	/**
	 * Performs a string-to-object conversion to a fundamental value, f.ex.
	 * a string, a date or a number of some sort.
	 * @param value the plain literal value to convert into a real java object.
	 * @return the converted value.
	 * @throws ParseException if the {@code value} has some format error of
	 * some sort.
	 */
	public abstract Object rdfLiteralToJavaObject( String value )
		throws ParseException;
	
	/**
	 * Performs a object-to-string conversion from a fundamental value, f.ex.
	 * a string, a date or a number of some sort.
	 * @param value the object to get the string representation of.
	 * @return the string representation of {@code value}.
	 */
	public abstract String javaObjectToRdfLiteral( Object value );
	
	/**
	 * @return wether the expected value of this property range is a datatype,
	 * i.e. plain fundamental values.
	 */
	public abstract boolean isDatatype();
}
