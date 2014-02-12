
public class Node1F0M extends Node
{
	public Node1F0M(long key, long value, int threadId)
	{
		super(key, value, threadId);
	}
	
	public Node1F0M(long key, long value, Node lChild, Node rChild, int threadId)
	{
		super(key, value, lChild, rChild, threadId);
	}
}
