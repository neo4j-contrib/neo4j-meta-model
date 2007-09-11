package org.neo4j.meta;

import java.util.Iterator;
import org.neo4j.api.core.Node;
import org.neo4j.api.core.Transaction;
import org.neo4j.api.core.Traverser;

class MetaNodeWrapperTraverser<T> implements Iterable<T>
{
	private final Traverser traverser;
	private final Class<? extends MetaNodeWrapper> wrapperClass;
	private final MetaManager metaManager;

	public MetaNodeWrapperTraverser( Class<? extends MetaNodeWrapper>
		wrapperClass, Traverser traverser, MetaManager metaManager )
	{
		this.traverser = traverser;
		this.wrapperClass = wrapperClass;
		this.metaManager = metaManager;
	}

	public Iterator<T> iterator()
	{
		final Iterator<Node> me = this.traverser.iterator();
		return new Iterator<T>()
		{
			public boolean hasNext()
			{
				Transaction tx = Transaction.begin();
				try
				{
					boolean result = me.hasNext();
					tx.success();
					return result;
				}
				finally
				{
					tx.finish();
				}
			}

			public T next()
			{
				Transaction tx = Transaction.begin();
				try
				{
					T result = ( T ) MetaNodeWrapper.newInstance(
						MetaNodeWrapperTraverser.this.wrapperClass,	me.next(),
							MetaNodeWrapperTraverser.this.metaManager ); 
					tx.success();
					return result;
				}
				finally
				{
					tx.finish();
				}
			}

			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}
}