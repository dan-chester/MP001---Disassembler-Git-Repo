package disassembler;
import java.io.*;
import java.util.*;
import javax.swing.*;

public class DisassembleDriver {

	/**
	 * @param args
	 */
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		System.out.println("========= JAVA DISASSEMBLER / ASSEMBLER ================");
		System.out.println("[1] Assembly to Java");
		System.out.println("[2] Java to Assembly");
		System.out.print("[1?/ 2?] : ");
		Scanner keyboard = new Scanner(System.in);
		String key = keyboard.nextLine();
		if(Integer.valueOf(key) > 2 || key.length() > 1){
			System.out.println("Choose Again");
			return;
		}
		System.out.print("Directory Input (e.g C:\\this\\path\\to\\file.asm) : ");
		String pathToFile = keyboard.nextLine();
		System.out.print("Directory Output (e.g C:\\this\\path\\to\\folder) : ");
		String pathToOut = keyboard.nextLine();
		//C:\Users\Windows 8\Documents\Eclipse\Disassembler-WSM\src\disassembler\ForLoop.java
		//C:\masm32\_projects
		pathToFile = pathToFile.replaceAll("\\\\", "\\\\\\\\");
		pathToOut = pathToOut.replaceAll("\\\\", "\\\\\\\\");
	try{
		if(Integer.valueOf(key) == 2){
			Disassemble test = new Disassemble(pathToFile); 
			String[] testArray = test.readFile();
			test.asmStart();
			test.getDataSegment(testArray);
			test.getCodeSegment(testArray);
			test.writeToASM(pathToOut);
			}
		else if(Integer.valueOf(key) == 1){
			AssemblyToJava test = new AssemblyToJava(pathToFile); 
			String[] testArray = test.readFile();
			test.javaStart();
			test.getVariable(testArray);
			test.getMain(testArray, 0);
			test.writeToJAVA(pathToOut);
			}
		}
		
		catch(IOException e){
			System.out.println("Error.");
			
		}
	}
}
