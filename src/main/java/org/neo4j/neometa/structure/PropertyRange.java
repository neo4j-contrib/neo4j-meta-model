package org.neo4j.neometa.structure;

import java.text.ParseException;

import org.neo4j.api.core.Node;
import org.neo4j.api.core.Transaction;

/**
 * The range of a property, i.e. a properties expected value type. F.ex. it
 * could be a String, an integer or a {@link MetaStructureClass} (which
 * would refer to a {@link Node} which has a relationship to that class).
 */
public abstract class PropertyRange
{
	static final String KEY_RANGE_TYPE = "range_type";
	
	protected void store( MetaStructureProperty property )
	{
		Transaction tx = property.neo().beginTx();
		try
		{
			property.node().setProperty( KEY_RANGE_TYPE, getClass().getName() );
			internalStore( property );
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	protected abstract void internalStore( MetaStructureProperty property );
	
	protected abstract void internalLoad( MetaStructureProperty property );
	
	protected static PropertyRange loadRange( MetaStructureProperty property )
	{
		Transaction tx = property.neo().beginTx();
		try
		{
			String rangeType = ( String ) property.node().getProperty(
				KEY_RANGE_TYPE );
			Class<?> cls = Class.forName( rangeType );
			PropertyRange result = ( PropertyRange ) cls.newInstance();
			result.internalLoad( property );
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
}
