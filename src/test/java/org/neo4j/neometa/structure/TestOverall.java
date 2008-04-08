package org.neo4j.neometa.structure;

import java.util.Set;

import org.neo4j.api.core.Node;
import org.neo4j.api.core.Transaction;
import org.neo4j.neometa.MetaTestCase;

/**
 * Tests the meta structure.
 */
public class TestOverall extends MetaTestCase
{
	private Transaction tx;
	
	/**
	 * Some basic tests.
	 */
	public void testSome()
	{
		tx = neo().beginTx();
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
		MetaStructure structure = new MetaStructure( neo() );
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
		phoneTypeProperty.setRange( new DataRange(
			RdfUtil.NS_XML_SCHEMA + "string", "home", "work", "cell" ) );
		assertCollection( ( ( DataRange ) phoneTypeProperty.getRange() ).
			getValues(), "home", "work", "cell" );
		assertEquals( 1, namespace.getMetaProperties().size() );
		phoneClass.getDirectProperties().add( phoneTypeProperty );
		assertCollection( phoneTypeProperty.associatedMetaClasses(),
			phoneClass );
//		assertNull( phoneTypeProperty.getMinCardinality() );
//		assertNull( phoneTypeProperty.getMaxCardinality() );
		assertNull( phoneTypeProperty.getCollectionBehaviourClass() );
//		phoneTypeProperty.setMinCardinality( 0 );
//		phoneTypeProperty.setMaxCardinality( 1 );
//		assertEquals( 0, ( int ) phoneTypeProperty.getMinCardinality() );
//		assertEquals( 1, ( int ) phoneTypeProperty.getMaxCardinality() );
		
		MetaStructureProperty phoneNumberProperty =
			namespace.getMetaProperty( "http://test#phoneNumber", true );
		assertEquals( 2, namespace.getMetaProperties().size() );
		phoneClass.getDirectProperties().add( phoneNumberProperty );
		
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
		assertEquals( phoneProperty.getName(), ( ( MetaStructureClassRange )
			phoneProperty.getRange() ).getRelationshipTypeToUse().name() );
//		phoneProperty.setMinCardinality( 0 );
//		phoneProperty.setMaxCardinality( 3 );
//		assertEquals( 3, ( int ) phoneProperty.getMaxCardinality() );
//		phoneProperty.setMaxCardinality( null );
//		assertNull( phoneProperty.getMaxCardinality() );
		phoneProperty.setCollectionBehaviourClass( Set.class );
		assertEquals( Set.class, phoneProperty.getCollectionBehaviourClass() );
		
		personClass.getDirectProperties().add( givenNameProperty );
		
		MetaStructureClass userClass =
			namespace.getMetaClass( "http://test#User", true );
		personClass.getDirectSubs().add( userClass );
		assertCollection( userClass.getDirectProperties() );
		assertCollection( userClass.getAllProperties(), givenNameProperty );
		
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
		deleteMetaModel();
	}
	
	/**
	 * Tests extended functionality, like OWL constructs.
	 */
	public void testExtended()
	{
		tx = neo().beginTx();
		try
		{
			txTestExtended();
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	private void txTestExtended()
	{
		MetaStructure structure = new MetaStructure( neo() );
		MetaStructureNamespace namespace = structure.getGlobalNamespace();
		MetaStructureProperty maker = namespace.getMetaProperty(
			"http://test.org/test#maker", true );
		MetaStructureProperty madeBy = namespace.getMetaProperty(
			"http://test.org/test#madeBy", true );
		assertNull( maker.getInverseOf() );
		assertNull( madeBy.getInverseOf() );
		maker.setInverseOf( madeBy );
		assertEquals( maker, madeBy.getInverseOf() );
		assertEquals( madeBy, maker.getInverseOf() );
		madeBy.setInverseOf( maker );
		assertEquals( maker, madeBy.getInverseOf() );
		assertEquals( madeBy, maker.getInverseOf() );
		maker.setInverseOf( null );
		assertNull( maker.getInverseOf() );
		assertNull( madeBy.getInverseOf() );
		deleteMetaModel();
	}
}
