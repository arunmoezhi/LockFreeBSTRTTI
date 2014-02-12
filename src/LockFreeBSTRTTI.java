import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class LockFreeBSTRTTI
{
	static Node grandParentHead;
	static Node parentHead;
	static LockFreeBSTRTTI obj;
	static long nodeCount=0;

	static final AtomicReferenceFieldUpdater<Node, Node> lUpdate = AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "lChild");
	static final AtomicReferenceFieldUpdater<Node, Node> rUpdate = AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "rChild");

	//	static final AtomicReferenceFieldUpdater<Node, Node0F0M> l0F0MUpdate = AtomicReferenceFieldUpdater.newUpdater(Node.class, Node0F0M.class, "lChild");
	//	static final AtomicReferenceFieldUpdater<Node, Node0F1M> l0F1MUpdate = AtomicReferenceFieldUpdater.newUpdater(Node.class, Node0F1M.class, "lChild");
	//	static final AtomicReferenceFieldUpdater<Node, Node1F0M> l1F0MUpdate = AtomicReferenceFieldUpdater.newUpdater(Node.class, Node1F0M.class, "lChild");
	//	static final AtomicReferenceFieldUpdater<Node, Node1F1M> l1F1MUpdate = AtomicReferenceFieldUpdater.newUpdater(Node.class, Node1F1M.class, "lChild");
	//
	//	static final AtomicReferenceFieldUpdater<Node, Node0F0M> r0F0MUpdate = AtomicReferenceFieldUpdater.newUpdater(Node.class, Node0F0M.class, "rChild");
	//	static final AtomicReferenceFieldUpdater<Node, Node0F1M> r0F1MUpdate = AtomicReferenceFieldUpdater.newUpdater(Node.class, Node0F1M.class, "rChild");
	//	static final AtomicReferenceFieldUpdater<Node, Node1F0M> r1F0MUpdate = AtomicReferenceFieldUpdater.newUpdater(Node.class, Node1F0M.class, "rChild");
	//	static final AtomicReferenceFieldUpdater<Node, Node1F1M> r1F1MUpdate = AtomicReferenceFieldUpdater.newUpdater(Node.class, Node1F1M.class, "rChild");



	public LockFreeBSTRTTI()
	{
	}

	public final long lookup(long target)
	{
		
		Node node = grandParentHead;
		while(node.lChild != null) //loop until a leaf or dummy node is reached
		{
			if(target < node.key)
			{
				node = node.lChild;
			}
			else
			{
				node = node.rChild;
			}
		}

		if(target == node.key)
		{
			//key found
			return(1);
		}
		else
		{
			//key not found
			return(0);
		}
	}

	public final void insert(long insertKey, long insertValue, int threadId)
	{

		Node node;
		Node pnode;
		SeekRecord s;
		while(true)
		{
			pnode = parentHead;
			node = parentHead.lChild;
			while(node.lChild != null) //loop until a leaf or dummy node is reached
			{
				if(insertKey < node.key)
				{
					pnode = node;
					node = node.lChild;
				}
				else
				{
					pnode = node;
					node = node.rChild;
				}
			}

			//leaf node is reached
			if(node.key == insertKey)
			{
				//key is already present in tree. So return
				return;
			}

			Node0F0M internalNode, lLeafNode,rLeafNode;
			if(insertKey > node.key)
			{
				rLeafNode = new Node0F0M(insertKey, insertValue, threadId);
				internalNode = new Node0F0M(insertKey, insertValue, node, rLeafNode, threadId);
			}
			else
			{
				lLeafNode = new Node0F0M(insertKey, insertValue, threadId);
				internalNode = new Node0F0M(node.key, node.value, lLeafNode, node, threadId);
			}

			if(insertKey < pnode.key)
			{
				if(node.getClass() == Node0F0M.class)
				{
					if(lUpdate.compareAndSet(pnode, node, internalNode))
					{
						return;
					}
				}
				else
				{
					s = seek(insertKey);
					cleanUp(insertKey,s);
				}


//				if(node.getClass() == Node0F0M.class && lUpdate.compareAndSet(pnode, node, internalNode))
//				{
//					//System.out.println("I3 " + insertKey);
//					return;
//				}
//				else
//				{
//
//					//insert failed; help the conflicting delete operation
//					//System.out.println("1 " + insertKey + " " + node + " " + " " + pnode.lChild + " " + internalNode);	
//					if(pnode.lChild.getClass() != Node0F0M.class) // address has not changed. So CAS would have failed coz of flag/mark only
//					{
//						//System.out.println("2 " + insertKey);
//						//help other thread with cleanup
//						s = seek(insertKey);
//						cleanUp(insertKey,s);
//					}
//				}
			}
			else
			{
				if(node.getClass() == Node0F0M.class)
				{
					if(rUpdate.compareAndSet(pnode, node, internalNode))
					{
						return;
					}
				}
				else
				{
					s = seek(insertKey);
					cleanUp(insertKey,s);
				}
				
//				if(node.getClass() == Node0F0M.class && rUpdate.compareAndSet(pnode, node, internalNode))
//				{
//					//System.out.println("I4 " + insertKey);
//					return;
//				}
//				else
//				{
//					//insert failed; help the conflicting delete operation
//					//System.out.println("3 " + insertKey + " " + node + " " + " " + pnode.rChild + " " + internalNode);	
//
//					//if(node == pnode.rChild) // address has not changed. So CAS would have failed coz of flag/mark only
//					if(pnode.rChild.getClass() != Node0F0M.class) // address has not changed. So CAS would have failed coz of flag/mark only
//					{
//						//System.out.println("4 " + insertKey);
//						//help other thread with cleanup
//						s = seek(insertKey);
//						cleanUp(insertKey,s);
//					}
//				}
			}
			//System.out.print(".");
		}
	}

	public final void delete(long deleteKey, int threadId)
	{
		boolean isCleanUp=false;
		SeekRecord s;
		Node parent;
		Node leaf=null;
		Node tempFlaggedNode;
		while(true)
		{
			s = seek(deleteKey);
			if(!isCleanUp)
			{
				leaf = s.leaf;
				if(leaf.key != deleteKey)
				{
					return;
				}
				else
				{
					parent = s.parent;
					if(deleteKey < parent.key)
					{
						tempFlaggedNode = new Node1F0M(leaf.key,leaf.value, threadId);
						tempFlaggedNode.lChild = leaf.lChild;
						tempFlaggedNode.rChild = leaf.rChild;
						if(leaf.getClass() == Node0F0M.class && lUpdate.compareAndSet(parent, leaf, tempFlaggedNode)) //00 to 10 - just flag
						{
							isCleanUp = true;
							//do cleanup
							if(cleanUp(deleteKey,s))
							{
								return;
							}
						}
						else
						{
							if(parent.lChild.getClass() != Node0F0M.class) // address has not changed. So CAS would have failed coz of flag/mark only
							{
								//help other thread with cleanup
								cleanUp(deleteKey,s);
							}
						}
					}
					else
					{
						tempFlaggedNode = new Node1F0M(leaf.key,leaf.value);
						tempFlaggedNode.lChild = leaf.lChild;
						tempFlaggedNode.rChild = leaf.rChild;
						if(leaf.getClass() == Node0F0M.class && rUpdate.compareAndSet(parent, leaf, tempFlaggedNode)) //00 to 10 - just flag
						{
							isCleanUp = true;
							//do cleanup
							if(cleanUp(deleteKey,s))
							{
								return;
							}
						}
						else
						{
							if(parent.rChild.getClass() != Node0F0M.class) // address has not changed. So CAS would have failed coz of flag/mark only
							{
								//help other thread with cleanup
								cleanUp(deleteKey,s);
							}
						}
					}
				}
			}
			else
			{
				//in the cleanup phase
				//check if leaf is still present in the tree. If nobody has helped with the clean up old leaf will be still hanging. So remove it
				if(s.leaf == leaf)
				{
					//do cleanup
					if(cleanUp(deleteKey,s))
					{
						return;
					}
				}
				else
				{
					//someone helped with my cleanup. So I'm done
					return;
				}
				//System.out.println("Del5");
			}
		}
	}

	public final boolean cleanUp(long key, SeekRecord s)
	{
		Node ancestor = s.ancestor;
		Node parent = s.parent;
		Node oldSuccessor;
		Node sibling;
		Node tempTaggedNode,tempUnTaggedNode;

		if(key < parent.key) //xl case
		{
			if(parent.lChild.getClass() == Node1F0M.class || parent.lChild.getClass() == Node1F1M.class)
				//if(parent.lChild.getStamp() > 1 ) // check if parent to leaf edge is already flagged. 10 or 11
			{
				//leaf node is flagged for deletion. tag the sibling edge to prevent any modification at this edge now
				sibling = parent.rChild;
				if(sibling.getClass().getName() == "Node0F0M")
				{
					tempTaggedNode = new Node0F1M(sibling.key, sibling.value);
					tempTaggedNode.lChild = sibling.lChild;
					tempTaggedNode.rChild = sibling.rChild;
					rUpdate.compareAndSet(parent, sibling, tempTaggedNode);
				}
				else if(sibling.getClass().getName() == "Node1F0M")
				{
					tempTaggedNode = new Node1F1M(sibling.key, sibling.value);
					tempTaggedNode.lChild = sibling.lChild;
					tempTaggedNode.rChild = sibling.rChild;
					rUpdate.compareAndSet(parent, sibling, tempTaggedNode);
				}
			}
			else
			{				
				//leaf node is not flagged. So sibling node must have been flagged for deletion	
				sibling = parent.lChild;
				if(sibling.getClass().getName() == "Node0F0M") 
				{
					tempTaggedNode = new Node0F1M(sibling.key, sibling.value);
					tempTaggedNode.lChild = sibling.lChild;
					tempTaggedNode.rChild = sibling.rChild;
					lUpdate.compareAndSet(parent, sibling, tempTaggedNode);
				}
			}		
		}
		else //xr case
		{
			if(parent.rChild.getClass() == Node1F0M.class || parent.rChild.getClass() == Node1F1M.class)
			{
				//leaf node is flagged for deletion. tag the sibling edge to prevent any modification at this edge now
				sibling = parent.lChild;
				if(sibling.getClass().getName() == "Node0F0M")
				{
					tempTaggedNode = new Node0F1M(sibling.key, sibling.value);
					tempTaggedNode.lChild = sibling.lChild;
					tempTaggedNode.rChild = sibling.rChild;
					lUpdate.compareAndSet(parent, sibling, tempTaggedNode);
				}
				else if(sibling.getClass().getName() == "Node1F0M")
				{
					tempTaggedNode = new Node1F1M(sibling.key, sibling.value);
					tempTaggedNode.lChild = sibling.lChild;
					tempTaggedNode.rChild = sibling.rChild;
					lUpdate.compareAndSet(parent, sibling, tempTaggedNode);
				}
			}
			else
			{				
				//leaf node is not flagged. So sibling node must have been flagged for deletion	
				sibling = parent.rChild;
				if(sibling.getClass().getName() == "Node0F0M") 
				{
					tempTaggedNode = new Node0F1M(sibling.key, sibling.value);
					tempTaggedNode.lChild = sibling.lChild;
					tempTaggedNode.rChild = sibling.rChild;
					rUpdate.compareAndSet(parent, sibling, tempTaggedNode);
				}
			}		
		}

		if(key < ancestor.key)
		{
			oldSuccessor = ancestor.lChild;

			//copy only the flag	
			if(sibling.getClass().getName() == "Node1F0M" || sibling.getClass().getName() == "Node0F0M" )
			{
				return(lUpdate.compareAndSet(ancestor, oldSuccessor, sibling));
			}
			else
			{
				tempUnTaggedNode = new Node0F0M(sibling.key,sibling.value);
				tempUnTaggedNode.lChild = sibling.lChild;
				tempUnTaggedNode.rChild = sibling.rChild;
				return(lUpdate.compareAndSet(ancestor, oldSuccessor, tempUnTaggedNode));

			}
		}
		else
		{
			//copy only the flag	
			oldSuccessor = ancestor.rChild;
			if(sibling.getClass().getName() == "Node1F0M" || sibling.getClass().getName() == "Node0F0M" )
			{
				return(rUpdate.compareAndSet(ancestor, oldSuccessor, sibling));
			}
			else
			{
				tempUnTaggedNode = new Node0F0M(sibling.key,sibling.value);
				tempUnTaggedNode.lChild = sibling.lChild;
				tempUnTaggedNode.rChild = sibling.rChild;
				return(rUpdate.compareAndSet(ancestor, oldSuccessor, tempUnTaggedNode));

			}
		}
	}

	public final SeekRecord seek(long key)
	{
		Node parentField;
		Node currentField;
		Node current;

		//initialize the seek record
		SeekRecord s = new SeekRecord(grandParentHead, parentHead, parentHead, parentHead.lChild);

		parentField = s.ancestor.lChild;
		currentField = s.successor.lChild;

		while(currentField != null)
		{
			current = currentField;
			//move down the tree
			//check if the edge from the current parent node in the access path is tagged
			if(parentField.getClass() == Node0F0M.class || parentField.getClass() == Node1F0M.class)
				//if(parentField.getStamp() == 0 || parentField.getStamp() == 2) // 00, 10 untagged
			{
				s.ancestor = s.parent;
				s.successor = s.leaf;
			}
			//advance parent and leaf pointers
			s.parent = s.leaf;
			s.leaf = current;
			parentField = currentField;
			if(key < current.key)
			{
				currentField = current.lChild;
			}
			else
			{
				currentField = current.rChild;
			}
		}
		return s;
	}

	public final void nodeCount(Node node)
	{
		if(node == null || node.key == 0)
		{
			return;
		}
		if(node.lChild == null)
		{
			nodeCount++;
		}

		if(node.lChild != null)
		{
			nodeCount(node.lChild);
			nodeCount(node.rChild);
		}
	}

	public final void createHeadNodes()
	{
		long key = Long.MAX_VALUE;
		long value = Long.MAX_VALUE;

		parentHead = new Node0F0M(key, value, new Node0F0M(key,value,-1), new Node0F0M(key,value,-1), -1);
		grandParentHead = new Node0F0M(key, value, parentHead, new Node0F0M(key,value, -1),-1);
	}

}