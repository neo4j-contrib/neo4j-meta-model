package org.neo4j.neometa.structure;

import org.neo4j.api.core.Node;
import org.neo4j.api.core.Transaction;
import org.neo4j.neometa.MetaTestCase;

public class TestOverall extends MetaTestCase
{
	public void testSome()
	{
		Transaction tx = neo().beginTx();
		try
		{
			txTestSome();
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	private void txTestSome()
	{
		Node rootNode = neo().createNode();
		MetaStructure structure = new MetaStructure( neo(), rootNode );
		
		assertEquals( 0, structure.getNamespaces().size() );
		MetaStructureNamespace namespace = structure.getGlobalNamespace();
		assertNull( namespace.getName() );
		assertEquals( 1, structure.getNamespaces().size() );
		assertEquals( namespace, structure.getGlobalNamespace() );
		
		assertNull( structure.getNamespace( "something",
			false ) );
		MetaStructureNamespace anotherNamespace = structure.getNamespace(
			"something", true );
		assertNotNull( anotherNamespace );
		
		assertEquals( 0, namespace.getMetaClasses().size() );
		assertEquals( 0, namespace.getMetaProperties().size() );
		assertNull( namespace.getMetaClass( "http://test#Thing", false ) );
		MetaStructureClass thingClass =
			namespace.getMetaClass( "http://test#Thing", true );
		assertNotNull( thingClass );
		assertNotNull( namespace.getMetaClass( "http://test#Thing", false ) );
		assertEquals( 1, namespace.getMetaClasses().size() );
		assertEquals( 0, namespace.getMetaProperties().size() );
		
		MetaStructureClass phoneClass =
			namespace.getMetaClass( "http://test#Phone", true );
		assertEquals( 2, namespace.getMetaClasses().size() );
		thingClass.getDirectSubs().add( phoneClass );
		MetaStructureProperty phoneTypeProperty =
			namespace.getMetaProperty( "http://test#phoneType", true );
		assertEquals( 1, namespace.getMetaProperties().size() );
		phoneClass.getProperties().add( phoneTypeProperty );
		MetaStructureProperty phoneNumberProperty =
			namespace.getMetaProperty( "http://test#phoneNumber", true );
		assertEquals( 2, namespace.getMetaProperties().size() );
		phoneClass.getProperties().add( phoneNumberProperty );
		
		MetaStructureClass personClass =
			namespace.getMetaClass( "http://test#Person", true );
		personClass.getDirectSupers().add( thingClass );
		MetaStructureProperty nameProperty =
			namespace.getMetaProperty( "http://test#name", true );
		nameProperty.setRange( new DatatypeClassRange( String.class ) );
		assertEquals( DatatypeClassRange.class,
			nameProperty.getRange().getClass() );
		assertEquals( String.class, ( ( DatatypeClassRange )
			nameProperty.getRange() ).getRangeClass() );
		MetaStructureProperty givenNameProperty =
			namespace.getMetaProperty( "http://test#givenName", true );
		nameProperty.getDirectSubs().add( givenNameProperty );
		MetaStructureProperty phoneProperty =
			namespace.getMetaProperty( "http://test#phone", true );
		phoneProperty.setRange( new MetaStructureClassRange( phoneClass ) );
		
		personClass.getProperties().add( givenNameProperty );
		
		MetaStructureClass userClass =
			namespace.getMetaClass( "http://test#User", true );
		personClass.getDirectSubs().add( userClass );
		assertTrue( personClass.isSubOf( thingClass ) );
		assertTrue( userClass.isSubOf( personClass ) );
		assertTrue( userClass.isSubOf( thingClass ) );
		assertFalse( userClass.isSubOf( phoneClass ) );
		
		Node person1 = neo().createNode();
		Node person2 = neo().createNode();
		assertCollection( personClass.getInstances() );
		personClass.getInstances().add( person1 );
		assertCollection( personClass.getInstances(), person1 );
		personClass.getInstances().add( person2 );
		assertCollection( personClass.getInstances(), person1, person2 );
		
		personClass.getInstances().remove( person2 );
		assertCollection( personClass.getInstances(), person1 );
		neo().getNodeById( person2.getId() );
	}
}
