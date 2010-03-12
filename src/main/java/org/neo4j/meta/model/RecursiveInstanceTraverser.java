package org.neo4j.meta.model;

import java.util.Iterator;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.TraversalPosition;
import org.neo4j.graphdb.Traverser.Order;

public class RecursiveInstanceTraverser implements Iterable<Node>
{
    private final GraphDatabaseService graphDb;
    private final Node node;
    private final MetaModel model;

    public RecursiveInstanceTraverser( GraphDatabaseService graphDb, Node node,
            MetaModel model )
    {
        this.graphDb = graphDb;
        this.node = node;
        this.model = model;
    }

    public Iterator<Node> iterator()
    {
        ReturnableEvaluator eval = new ReturnableEvaluator()
        {
            public boolean isReturnableNode( TraversalPosition position )
            {
                if ( position.notStartNode() && position.lastRelationshipTraversed().isType(
                        MetaModelRelTypes.META_IS_INSTANCE_OF ) )
                {
                    return true;
                }
                return false;
            }
        };
        
        return node.traverse( Order.BREADTH_FIRST, StopEvaluator.END_OF_GRAPH, eval,
                MetaModelRelTypes.META_IS_INSTANCE_OF, Direction.INCOMING,
                MetaModelRelTypes.META_IS_SUBCLASS_OF, Direction.INCOMING ).iterator();
    }
}
