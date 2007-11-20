package org.neo4j.meta;

import java.util.Arrays;

import junit.framework.TestCase;

import org.neo4j.api.core.Direction;
import org.neo4j.api.core.EmbeddedNeo;
import org.neo4j.api.core.NeoService;
import org.neo4j.api.core.Transaction;

public class SimpleTest extends TestCase
{
	private NeoService embeddedNeo = null;
	private MetaManager metaManager = null;
	
	private NeoService getNeo()
	{
		return this.embeddedNeo;
	}
	
	private MetaManager getMetaManager()
	{
		return this.metaManager;
	}
	
	public static void main( String[] args )
	{
		SimpleTest me = new SimpleTest();
		try
		{
			me.setUp();
			me.testCreateAndDeleteNodeType();
			me.testRequiredProperties();
			me.testAllowedRelationships();
			me.testSuperTypes();
		}
		finally
		{
			me.tearDown();
		}
	}
	
	@Override
	public void setUp()
	{
		this.embeddedNeo = new EmbeddedNeo( "var" );
		this.metaManager = new MetaManager( getNeo() );
	}
	
	@Override
	public void tearDown()
	{
		if ( getNeo() != null )
		{
			getNeo().shutdown();
		}
	}
	
	public void testCreateAndDeleteNodeType()
	{
		String name = "UnitTest";
		Transaction tx = Transaction.begin();
		try
		{
			// Create and assert
			NodeType newNodeType = getMetaManager().createNodeType( name );
			assertNotNull( newNodeType );
			assertEquals( newNodeType.getName(), name );
			assertTrue( getMetaManager().getNodeTypes().contains(
				newNodeType ) );
			assertEquals( newNodeType,
				getMetaManager().getNodeTypeByName( name ) );
			
			// Delete and assert
			try
			{
				newNodeType.delete();			
				getMetaManager().getNodeTypeByName( name );
				fail( "We shouldn't be able to get a deleted NodeType" );
			}
			catch ( IllegalArgumentException iae )
			{
				// Ok
			}
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	public void testRequiredProperties()
	{
		String typeName = "UnitTest1";
		String propertyKey = "UnitTestPropName";
		Transaction tx = Transaction.begin();
		try
		{
			NodeType newNodeType = getMetaManager().createNodeType( typeName );
		
			// Add and assert key
			newNodeType.addRequiredProperty( propertyKey );
			assertTrue( newNodeType.getRequiredProperties().iterator().
				hasNext() );
			assertEquals( propertyKey,
				newNodeType.getRequiredProperties().iterator().next().getKey());
			assertNotNull( newNodeType.getRequiredProperty( propertyKey ) );

			// Test value types
			this.doPropertyValueTypeTest( newNodeType, boolean.class );
			this.doPropertyValueTypeTest( newNodeType, Boolean.class );
			this.doPropertyValueTypeTest( newNodeType, byte.class );
			this.doPropertyValueTypeTest( newNodeType, Byte.class );
			this.doPropertyValueTypeTest( newNodeType, short.class );
			this.doPropertyValueTypeTest( newNodeType, Short.class );
			this.doPropertyValueTypeTest( newNodeType, int.class );
			this.doPropertyValueTypeTest( newNodeType, Integer.class );
			this.doPropertyValueTypeTest( newNodeType, long.class );
			this.doPropertyValueTypeTest( newNodeType, Long.class );
			this.doPropertyValueTypeTest( newNodeType, float.class );
			this.doPropertyValueTypeTest( newNodeType, Float.class );
			this.doPropertyValueTypeTest( newNodeType, double.class );
			this.doPropertyValueTypeTest( newNodeType, Double.class );
			this.doPropertyValueTypeTest( newNodeType, String.class );
			
			// Remove and assert
			newNodeType.removeRequiredProperty(
				newNodeType.getRequiredProperty( propertyKey ) );
			assertFalse( newNodeType.getRequiredProperties().iterator().
				hasNext() );
			assertNull( newNodeType.getRequiredProperty( propertyKey ) );
			
			// Clean up
			newNodeType.delete();
			
			tx.success();
		}
		finally
		{
			tx.finish();
		}		
	}
	
	public void testAllowedRelationships()
	{
		String firstTypeName = "UnitTestRelTestType1";
		String secondTypeName = "UnitTestRelTestType2";
		String relationshipTypeName = "A_RELATIONSHIP_THAT_BINDS_THEM";
		Transaction tx = Transaction.begin();
		try
		{
			// Create types
			NodeType firstType = getMetaManager().createNodeType(
				firstTypeName );
			NodeType secondType = getMetaManager().createNodeType(
				secondTypeName );
		
			// Add and assert relationship
			MetaRelationship relationship = firstType.addAllowedRelationship(
				relationshipTypeName, Direction.OUTGOING, secondType );
			assertEquals( relationship, firstType.getAllowedRelationship(
				relationshipTypeName, Direction.OUTGOING ) );
			assertEquals( relationship, secondType.getAllowedRelationship(
				relationshipTypeName, Direction.INCOMING ) );
			assertEquals(
				firstType.getAllowedRelationship(
					relationshipTypeName, Direction.OUTGOING ),
				secondType.getAllowedRelationship(
					relationshipTypeName, Direction.INCOMING ) );
			
			// Clean up
			firstType.delete();
			secondType.delete();
			
			tx.success();
		}
		finally
		{
			tx.finish();
		}		
	}
	
	private void doPropertyValueTypeTest( NodeType nodeType,
		Class<?> valueClass )
	{
		String propertyKey = "UnitTestPropertyFor" + valueClass.getName(); 
		MetaProperty property = nodeType.addRequiredProperty( propertyKey );
		property.setValueType( valueClass );
		assertEquals( "Value type " + valueClass.getName() + " failed",
			valueClass, property.getValueType() );
		nodeType.removeRequiredProperty( property );
	}
	
	public void testSuperTypes()
	{
		String nodeTypeName = "UnitTestType";
		String subTypeName = "UnitTestSubType";
		String anotherSubTypeName = "UnitTestAnotherSubType";
		String subSubTypeName = "UnitTestSubSubType";
		String superPropertyKey = "UnitTestSuperPropertyKey";
		String subPropertyKey = "UnitTestSubPropertyKey";
		String anotherSubPropertyKey = "UnitTestAnotherSubPropertyKey";
		
		Transaction tx = Transaction.begin();
		try
		{
			// Create a simple type hierarchy
			NodeType superType = getMetaManager().createNodeType(
				nodeTypeName );
			NodeType subType = getMetaManager().createNodeType( subTypeName );
			superType.directSubTypes().add( subType );
			
			// Assert superTypes(), both ways
			assertTrue( superType.directSuperTypes().isEmpty() );
			assertFalse( subType.directSuperTypes().isEmpty() );
			assertEquals( 1, subType.directSuperTypes().size() );
			assertTrue( subType.directSuperTypes().contains( superType ) );
			
			// Assert subTypes(), both ways
			assertFalse( superType.directSubTypes().isEmpty() );
			assertTrue( subType.directSubTypes().isEmpty() );
			assertEquals( 1, superType.directSubTypes().size() );
			assertTrue( superType.directSubTypes().contains( subType ) );
			
			// Assert isSubTypeOf(), both ways
			assertEquals( superType,
				subType.directSuperTypes().iterator().next() );
			assertTrue( subType.isSubTypeOf( superType ) );
			assertFalse( superType.isSubTypeOf( subType ) );
			assertFalse( subType.isSubTypeOf( subType ) );			
			
			// Extend the type hierarchy
			NodeType anotherSubType = getMetaManager().createNodeType(
				anotherSubTypeName );
			NodeType subSubType = getMetaManager().createNodeType(
				subSubTypeName );
			superType.directSubTypes().add( anotherSubType );
			subType.directSubTypes().add( subSubType );
			
			// Assert {super,sub}Types()
			assertEquals( 2, superType.directSubTypes().size() );
			assertEquals( 1, subType.directSuperTypes().size() );
			assertEquals( 1, subType.directSubTypes().size() );
			assertEquals( 1, anotherSubType.directSuperTypes().size() );
			assertEquals( 0, anotherSubType.directSubTypes().size() );			
			assertTrue( superType.directSubTypes().containsAll(
				Arrays.asList( new NodeType[] { subType, anotherSubType } ) ) );
			assertEquals( superType,
				subSubType.directSuperTypes().iterator().next().
					directSuperTypes().iterator().next() );
			
			// Assert isSubTypeOf()
			assertTrue( subSubType.isSubTypeOf( superType ) );
			assertFalse( subSubType.isSubTypeOf( anotherSubType ) );
			
			// Inject property constraint at the super level, assert that it
			// propagates
			MetaProperty superProperty = superType.addRequiredProperty(
				superPropertyKey );
			assertEquals( superProperty,
				subType.getRequiredProperty( superPropertyKey ) );
			assertEquals( superProperty,
				subSubType.getRequiredProperty( superPropertyKey ) );
			
			// Inject property constraints at the first level of subclasses,
			// assert that they propagate correctly
			MetaProperty subProperty = subType.addRequiredProperty(
				subPropertyKey );
			anotherSubType.addRequiredProperty( anotherSubPropertyKey );
			assertEquals( subProperty,
				subSubType.getRequiredProperty( subPropertyKey ) );
			assertNull( anotherSubType.getRequiredProperty( subPropertyKey ) );
			assertNull( subSubType.getRequiredProperty(
				anotherSubPropertyKey ) );
			
			// Clean up
			superType.delete();
			subType.delete();
			anotherSubType.delete();
			subSubType.delete();
			tx.success();
		}
		finally
		{
			tx.finish();
		}		
	}	
}
