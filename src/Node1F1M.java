
public class Node1F1M extends Node
{
	public Node1F1M(long key, long value, int threadId)
	{
		super(key, value, threadId);
	}
	
	public Node1F1M(long key, long value, Node lChild, Node rChild, int threadId)
	{
		super(key, value, lChild, rChild, threadId);
	}
}
