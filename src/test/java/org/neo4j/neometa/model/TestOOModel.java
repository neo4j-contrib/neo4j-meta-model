package org.neo4j.neometa.model;

import org.neo4j.api.core.Node;
import org.neo4j.api.core.Transaction;
import org.neo4j.neometa.MetaTestCase;

public class TestOOModel extends MetaTestCase
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
		MetaModel model = new MetaModel( neo(), rootNode );
		
		// Model some of the daqapo model.
		MetaClass orgClass =
			model.getMetaClass( "OrganisationalUnit", true );
		MetaProperty orgName = orgClass.getProperty( "name", true );
		orgName.setFundamentalValueType( String.class );
		MetaProperty orgParent = orgClass.getProperty( "parent", true );
		orgParent.setMetaClassValueType( orgClass );
		
		MetaClass userClass = model.getMetaClass( "User", true );
		MetaProperty userName = userClass.getProperty( "name", true );
		userName.setFundamentalValueType( String.class );
		MetaProperty userOrg =
			userClass.getProperty( "home_organisation", true );
		userOrg.setMetaClassValueType( orgClass );
		
		// Verify
		assertCollection( model.getMetaClasses(), orgClass, userClass );
		assertCollection( orgClass.getProperties(), orgName, orgParent );
		assertCollection( userClass.getProperties(), userName, userOrg );
	}
}
