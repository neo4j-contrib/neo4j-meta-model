package org.neo4j.neometa.structure;

/**
 * Adds methods which the JDK is missing.
 */
public abstract class StringUtil
{
	/**
	 * Like the PHP function.
	 * @param <T> the type of values.
	 * @param delimiter the delimiter between values.
	 * @param items the items to join.
	 * @return the joined string.
	 */
	public static <T> String join( String delimiter, T... items )
	{
		StringBuffer buffer = new StringBuffer();
		for ( T item : items )
		{
			if ( buffer.length() > 0 )
			{
				buffer.append( delimiter );
			}
			buffer.append( item );
		}
		return buffer.toString();
	}
}
