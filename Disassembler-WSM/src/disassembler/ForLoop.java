package disassembler;

public class ForLoop {

	/**
	 * @param args
	 */
	public static void main(String[] args)
    {
        int z;
        int a = 10, b = 5;
        for (z = 12; z > 1; z--)
        {
            a += b;
            System.out.println(a);
        }
    }
}
