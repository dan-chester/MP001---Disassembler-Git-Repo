package disassembler;

public class Switch {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int tree = 24; 
		int x = 2;
		int g = x + tree;
		g += tree;
		g -= x;
		if(g < 250){
			System.out.print(tree);
		}else{
			System.out.print(g);
		}
	}
}
