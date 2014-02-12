public class Node
{
	long key;
	long value;
	volatile Node lChild, rChild;
	int threadId;
	
	public Node()
	{		
	}	

	public Node(long key, long value, int threadId)
	{
		this.key = key;
		this.value = value;
		this.threadId = threadId;
	}

	public Node(long key, long value, Node lChild, Node rChild, int threadId)
	{
		this.key = key;
		this.value = value;
		this.lChild = lChild;
		this.rChild = rChild;
		this.threadId = threadId;
	}
}