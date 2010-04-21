/*
 * Copyright  2003-2005 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.neo4j.meta.model;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

/**
 * An implementation of the standard xml schema date format:
 * http://www.w3.org/2001/XMLSchema#dateTime as a {@link DateFormat}.
 */
public class XmlSchemaDateFormat extends DateFormat
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6706720726042964472L;
	
	private static final DateFormat DATEFORMAT_XSD_ZULU = new SimpleDateFormat(
	    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" );
	
	static
	{
		DATEFORMAT_XSD_ZULU.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
	}
	
	@Override
	public Date parse( String src, ParsePosition position )
	{
		Date date = null;
		
		// validate fixed portion of format
		int index = 0;
		try
		{
			if ( src != null )
			{
				if ( ( src.charAt( 0 ) == '+' ) || ( src.charAt( 0 ) == '-' ) )
				{
					src = src.substring( 1 );
				}
				
				if ( src.length() < 19 )
				{
					position.setIndex( src.length() - 1 );
					handleParseError( position, "TOO_FEW_CHARS" );
				}
				validateChar( src, position, index = 4, '-', "EXPECTED_DASH" );
				validateChar( src, position, index = 7, '-', "EXPECTED_DASH" );
				validateChar( src, position, index = 10, 'T',
				    "EXPECTED_CAPITAL_T" );
				validateChar( src, position, index = 13, ':',
				    "EXPECTED_COLON_IN_TIME" );
				validateChar( src, position, index = 16, ':',
				    "EXPECTED_COLON_IN_TIME" );
			}
			
			// convert what we have validated so far
			try
			{
				date = DATEFORMAT_XSD_ZULU.parse( ( src == null ) ?
					null : ( src.substring( 0, 19 ) + ".000Z" ) );
			}
			catch ( Exception e )
			{
				throw new NumberFormatException( e.toString() );
			}
			
			index = 19;
			
			// parse optional milliseconds
			if ( src != null )
			{
				if ( ( index < src.length() ) &&
					( src.charAt( index ) == '.' ) )
				{
					int milliseconds = 0;
					int start = ++index;
					
					while ( ( index < src.length() )
					    && Character.isDigit( src.charAt( index ) ) )
					{
						index++;
					}
					
					String decimal = src.substring( start, index );
					
					if ( decimal.length() == 3 )
					{
						milliseconds = Integer.parseInt( decimal );
					}
					else if ( decimal.length() < 3 )
					{
						milliseconds = Integer.parseInt( ( decimal + "000" )
						    .substring( 0, 3 ) );
					}
					else
					{
						milliseconds = Integer.parseInt( decimal.substring( 0,
						    3 ) );
						
						if ( decimal.charAt( 3 ) >= '5' )
						{
							++milliseconds;
						}
					}
					
					// add milliseconds to the current date
					date.setTime( date.getTime() + milliseconds );
				}
				
				// parse optional timezone
				if ( ( ( index + 5 ) < src.length() )
				    && ( ( src.charAt( index ) == '+' ) ||
				    	( src.charAt( index ) == '-' ) ) )
				{
					validateCharIsDigit( src, position, index + 1,
					    "EXPECTED_NUMERAL" );
					validateCharIsDigit( src, position, index + 2,
					    "EXPECTED_NUMERAL" );
					validateChar( src, position, index + 3, ':',
					    "EXPECTED_COLON_IN_TIMEZONE" );
					validateCharIsDigit( src, position, index + 4,
					    "EXPECTED_NUMERAL" );
					validateCharIsDigit( src, position, index + 5,
					    "EXPECTED_NUMERAL" );
					
					final int hours = ( ( ( src.charAt( index + 1 ) - '0' ) *
						10 ) + src.charAt( index + 2 ) ) - '0';
					final int mins = ( ( ( src.charAt( index + 4 ) - '0' ) *
						10 ) + src.charAt( index + 5 ) ) - '0';
					int millisecs = ( ( hours * 60 ) + mins ) * 60 * 1000;
					
					// subtract millisecs from current date to obtain GMT
					if ( src.charAt( index ) == '+' )
					{
						millisecs = -millisecs;
					}
					
					date.setTime( date.getTime() + millisecs );
					index += 6;
				}
				
				if ( ( index < src.length() ) &&
					( src.charAt( index ) == 'Z' ) )
				{
					index++;
				}
				
				if ( index < src.length() )
				{
					handleParseError( position, "TOO_MANY_CHARS" );
				}
			}
		}
		catch ( ParseException pe )
		{
			// This tells DateFormat.parse() to throw a ParseException
			index = 0;
			position.setErrorIndex( index );
			date = null;
		}
		position.setIndex( index );
		return ( date );
	}
	
	@Override
	public StringBuffer format( Date date, StringBuffer buffer,
	    FieldPosition position )
	{
		String str = DATEFORMAT_XSD_ZULU.format( date );
		if ( buffer == null )
		{
			buffer = new StringBuffer();
		}
		
		buffer.append( str );
		return ( buffer );
	}
	
	private void validateChar( String str, ParsePosition parse_pos, int index,
	    char expected, String erorrReason ) throws ParseException
	{
		if ( str.charAt( index ) != expected )
		{
			handleParseError( parse_pos, erorrReason );
		}
	}
	
	private void validateCharIsDigit( String str, ParsePosition position,
	    int index, String errorReason ) throws ParseException
	{
		if ( !Character.isDigit( str.charAt( index ) ) )
		{
			handleParseError( position, errorReason );
		}
	}
	
	private void handleParseError( ParsePosition position, String errorReason )
	    throws ParseException
	{
		throw new ParseException( "INVALID_XSD_DATETIME", position
		    .getErrorIndex() );
	}
}