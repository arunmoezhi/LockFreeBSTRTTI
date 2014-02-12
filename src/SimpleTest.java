public class SimpleTest 
{
	public static void main(String[] args)
	{
		LockFreeBSTRTTI obj = new LockFreeBSTRTTI();
		obj.createHeadNodes();
		//obj.insert(1, 1);
		System.out.println(obj.lookup(1));

		//obj.insert(2, 2);
		System.out.println(obj.lookup(2));

		//obj.insert(3, 3);
		System.out.println(obj.lookup(3));

		//obj.insert(99,99);
		System.out.println(obj.lookup(99));
		
		//obj.delete(1);
		System.out.println(obj.lookup(99));
	}
}
