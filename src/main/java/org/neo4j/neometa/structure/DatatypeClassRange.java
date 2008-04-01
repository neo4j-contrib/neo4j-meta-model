package org.neo4j.neometa.structure;

/**
 * Represents a {@link PropertyRange} where the values are of a data type,
 * f.ex. a string, a number, a date or something similar.
 */
public class DatatypeClassRange extends SimpleStringPropertyRange
{
	private Class<?> rangeClass;
	
	/**
	 * @param rangeClass the expected value type.
	 */
	public DatatypeClassRange( Class<?> rangeClass )
	{
		this.rangeClass = rangeClass;
	}
	
	/**
	 * Used internally.
	 */
	public DatatypeClassRange()
	{
	}
	
	/**
	 * @return the value type.
	 */
	public Class<?> getRangeClass()
	{
		return this.rangeClass;
	}
	
	@Override
	protected String toStringRepresentation( MetaStructureProperty property )
	{
		return this.rangeClass.getName();
	}
	
	@Override
	protected void fromStringRepresentation( MetaStructureProperty property,
		String stringRepresentation )
	{
		try
		{
			this.rangeClass = Class.forName( stringRepresentation );
		}
		catch ( Exception e )
		{
			throw new RuntimeException( e );
		}
	}
	
	@Override
	public Object rdfLiteralToJavaObject( String value )
	{
		Object result = null;
		if ( rangeClass.equals( String.class ) )
		{
			result = value;
		}
		else
		{
			try
			{
				result = rangeClass.getConstructor( String.class ).newInstance(
					value );
			}
			catch ( Exception e )
			{
				throw new RuntimeException( e );
			}
		}
		return result;
	}
	
	@Override
	public String javaObjectToRdfLiteral( Object value )
	{
		return value.toString();
	}
}
