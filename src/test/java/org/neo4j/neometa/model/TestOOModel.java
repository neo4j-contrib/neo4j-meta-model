package org.neo4j.neometa.model;

import org.neo4j.neometa.MetaTestCase;

/**
 * Tests the meta model through the object oriented {@link MetaModel} API.
 */
public class TestOOModel extends MetaTestCase
{
	/**
	 * Some basic tests.
	 */
	public void testSome()
	{
		MetaModel model = new MetaModel( neo() );
		
		// Model some of the daqapo model.
		MetaClass orgClass =
			model.getMetaClass( "OrganisationalUnit", true );
		MetaProperty orgName = orgClass.getProperty( "name", true );
		orgName.setFundamentalValueType( String.class );
		assertEquals( orgClass, orgName.getOwner() );
		MetaProperty orgParent = orgClass.getProperty( "parent", true );
		orgParent.setMetaClassValueType( orgClass );
		assertCollection( orgClass.getDeclaredProperties(),
			orgName, orgParent );
		assertCollection( orgClass.getProperties(), orgName, orgParent );
		
		MetaClass userClass = model.getMetaClass( "User", true );
		MetaProperty userName = userClass.getProperty( "name", true );
		userName.setFundamentalValueType( String.class );
		MetaProperty userOrg =
			userClass.getDeclaredProperty( "home_organisation", true );
		userOrg.setMetaClassValueType( orgClass );
		
		MetaClass guestUserClass = model.getMetaClass( "GuestUser", true );
		userClass.getDirectSubClasses().add( guestUserClass );
		assertTrue( userClass.getDirectSubClasses().contains(
			guestUserClass ) );
		assertEquals( guestUserClass, model.getMetaClass(
			"GuestUser", false ) );
		assertFalse( guestUserClass.equals( orgClass ) );
		assertCollection( guestUserClass.getDirectSuperClasses(), userClass );
		
		// Verify
		assertCollection( model.getMetaClasses(), orgClass, userClass,
			guestUserClass );
		assertCollection( orgClass.getProperties(), orgName, orgParent );
		assertCollection( userClass.getProperties(), userName, userOrg );
		deleteMetaModel();
	}
}
