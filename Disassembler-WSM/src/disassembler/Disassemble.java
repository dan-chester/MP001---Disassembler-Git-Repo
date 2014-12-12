package disassembler;
import java.io.*;
import java.util.*;


public class Disassemble {
	private final static int size = 128;
	private String pathName;
	private String result;
	private String fileName;
	private int counter;
	private int varCount = 0;
	private String[] varTypes = new String[size];
	private String[] varNames = new String[size];
	private String[] varValues= new String[size];
	private int index = 0;
	private String checkElse = null;
	private int checkIf = 0;
	private boolean checkIFELSE = false;
	
	public Disassemble (String pathName) throws IOException{
		this.pathName = pathName;
	}
	public String[] readFile() throws IOException{
		FileReader path = new FileReader(pathName);
		BufferedReader buffer = new BufferedReader(path);
				
		int countLines = count();
		String[] textData = new String[countLines];
		
		for(int count = 0; count < countLines; count++){
			textData[count] = buffer.readLine();	
		}
		
		
		buffer.close();
		path.close();
		return textData;
	}
	
	private int count() throws IOException{
		FileReader path = new FileReader(pathName);
		BufferedReader buffer = new BufferedReader(path);
		
		int countLine = 0;
		while((buffer.readLine()) != null){
			countLine++;
		}
		buffer.close();
		path.close();
		return countLine;
	}
	
	public String asmStart() {
		String x = this.pathName;
		StringTokenizer tokens = new StringTokenizer(x,"\\");
		int tokenCount = tokens.countTokens();
		String[] path = new String[tokenCount];
		int index = 0;
		while(tokens.hasMoreTokens()){
			path[index] = tokens.nextToken();
			index++;
		}
		fileName = path[tokenCount - 1];
		fileName = fileName.substring(0, (fileName.length() - 5));
		result = "title " + fileName + "\n.model small\n.stack 100h"; 
		return result;
	}
	public String getDataSegment(String[] lineOfCodes){
		result += "\n.data\n";
		int length = lineOfCodes.length;
		
		for(int i = 0; i < length; i++){
			String line = lineOfCodes[i].trim().replaceAll(" +", " ");

			if(line.toLowerCase().contains("system.out.println(\"")){
				result += "var" + varCount + " db " + "\"" + tokenizeLine(line) + "$\"\n";
				varCount++;
			}
			if(line.toLowerCase().contains("system.out.print(\"")){
				result += "var" + varCount + " db " + "\"" + tokenizeLine(line) + "$\"\n";
				varCount++;
			}
		}
		
		String[] array;
		String[][] twoDarr;
		for(int b = 0 ; b < length; b++){
			String line = lineOfCodes[b].trim().replaceAll(" +", "");
			String line2 = lineOfCodes[b].trim().replaceAll(" +", " ");
			if(line.toLowerCase().contains("int")){
				if(line.toLowerCase().contains("system.out.println(\"")){
					continue;
				}else if(line.toLowerCase().contains("for(int")){
					array = tokenizeFor(line);
					result += array[4] + " db " + array[0] + " \n"; 
					varNames[index] = array[4];
					varValues[index] = array[0];
					varTypes[index] = array[5];
					index++;
				}else if(line.toLowerCase().contains("system.out.println(")){
					continue;
				}else if(line.toLowerCase().contains("system.out.print(")){
					continue;
				}else if(line.toLowerCase().contains("system.out.print(\"")){
					continue;
				}else{
				twoDarr = tokenizeInt(line);
				for(int j =0 ; j < twoDarr.length; j++){
					if(!isInteger(twoDarr[j][1])){
						twoDarr[j][1] = "?";
					}
					result += twoDarr[j][0] + " db " + twoDarr[j][1] + " \n";
					varNames[index] = twoDarr[j][0];
					varValues[index] = twoDarr[j][1];
					varTypes[index] = twoDarr[j][2];
					index++;
					}
				}
			}
			if(line.toLowerCase().contains("char")){
					array = tokenizeChar(line);
					result += array[0] + " db '" + array[1] + "'\n";
					varNames[index] = array[0];
					varValues[index] = array[1];
					varTypes[index] = array[2];
					index++;
			}
			if(line2.toLowerCase().contains("string")){
				if(line2.toLowerCase().contains("string[] args")){
					continue;
				}else
					array = tokenizeString(line2);
					result += array[0] + " db " + array[1] + "$\"\n";  
					varNames[index] = array[0];
					varValues[index] = array[1];
					varTypes[index] = array[2];
					index++;
			}
			
			if(line.toLowerCase().contains("boolean")){
				array = tokenizeBoolean(line);
				result += array[0] + " db '" + array[1] + "'\n";
		}
		
		}
			
		return result;
	}
	public String getCodeSegment(String[] lineOfCodes){
		result += ".code\nmain proc\n\nmov ax, @data\nmov ds,ax\n\n";
		int length = lineOfCodes.length;
		varCount = 0;
		String[][] twoDarr;
		String[] arr;
		int x = 0;
		String line = null;
		String line1 = null;
		String line2 = null;
		for(int a = 0; a < length; a++){
			line = lineOfCodes[a].trim().replaceAll("\\s", " ");
			if(line.equals("")){
				x++;
			}
		}
		int length2 = length - x;
		String[] checkArr = new String[length2];
		for(int b = 0, y =0; b < length; b++){
			line = lineOfCodes[b].trim().replaceAll("\\s", " ");
			if(!line.equals("")){
				checkArr[y] = line;
				y++;
			}
		}
		for(int i = 0; i < length2; i++){	
			line = checkArr[i].trim().replaceAll("\\s", " ");
			line1 = checkArr[i].trim().replaceAll(" +", "");
			
			if(line.toLowerCase().contains("if(")){
				getIfStatements(checkArr,i);
				i += counter;
			}
			if((line.toLowerCase().contains("else{") || line.toLowerCase().contains("else")) && checkIFELSE == false) {
				i++;
				getElseStatements(checkArr,i);
				i += counter;
			}else if((line.toLowerCase().contains("else{") || line.toLowerCase().contains("else")) && checkIFELSE == true){
				getElseStatements(checkArr,i);
				i+= counter;
			}
			if(line1.toLowerCase().contains("for(")){
				getForStatements(checkArr,i);
				i += counter;
			}
			if(line.toLowerCase().contains("+=")){
				arr = tokenMore(line.replaceAll(" +", ""),"+=");
				result += "xor ax,ax \nxor bx,bx \nmov al,"+arr[0]+"\nmov bl,"+arr[1]+"\n";
				result += arr[2]+" al,bl \nmov "+arr[0]+",al\n";
			}
			if(line.toLowerCase().contains("-=")){
				arr = tokenMore(line.replaceAll(" +", ""),"-=");
				result += "xor ax,ax \nxor bx,bx \nmov al,"+arr[0]+"\nmov bl,"+arr[1]+"\n";
				result += arr[2]+" al,bl \nmov "+arr[0]+",al\n";
			}
		 if(line.toLowerCase().contains("int")){
			 if(line.toLowerCase().contains("system.out.println(\"")){
				result += "lea dx, var"+ varCount +"\n mov ah, 09h\n" + "int 21h\n\n"; 
				result += "mov dl, 10\nmov ah, 2\nint 21h\n\n";
				varCount++;
			}
			else if(line.toLowerCase().contains("system.out.print(\"")){
				result += "lea dx, var"+ varCount +"\n mov ah, 09h\n" + "int 21h\n\n"; 
				varCount++;
			}
			else if(line.toLowerCase().contains("system.out.println(")){
				String temp = tokenizeVar(line);
				String indexType = "";
				int indexNum = 0;
				for(int a = 0; a < index; a++){
					if(varNames[a].equals(temp)){
						indexType = varTypes[a];
						indexNum = a;
					}
				}
				if(indexType.equals("int") && ((Integer.valueOf(varValues[indexNum]) > 100) && (Integer.valueOf(varValues[indexNum]) < 256))){
					result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov bl, 100 \n" ;
					result += "div bl \nmov ch,ah \nadd al, 48 \nmov dl, al \n mov ah, 02h \nint 21h\n";
					result += "mov ah,0 \nmov al, ch \n mov ch,0 \nxor bl,bl \nmov bl,10 \ndiv bl \nmov ch,ah \nadd al,48 \n mov dl, al\n mov ah,02h\n int 21h\n";
					result += "mov ah,0 \n mov al,ch \nmov ch,0 \nadd al,48 \nmov dl,al \nmov ah,02h \n int 21h\nmov dl, 10 \nmov ah,02h \n int 21h\n\n";
				}
				
				if(indexType.equals("int") && ((Integer.valueOf(varValues[indexNum]) > 10) && (Integer.valueOf(varValues[indexNum]) < 100))){
					result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov bl, 10 \n" ;
					result += "div bl \nmov ch,ah \nadd al, 48 \nmov dl, al \n mov ah, 02h \nint 21h\n";
					result += "mov ah,0 \n mov al,ch \nmov ch,0 \nadd al,48 \nmov dl,al \nmov ah,02h \n int 21h\nmov dl, 10 \nmov ah,02h \n int 21h\n\n";
				}
				
				if(indexType.equals("int") && ((Integer.valueOf(varValues[indexNum]) >= 0) && (Integer.valueOf(varValues[indexNum]) < 10))){
					result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + " \nadd al,48 \nmov dl, al \n";
					result += "mov ah,02h \nint 21h \nmov dl, 10 \nmov ah,02h \nint 21h\n\n";
				}
				if(indexType.equals("char")){
					result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov dl, al \n";
					result += "mov ah,02h \nint 21h \nmov dl, 10 \nmov ah,02h \nint 21h\n\n";
				}
				if(indexType.equals("string")){
					result += "xor ax,ax \nxor bx,bx \nmov dx, offset " + varNames[indexNum] + "\n";
					result += "mov ah,09h \nint 21h \nmov dl, 10 \nmov ah,02h \nint 21h\n\n";
				}	
			}else if(line.toLowerCase().contains("system.out.print(")){
				String temp = tokenizeVar(line);
				String indexType = "";
				int indexNum = 0;
				for(int a = 0; a < index; a++){
					if(varNames[a].equals(temp)){
						indexType = varTypes[a];
						indexNum = a;
					}
				}
				if(indexType.equals("int") && ((Integer.valueOf(varValues[indexNum]) > 100) && (Integer.valueOf(varValues[indexNum]) < 256))){
					result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov bl, 100 \n" ;
					result += "div bl \nmov ch,ah \nadd al, 48 \nmov dl, al \n mov ah, 02h \nint 21h\n";
					result += "mov ah,0 \nmov al, ch \n mov ch,0 \nxor bl,bl \nmov bl,10 \ndiv bl \nmov ch,ah \nadd al,48 \n mov dl, al\n mov ah,02h\n int 21h\n";
					result += "mov ah,0 \n mov al,ch \nmov ch,0 \nadd al,48 \nmov dl,al \nmov ah,02h \n int 21h\n\n";
				}
				
				if(indexType.equals("int") && ((Integer.valueOf(varValues[indexNum]) > 10) && (Integer.valueOf(varValues[indexNum]) < 100))){
					result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov bl, 10 \n" ;
					result += "div bl \nmov ch,ah \nadd al, 48 \nmov dl, al \n mov ah, 02h \nint 21h\n";
					result += "mov ah,0 \n mov al,ch \nmov ch,0 \nadd al,48 \nmov dl,al \nmov ah,02h \n int 21h\n\n";
				}
				
				if(indexType.equals("int") && ((Integer.valueOf(varValues[indexNum]) >= 0) && (Integer.valueOf(varValues[indexNum]) < 10))){
					result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + " \nadd al,48 \nmov dl, al \n";
					result += "mov ah,02h \nint 21h \n\n";
				}
				if(indexType.equals("char")){
					result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov dl, al \n";
					result += "mov ah,02h \nint 21h \n\n";
				}
				if(indexType.equals("string")){
					result += "xor ax,ax \nxor bx,bx \nmov dx, offset " + varNames[indexNum] + "\n";
					result += "mov ah,09h \nint 21h \n\n";
					}	
				}
			 	if(line.contains("for(int")){
			 		continue;
			 	}
				else{
					String operator = "";
					String[] out = new String[3];
					twoDarr = tokenizeInt(line);
					for(int j =0 ; j < twoDarr.length; j++){
						if(!isInteger(twoDarr[j][1])){
							operator = "+";
							if(twoDarr[j][1].contains(operator)){
								out = tokenizeOp(twoDarr[j][1].replaceAll(" +",""), operator);
								result += "xor ax,ax \nxor bx,bx \nmov al,"+ out[0]+ "\nmov bl,"+out[1]+"\n";
								result += out[2] + " al,bl\n mov "+twoDarr[j][0]+",al\n";
							}
							operator = "-";
							if(twoDarr[j][1].contains(operator)){
								out = tokenizeOp(twoDarr[j][1].replaceAll(" +",""), operator);
								result += "xor ax,ax \nxor bx,bx \nmov al,"+ out[0]+ "\nmov bl,"+out[1]+"\n";
								result += out[2] + " al,bl\n mov "+twoDarr[j][0]+",al\n";
							}
						}
					}
				}
			}
		}
		result += "\nmov ax, 4c00h\nint 21h \n\nmain endp\nend main";
		//System.out.println(result);
		return result;
	}
	// ===================== IF ELSE CONDITION ==========================
	private void getIfStatements(String[] lineOfCodes, int i) {
		int length = lineOfCodes.length;
		String[] sh = new String[4];
		String concat = "";
		String concat1 = "";
		String concatCons = "";
		String cons = "";
		String checkElseIf = "";
		int u = i;
		counter = 0;
		boolean checkFor = false;

		while((u < length) && (checkFor == false)){
			String line = lineOfCodes[u].trim().replaceAll("\\s", ""); 
			String line1 = lineOfCodes[u + 1].trim().replaceAll("\\s", ""); 
			if(line.toLowerCase().contains("if(")){
				sh = tokenizeIf(line);
				checkIf = u;
				concatCons += "";
			}
			else if(line.toLowerCase().contains("}")){
					if(line.toLowerCase().contains("else{") || line1.toLowerCase().contains("else{") || line.toLowerCase().contains("else") || line1.toLowerCase().contains("else")){
						checkElse = "jmp Else" + checkIf;
						checkFor = true;
					}else{
						checkElse = "jmp Cons";
						checkFor = true;
					}
					
			}
			u++;	
		}
		checkFor = false;
		u = i;
		String[][] twoDarr;
		while((u < length) && (checkFor == false)){
			String line = lineOfCodes[u].trim().replaceAll("\\s", ""); 
			String line1 = lineOfCodes[u + 1].trim().replaceAll("\\s", ""); 
			if(line.toLowerCase().contains("if(")){
				sh = tokenizeIf(line);
				result += "xor bx,bx \nmov bl, "+ sh[0] +"\n";
				result += "cmp bl, "+ sh[1] +"\n "+ sh[3] +" If"+ u +"\n";
				result += checkElse +"\n";
				result += "\n If" + u + ":\n";	
				counter++;
			}
			if(line.toLowerCase().contains("}")){
				if(line.length() == 1){
					concat1 += "jmp Cons\n";
					result += concat + concat1 + "\n" + concatCons + "\n";
					checkFor = true;
				}else if(line.length() > 1 && line.contains("else{")){
					checkIFELSE = false;
					counter--;
					concat1 += "jmp Cons\n";
					result += concat + concat1 + "\n";
					checkFor = true;
				}else if(line.length() > 1 && line.contains("else")){
					checkIFELSE = false;
					counter--;
					concat1 += "jmp Cons\n";
					result += concat + concat1 + "\n";
					checkFor = true;
				}else if(line.length() == 1 && line1.contains("else{")){
					checkIFELSE = true;
					concat1 += "jmp Cons\n";
					result += concat + concat1 + "\n";
					checkFor = true;
				}else if(line.length() == 1 && line1.contains("else")){
					checkIFELSE = true;
					concat1 += "jmp Cons\n";
					result += concat + concat1 + "\n";
					checkFor = true;
				}
			}
			if(line.toLowerCase().contains("for(")){
				if(line.toLowerCase().contains("for(int")){
					sh = tokenizeFor(line);
					if(sh[3] != " "){
						result += "mov bx, "+ sh[0] +"\nLoop" + u + ": \n"+ sh[3] +" bx\n";
						concat += "cmp bx, "+ sh[1] +"\n "+ sh[2]+ " Loop"+ u +"\n" ;
						counter++;
					}else{
						result += "mov bx, "+ sh[0] +"\nLoop" + u + ": \n";
						concat += "cmp bx, "+ sh[1] +"\n "+ sh[2]+ " Loop"+ u +"\n" ;
						counter++;
					}
				}else{
					sh = tokenizeFor(line);
					if(sh[3] != " "){
						result += "mov bx, "+ sh[0] +"\nLoop" + u + ": \n"+ sh[3] +" bx\n";
						concat += "cmp bx, "+ sh[1] +"\n "+ sh[2]+ " Loop"+ u +"\n" ;
						counter++;
					}else{
						result += "mov bx, "+ sh[0] +"\nLoop" + u + ": \n";
						concat += "cmp bx, "+ sh[1] +"\n "+ sh[2]+ " Loop"+ u +"\n" ;
						counter++;
					}
				}
			}
			 if(line.toLowerCase().contains("int")){
				 if(line.toLowerCase().contains("system.out.println(\"")){
					result += "lea dx, var"+ varCount +"\n mov ah, 09h\n" + "int 21h\n\n"; 
					result += "mov dl, 10\nmov ah, 2\nint 21h\n\n";
					varCount++;
					counter++;
				}
				else if(line.toLowerCase().contains("system.out.print(\"")){
					result += "lea dx, var"+ varCount +"\n mov ah, 09h\n" + "int 21h\n\n"; 
					varCount++;
					counter++;
				}
				else if(line.toLowerCase().contains("system.out.println(")){
					String temp = tokenizeVar(line);
					String indexType = "";
					int indexNum = 0;
					for(int a = 0; a < index; a++){
						if(varNames[a].equals(temp)){
							indexType = varTypes[a];
							indexNum = a;
						}
					}
					if(indexType.equals("int") && isInteger(varValues[indexNum]) && ((Integer.valueOf(varValues[indexNum]) >= 100) && (Integer.valueOf(varValues[indexNum]) < 256))){
						result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov bl, 100 \n" ;
						result += "div bl \nmov ch,ah \nadd al, 48 \nmov dl, al \n mov ah, 02h \nint 21h\n";
						result += "mov ah,0 \nmov al, ch \n mov ch,0 \nxor bl,bl \nmov bl,10 \ndiv bl \nmov ch,ah \nadd al,48 \n mov dl, al\n mov ah,02h\n int 21h\n";
						result += "mov ah,0 \n mov al,ch \nmov ch,0 \nadd al,48 \nmov dl,al \nmov ah,02h \n int 21h\nmov dl, 10 \nmov ah,02h \n int 21h\n\n";
						counter++;
					}
					
					if(indexType.equals("int") && isInteger(varValues[indexNum]) && ((Integer.valueOf(varValues[indexNum]) >= 10) && (Integer.valueOf(varValues[indexNum]) < 100))){
						result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov bl, 10 \n" ;
						result += "div bl \nmov ch,ah \nadd al, 48 \nmov dl, al \n mov ah, 02h \nint 21h\n";
						result += "mov ah,0 \n mov al,ch \nmov ch,0 \nadd al,48 \nmov dl,al \nmov ah,02h \n int 21h\nmov dl, 10 \nmov ah,02h \n int 21h\n\n";
						counter++;
					}
					
					if(indexType.equals("int") && isInteger(varValues[indexNum]) && ((Integer.valueOf(varValues[indexNum]) >= 0) && (Integer.valueOf(varValues[indexNum]) < 10))){
						result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + " \nadd al,48 \nmov dl, al \n";
						result += "mov ah,02h \nint 21h \nmov dl, 10 \nmov ah,02h \nint 21h\n\n";
						counter++;
					}
					if(indexType.equals("int") && (!isInteger(varValues[indexNum]))){
						result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov bl, 100 \n" ;
						result += "div bl \nmov ch,ah \nadd al, 48 \nmov dl, al \n mov ah, 02h \nint 21h\n";
						result += "mov ah,0 \nmov al, ch \n mov ch,0 \nxor bl,bl \nmov bl,10 \ndiv bl \nmov ch,ah \nadd al,48 \n mov dl, al\n mov ah,02h\n int 21h\n";
						result += "mov ah,0 \n mov al,ch \nmov ch,0 \nadd al,48 \nmov dl,al \nmov ah,02h \n int 21h\n\n";
						counter++;
					}
					if(indexType.equals("char")){
						result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov dl, al \n";
						result += "mov ah,02h \nint 21h \nmov dl, 10 \nmov ah,02h \nint 21h\n\n";
						counter++;
					}
					if(indexType.equals("string")){
						result += "xor ax,ax \nxor bx,bx \nmov dx, offset " + varNames[indexNum] + "\n";
						result += "mov ah,09h \nint 21h \nmov dl, 10 \nmov ah,02h \nint 21h\n\n";
						counter++;
					}	
				}else if(line.toLowerCase().contains("system.out.print(")){
					String temp = tokenizeVar(line);
					String indexType = "";
					int indexNum = 0;
					for(int a = 0; a < index; a++){
						if(varNames[a].equals(temp)){
							indexType = varTypes[a];
							indexNum = a;
						}
					}
					if(indexType.equals("int") && isInteger(varValues[indexNum]) && ((Integer.valueOf(varValues[indexNum]) >= 100) && (Integer.valueOf(varValues[indexNum]) < 256))){
						result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov bl, 100 \n" ;
						result += "div bl \nmov ch,ah \nadd al, 48 \nmov dl, al \n mov ah, 02h \nint 21h\n";
						result += "mov ah,0 \nmov al, ch \n mov ch,0 \nxor bl,bl \nmov bl,10 \ndiv bl \nmov ch,ah \nadd al,48 \n mov dl, al\n mov ah,02h\n int 21h\n";
						result += "mov ah,0 \n mov al,ch \nmov ch,0 \nadd al,48 \nmov dl,al \nmov ah,02h \n int 21h\n\n";
						counter++;
					}
					
					if(indexType.equals("int") && isInteger(varValues[indexNum]) && ((Integer.valueOf(varValues[indexNum]) >= 10) && (Integer.valueOf(varValues[indexNum]) < 100))){
						result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov bl, 10 \n" ;
						result += "div bl \nmov ch,ah \nadd al, 48 \nmov dl, al \n mov ah, 02h \nint 21h\n";
						result += "mov ah,0 \n mov al,ch \nmov ch,0 \nadd al,48 \nmov dl,al \nmov ah,02h \n int 21h\n\n";
						counter++;
					}
					
					if(indexType.equals("int") && isInteger(varValues[indexNum]) && ((Integer.valueOf(varValues[indexNum]) >= 0) && (Integer.valueOf(varValues[indexNum]) < 10))){
						result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + " \nadd al,48 \nmov dl, al \n";
						result += "mov ah,02h \nint 21h \n\n";
						counter++;
					}
					if(indexType.equals("int") && (!isInteger(varValues[indexNum]))){
						result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov bl, 100 \n" ;
						result += "div bl \nmov ch,ah \nadd al, 48 \nmov dl, al \n mov ah, 02h \nint 21h\n";
						result += "mov ah,0 \nmov al, ch \n mov ch,0 \nxor bl,bl \nmov bl,10 \ndiv bl \nmov ch,ah \nadd al,48 \n mov dl, al\n mov ah,02h\n int 21h\n";
						result += "mov ah,0 \n mov al,ch \nmov ch,0 \nadd al,48 \nmov dl,al \nmov ah,02h \n int 21h\n\n";
						counter++;
					}
					if(indexType.equals("char")){
						result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov dl, al \n";
						result += "mov ah,02h \nint 21h \n\n";
						counter++;
					}
					if(indexType.equals("string")){
						result += "xor ax,ax \nxor bx,bx \nmov dx, offset " + varNames[indexNum] + "\n";
						result += "mov ah,09h \nint 21h \n\n";
						counter++;
						}	
					}else if(line.toLowerCase().contains("for(int")){
						continue;
					}
					else{
						String operator = "";
						String[] out = new String[3];
						twoDarr = tokenizeInt(line);
						for(int j =0 ; j < twoDarr.length; j++){
							if(!isInteger(twoDarr[j][1])){
								operator = "+";
								if(twoDarr[j][1].contains(operator)){
									out = tokenizeOp(twoDarr[j][1].replaceAll(" +",""), operator);
									result += "xor ax,ax \nxor bx,bx \nmov al,"+ out[0]+ "\nmov bl,"+out[1]+"\n";
									result += out[2] + " al,bl\n mov "+twoDarr[j][0]+",al";
									counter++;
								}
								operator = "-";
								if(twoDarr[j][1].contains(operator)){
									out = tokenizeOp(twoDarr[j][1].replaceAll(" +",""), operator);
									result += "xor ax,ax \nxor bx,bx \nmov al,"+ out[0]+ "\nmov bl,"+out[1]+"\n";
									result += out[2] + " al,bl\n mov "+twoDarr[j][0]+",al";
									counter++;
								}
							}
						}
					}
				}
			
				u++;	
		}	
	}

	private void getElseStatements(String[] codes, int index1){
		int length = codes.length;
		String[] sh = new String[4];
		String concat = "";
		String concat1 = "";
		int u = index1;
		counter = 0;
		String[][] twoDarr;
		boolean checkFor = false;
		while((u < length) && (checkFor == false)){
			String line2 = codes[u-1].trim().replaceAll("\\s", "");
			String line = codes[u].trim().replaceAll("\\s", ""); 
			String line1 = codes[u + 1].trim().replaceAll("\\s", ""); 
			if(line.toLowerCase().contains("else{") || line.toLowerCase().contains("else") || line2.toLowerCase().contains("else{") || line2.toLowerCase().contains("else")){
				result += "Else" + checkIf +":\n";
				counter++;
			}
			if(line.toLowerCase().contains("}") && (line.length() == 1)){
				checkElse = "jmp Cons";
				concat1 = "";
				concat1 += "jmp Cons\n Cons:";
				result+= concat + concat1 + "\n";
				checkFor = true;
			}else if(line.toLowerCase().contains("}") && line.length() == 1 && line1.toLowerCase().contains("else{")){
				checkElse = "jmp Else"+ checkIf;
				concat1 = "";
				concat1 += "jmp Cons\n";
				result+= concat + concat1 + "\n";
				checkFor = true;
			}else if(line.toLowerCase().contains("}") && line.length() == 1 && line1.toLowerCase().contains("else")){
				checkElse = "jmp Else"+ checkIf;
				concat1 = "";
				concat1 += "jmp Cons\n";
				result+= concat + concat1 + "\n";
				checkFor = true;
			}
			else if(line.toLowerCase().contains("}") && line.length() > 1 && line.toLowerCase().contains("else")){				checkElse = "jmp Else"+ checkIf;
				concat1 = "";
				concat1 += "jmp Cons";
				result += concat + concat1 + "\n";
				checkFor = true;
			}
			else if(line.toLowerCase().contains("}") && line.length() > 1 && line.toLowerCase().contains("else{")){
				checkElse = "jmp Else"+ checkIf;
				concat1 = "";
				concat1 += "jmp Cons";
				result += concat + concat1 + "\n";
				checkFor = true;
			}
			if(line.toLowerCase().contains("if(")){
				sh = tokenizeIf(line);
				checkIf = u;
				result += "xor bx,bx \nmov bl, "+ sh[0] +"\n";
				result += "cmp bl, "+ sh[1] +"\n "+ sh[3] +" If"+ u +"\n";
				result += "\n If" + u + ":\n";	
				result += "jmp Else"+ checkIf +"\n";
				counter++;
			}
			if(line.toLowerCase().contains("for(")){
				if(line.toLowerCase().contains("for(int")){
					sh = tokenizeFor(line);
					if(sh[3] != " "){
						result += "mov "+ sh[4] +","+ sh[0] +"\nLoop" + u + ": \n"+ sh[3] +" bx\n";
						concat += "cmp "+ sh[4] +","+ sh[1] +"\n "+ sh[2]+ " Loop"+ u +"\n" ;
						counter++;
					}else{
						result += "mov "+ sh[4] +","+ sh[0] +"\nLoop" + u + ": \n";
						concat += "cmp "+ sh[4] +","+ sh[1] +"\n "+ sh[2]+ " Loop"+ u +"\n" ;
						counter++;
					}
				}else{
					sh = tokenizeFor(line);
					if(sh[3] != " "){
						result += "mov "+ sh[4] +","+ sh[0] +"\nLoop" + u + ": \n"+ sh[3] +" bx\n";
						concat += "cmp "+ sh[4] +","+ sh[1] +"\n "+ sh[2]+ " Loop"+ u +"\n" ;
						counter++;
					}else{
						result += "mov "+ sh[4] +","+ sh[0] +"\nLoop" + u + ": \n";
						concat += "cmp "+ sh[4] +","+ sh[1] +"\n "+ sh[2]+ " Loop"+ u +"\n" ;
						counter++;
					}
			}
		}
			 if(line.toLowerCase().contains("int")){
				 if(line.toLowerCase().contains("system.out.println(\"")){
					result += "lea dx, var"+ varCount +"\n mov ah, 09h\n" + "int 21h\n\n"; 
					result += "mov dl, 10\nmov ah, 2\nint 21h\n\n";
					varCount++;
					counter++;
				}
				else if(line.toLowerCase().contains("system.out.print(\"")){
					result += "lea dx, var"+ varCount +"\n mov ah, 09h\n" + "int 21h\n\n"; 
					varCount++;
					counter++;
				}
				else if(line.toLowerCase().contains("system.out.println(")){
					String temp = tokenizeVar(line);
					String indexType = "";
					int indexNum = 0;
					for(int a = 0; a < index; a++){
						if(varNames[a].equals(temp)){
							indexType = varTypes[a];
							indexNum = a;
						}
					}
					if(indexType.equals("int") && isInteger(varValues[indexNum]) && ((Integer.valueOf(varValues[indexNum]) >= 100) && (Integer.valueOf(varValues[indexNum]) < 256))){
						result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov bl, 100 \n" ;
						result += "div bl \nmov ch,ah \nadd al, 48 \nmov dl, al \n mov ah, 02h \nint 21h\n";
						result += "mov ah,0 \nmov al, ch \n mov ch,0 \nxor bl,bl \nmov bl,10 \ndiv bl \nmov ch,ah \nadd al,48 \n mov dl, al\n mov ah,02h\n int 21h\n";
						result += "mov ah,0 \n mov al,ch \nmov ch,0 \nadd al,48 \nmov dl,al \nmov ah,02h \n int 21h\nmov dl, 10 \nmov ah,02h \n int 21h\n\n";
						counter++;
					}
					
					if(indexType.equals("int") && isInteger(varValues[indexNum]) && ((Integer.valueOf(varValues[indexNum]) >= 10) && (Integer.valueOf(varValues[indexNum]) < 100))){
						result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov bl, 10 \n" ;
						result += "div bl \nmov ch,ah \nadd al, 48 \nmov dl, al \n mov ah, 02h \nint 21h\n";
						result += "mov ah,0 \n mov al,ch \nmov ch,0 \nadd al,48 \nmov dl,al \nmov ah,02h \n int 21h\nmov dl, 10 \nmov ah,02h \n int 21h\n\n";
						counter++;
					}
					
					if(indexType.equals("int") && isInteger(varValues[indexNum]) && ((Integer.valueOf(varValues[indexNum]) >= 0) && (Integer.valueOf(varValues[indexNum]) < 10))){
						result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + " \nadd al,48 \nmov dl, al \n";
						result += "mov ah,02h \nint 21h \nmov dl, 10 \nmov ah,02h \nint 21h\n\n";
						counter++;
					}
					if(indexType.equals("int") && (!isInteger(varValues[indexNum]))){
						result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov bl, 100 \n" ;
						result += "div bl \nmov ch,ah \nadd al, 48 \nmov dl, al \n mov ah, 02h \nint 21h\n";
						result += "mov ah,0 \nmov al, ch \n mov ch,0 \nxor bl,bl \nmov bl,10 \ndiv bl \nmov ch,ah \nadd al,48 \n mov dl, al\n mov ah,02h\n int 21h\n";
						result += "mov ah,0 \n mov al,ch \nmov ch,0 \nadd al,48 \nmov dl,al \nmov ah,02h \n int 21h\n\n";
						counter++;
					}
					if(indexType.equals("char")){
						result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov dl, al \n";
						result += "mov ah,02h \nint 21h \nmov dl, 10 \nmov ah,02h \nint 21h\n\n";
						counter++;
					}
					if(indexType.equals("string")){
						result += "xor ax,ax \nxor bx,bx \nmov dx, offset " + varNames[indexNum] + "\n";
						result += "mov ah,09h \nint 21h \nmov dl, 10 \nmov ah,02h \nint 21h\n\n";
						counter++;
					}	
				}else if(line.toLowerCase().contains("system.out.print(")){
					String temp = tokenizeVar(line);
					String indexType = "";
					int indexNum = 0;
					for(int a = 0; a < index; a++){
						if(varNames[a].equals(temp)){
							indexType = varTypes[a];
							indexNum = a;
						}
					}
					if(indexType.equals("int") && isInteger(varValues[indexNum]) && ((Integer.valueOf(varValues[indexNum]) >= 100) && (Integer.valueOf(varValues[indexNum]) < 256))){
						result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov bl, 100 \n" ;
						result += "div bl \nmov ch,ah \nadd al, 48 \nmov dl, al \n mov ah, 02h \nint 21h\n";
						result += "mov ah,0 \nmov al, ch \n mov ch,0 \nxor bl,bl \nmov bl,10 \ndiv bl \nmov ch,ah \nadd al,48 \n mov dl, al\n mov ah,02h\n int 21h\n";
						result += "mov ah,0 \n mov al,ch \nmov ch,0 \nadd al,48 \nmov dl,al \nmov ah,02h \n int 21h\n\n";
						counter++;
					}
					
					if(indexType.equals("int") && isInteger(varValues[indexNum])&& ((Integer.valueOf(varValues[indexNum]) >= 10) && (Integer.valueOf(varValues[indexNum]) < 100))){
						result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov bl, 10 \n" ;
						result += "div bl \nmov ch,ah \nadd al, 48 \nmov dl, al \n mov ah, 02h \nint 21h\n";
						result += "mov ah,0 \n mov al,ch \nmov ch,0 \nadd al,48 \nmov dl,al \nmov ah,02h \n int 21h\n\n";
						counter++;
					}
					
					if(indexType.equals("int") &&isInteger(varValues[indexNum]) && ((Integer.valueOf(varValues[indexNum]) >= 0) && (Integer.valueOf(varValues[indexNum]) < 10))){
						result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + " \nadd al,48 \nmov dl, al \n";
						result += "mov ah,02h \nint 21h \n\n";
						counter++;
					}
					if(indexType.equals("int") && (!isInteger(varValues[indexNum]))){
						result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov bl, 100 \n" ;
						result += "div bl \nmov ch,ah \nadd al, 48 \nmov dl, al \n mov ah, 02h \nint 21h\n";
						result += "mov ah,0 \nmov al, ch \n mov ch,0 \nxor bl,bl \nmov bl,10 \ndiv bl \nmov ch,ah \nadd al,48 \n mov dl, al\n mov ah,02h\n int 21h\n";
						result += "mov ah,0 \n mov al,ch \nmov ch,0 \nadd al,48 \nmov dl,al \nmov ah,02h \n int 21h\n\n";
						counter++;
					}
					if(indexType.equals("char")){
						result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov dl, al \n";
						result += "mov ah,02h \nint 21h \n\n";
						counter++;
					}
					if(indexType.equals("string")){
						result += "xor ax,ax \nxor bx,bx \nmov dx, offset " + varNames[indexNum] + "\n";
						result += "mov ah,09h \nint 21h \n\n";
						counter++;
						}	
					}else if(line.toLowerCase().contains("for(int")){
						continue;
					}
					else{
						String operator = "";
						String[] out = new String[3];
						twoDarr = tokenizeInt(line);
						for(int j =0 ; j < twoDarr.length; j++){
							if(!isInteger(twoDarr[j][1])){
								operator = "+";
								if(twoDarr[j][1].contains(operator)){
									out = tokenizeOp(twoDarr[j][1].replaceAll(" +",""), operator);
									result += "xor ax,ax \nxor bx,bx \nmov al,"+ out[0]+ "\nmov bl,"+out[1]+"\n";
									result += out[2] + " al,bl\n mov "+twoDarr[j][0]+",al";
									counter++;
								}
								operator = "-";
								if(twoDarr[j][1].contains(operator)){
									out = tokenizeOp(twoDarr[j][1].replaceAll(" +",""), operator);
									result += "xor ax,ax \nxor bx,bx \nmov al,"+ out[0]+ "\nmov bl,"+out[1]+"\n";
									result += out[2] + " al,bl\n mov "+twoDarr[j][0]+",al";
									counter++;
								}
							}
						}
					}
				}
				u++;	
		}
	}
	
	private void getElseIfStatements(String[] codes, int index1){
		int length = codes.length;
		String[] sh = new String[4];
		String concat = "";
		String concat1 = "";
		int u = index1;
		counter = 0;
		String[][] twoDarr;
		boolean checkFor = false;
		while((u < length) && (checkFor == false)){
			String line2 = codes[u-1].trim().replaceAll("\\s", "");
			String line = codes[u].trim().replaceAll("\\s", ""); 
			String line1 = codes[u + 1].trim().replaceAll("\\s", ""); 
			if(line.toLowerCase().contains("else{") || line.toLowerCase().contains("else") || line2.toLowerCase().contains("else{") || line2.toLowerCase().contains("else")){
				result += "Else" + checkIf +":\n";
				counter++;
			}
			if(line.toLowerCase().contains("}") && (line.length() == 1)){
				checkElse = "jmp Cons";
				concat1 = "";
				concat1 += "jmp Cons\n Cons:";
				result+= concat + concat1 + "\n";
				checkFor = true;
			}else if(line.toLowerCase().contains("}") && line.length() == 1 && line1.toLowerCase().contains("else{")){
				checkElse = "jmp Else"+ checkIf;
				concat1 = "";
				concat1 += "jmp Cons\n";
				result+= concat + concat1 + "\n";
				checkFor = true;
			}else if(line.toLowerCase().contains("}") && line.length() == 1 && line1.toLowerCase().contains("else")){
				checkElse = "jmp Else"+ checkIf;
				concat1 = "";
				concat1 += "jmp Cons\n";
				result+= concat + concat1 + "\n";
				checkFor = true;
			}
			else if(line.toLowerCase().contains("}") && line.length() > 1 && line.toLowerCase().contains("else")){
				checkElse = "jmp Else"+ checkIf;
				concat1 = "";
				concat1 += "jmp Cons";
				result += concat + concat1 + "\n";
				checkFor = true;
			}
			else if(line.toLowerCase().contains("}") && line.length() > 1 && line.toLowerCase().contains("else{")){
				checkElse = "jmp Else"+ checkIf;
				concat1 = "";
				concat1 += "jmp Cons";
				result += concat + concat1 + "\n";
				checkFor = true;
			}
			if(line.toLowerCase().contains("if(")){
				sh = tokenizeIf(line);
				checkIf = u;
				result += "xor bx,bx \nmov bl, "+ sh[0] +"\n";
				result += "cmp bl, "+ sh[1] +"\n "+ sh[3] +" If"+ u +"\n";
				result += "\n If" + u + ":\n";	
				result += "jmp Else"+ checkIf +"\n";
				counter++;
			}
			if(line.toLowerCase().contains("for(")){
				if(line.toLowerCase().contains("for(int")){
					sh = tokenizeFor(line);
					if(sh[3] != " "){
						result += "mov "+ sh[4] +","+ sh[0] +"\nLoop" + u + ": \n"+ sh[3] +" bx\n";
						concat += "cmp "+ sh[4] +","+ sh[1] +"\n "+ sh[2]+ " Loop"+ u +"\n" ;
						counter++;
					}else{
						result += "mov "+ sh[4] +","+ sh[0] +"\nLoop" + u + ": \n";
						concat += "cmp "+ sh[4] +","+ sh[1] +"\n "+ sh[2]+ " Loop"+ u +"\n" ;
						counter++;
					}
				}else{
					sh = tokenizeFor(line);
					if(sh[3] != " "){
						result += "mov "+ sh[4] +","+ sh[0] +"\nLoop" + u + ": \n"+ sh[3] +" bx\n";
						concat += "cmp "+ sh[4] +","+ sh[1] +"\n "+ sh[2]+ " Loop"+ u +"\n" ;
						counter++;
					}else{
						result += "mov "+ sh[4] +","+ sh[0] +"\nLoop" + u + ": \n";
						concat += "cmp "+ sh[4] +","+ sh[1] +"\n "+ sh[2]+ " Loop"+ u +"\n" ;
						counter++;
					}
			}
		}
			 if(line.toLowerCase().contains("int")){
				 if(line.toLowerCase().contains("system.out.println(\"")){
					result += "lea dx, var"+ varCount +"\n mov ah, 09h\n" + "int 21h\n\n"; 
					result += "mov dl, 10\nmov ah, 2\nint 21h\n\n";
					varCount++;
					counter++;
				}
				else if(line.toLowerCase().contains("system.out.print(\"")){
					result += "lea dx, var"+ varCount +"\n mov ah, 09h\n" + "int 21h\n\n"; 
					varCount++;
					counter++;
				}
				else if(line.toLowerCase().contains("system.out.println(")){
					String temp = tokenizeVar(line);
					String indexType = "";
					int indexNum = 0;
					for(int a = 0; a < index; a++){
						if(varNames[a].equals(temp)){
							indexType = varTypes[a];
							indexNum = a;
						}
					}
					if(indexType.equals("int") && isInteger(varValues[indexNum]) && ((Integer.valueOf(varValues[indexNum]) >= 100) && (Integer.valueOf(varValues[indexNum]) < 256))){
						result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov bl, 100 \n" ;
						result += "div bl \nmov ch,ah \nadd al, 48 \nmov dl, al \n mov ah, 02h \nint 21h\n";
						result += "mov ah,0 \nmov al, ch \n mov ch,0 \nxor bl,bl \nmov bl,10 \ndiv bl \nmov ch,ah \nadd al,48 \n mov dl, al\n mov ah,02h\n int 21h\n";
						result += "mov ah,0 \n mov al,ch \nmov ch,0 \nadd al,48 \nmov dl,al \nmov ah,02h \n int 21h\nmov dl, 10 \nmov ah,02h \n int 21h\n\n";
						counter++;
					}
					
					if(indexType.equals("int") && isInteger(varValues[indexNum]) && ((Integer.valueOf(varValues[indexNum]) >= 10) && (Integer.valueOf(varValues[indexNum]) < 100))){
						result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov bl, 10 \n" ;
						result += "div bl \nmov ch,ah \nadd al, 48 \nmov dl, al \n mov ah, 02h \nint 21h\n";
						result += "mov ah,0 \n mov al,ch \nmov ch,0 \nadd al,48 \nmov dl,al \nmov ah,02h \n int 21h\nmov dl, 10 \nmov ah,02h \n int 21h\n\n";
						counter++;
					}
					
					if(indexType.equals("int") && isInteger(varValues[indexNum]) && ((Integer.valueOf(varValues[indexNum]) >= 0) && (Integer.valueOf(varValues[indexNum]) < 10))){
						result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + " \nadd al,48 \nmov dl, al \n";
						result += "mov ah,02h \nint 21h \nmov dl, 10 \nmov ah,02h \nint 21h\n\n";
						counter++;
					}
					if(indexType.equals("int") && (!isInteger(varValues[indexNum]))){
						result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov bl, 100 \n" ;
						result += "div bl \nmov ch,ah \nadd al, 48 \nmov dl, al \n mov ah, 02h \nint 21h\n";
						result += "mov ah,0 \nmov al, ch \n mov ch,0 \nxor bl,bl \nmov bl,10 \ndiv bl \nmov ch,ah \nadd al,48 \n mov dl, al\n mov ah,02h\n int 21h\n";
						result += "mov ah,0 \n mov al,ch \nmov ch,0 \nadd al,48 \nmov dl,al \nmov ah,02h \n int 21h\n\n";
						counter++;
					}
					if(indexType.equals("char")){
						result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov dl, al \n";
						result += "mov ah,02h \nint 21h \nmov dl, 10 \nmov ah,02h \nint 21h\n\n";
						counter++;
					}
					if(indexType.equals("string")){
						result += "xor ax,ax \nxor bx,bx \nmov dx, offset " + varNames[indexNum] + "\n";
						result += "mov ah,09h \nint 21h \nmov dl, 10 \nmov ah,02h \nint 21h\n\n";
						counter++;
					}	
				}else if(line.toLowerCase().contains("system.out.print(")){
					String temp = tokenizeVar(line);
					String indexType = "";
					int indexNum = 0;
					for(int a = 0; a < index; a++){
						if(varNames[a].equals(temp)){
							indexType = varTypes[a];
							indexNum = a;
						}
					}
					if(indexType.equals("int") && isInteger(varValues[indexNum]) && ((Integer.valueOf(varValues[indexNum]) >= 100) && (Integer.valueOf(varValues[indexNum]) < 256))){
						result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov bl, 100 \n" ;
						result += "div bl \nmov ch,ah \nadd al, 48 \nmov dl, al \n mov ah, 02h \nint 21h\n";
						result += "mov ah,0 \nmov al, ch \n mov ch,0 \nxor bl,bl \nmov bl,10 \ndiv bl \nmov ch,ah \nadd al,48 \n mov dl, al\n mov ah,02h\n int 21h\n";
						result += "mov ah,0 \n mov al,ch \nmov ch,0 \nadd al,48 \nmov dl,al \nmov ah,02h \n int 21h\n\n";
						counter++;
					}
					
					if(indexType.equals("int") && isInteger(varValues[indexNum])&& ((Integer.valueOf(varValues[indexNum]) >= 10) && (Integer.valueOf(varValues[indexNum]) < 100))){
						result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov bl, 10 \n" ;
						result += "div bl \nmov ch,ah \nadd al, 48 \nmov dl, al \n mov ah, 02h \nint 21h\n";
						result += "mov ah,0 \n mov al,ch \nmov ch,0 \nadd al,48 \nmov dl,al \nmov ah,02h \n int 21h\n\n";
						counter++;
					}
					
					if(indexType.equals("int") &&isInteger(varValues[indexNum]) && ((Integer.valueOf(varValues[indexNum]) >= 0) && (Integer.valueOf(varValues[indexNum]) < 10))){
						result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + " \nadd al,48 \nmov dl, al \n";
						result += "mov ah,02h \nint 21h \n\n";
						counter++;
					}
					if(indexType.equals("int") && (!isInteger(varValues[indexNum]))){
						result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov bl, 100 \n" ;
						result += "div bl \nmov ch,ah \nadd al, 48 \nmov dl, al \n mov ah, 02h \nint 21h\n";
						result += "mov ah,0 \nmov al, ch \n mov ch,0 \nxor bl,bl \nmov bl,10 \ndiv bl \nmov ch,ah \nadd al,48 \n mov dl, al\n mov ah,02h\n int 21h\n";
						result += "mov ah,0 \n mov al,ch \nmov ch,0 \nadd al,48 \nmov dl,al \nmov ah,02h \n int 21h\n\n";
						counter++;
					}
					if(indexType.equals("char")){
						result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov dl, al \n";
						result += "mov ah,02h \nint 21h \n\n";
						counter++;
					}
					if(indexType.equals("string")){
						result += "xor ax,ax \nxor bx,bx \nmov dx, offset " + varNames[indexNum] + "\n";
						result += "mov ah,09h \nint 21h \n\n";
						counter++;
						}	
					}else if(line.toLowerCase().contains("for(int")){
						continue;
					}
					else{
						String operator = "";
						String[] out = new String[3];
						twoDarr = tokenizeInt(line);
						for(int j =0 ; j < twoDarr.length; j++){
							if(!isInteger(twoDarr[j][1])){
								operator = "+";
								if(twoDarr[j][1].contains(operator)){
									out = tokenizeOp(twoDarr[j][1].replaceAll(" +",""), operator);
									result += "xor ax,ax \nxor bx,bx \nmov al,"+ out[0]+ "\nmov bl,"+out[1]+"\n";
									result += out[2] + " al,bl\n mov "+twoDarr[j][0]+",al";
									counter++;
								}
								operator = "-";
								if(twoDarr[j][1].contains(operator)){
									out = tokenizeOp(twoDarr[j][1].replaceAll(" +",""), operator);
									result += "xor ax,ax \nxor bx,bx \nmov al,"+ out[0]+ "\nmov bl,"+out[1]+"\n";
									result += out[2] + " al,bl\n mov "+twoDarr[j][0]+",al";
									counter++;
								}
							}
						}
					}
				}
				u++;	
		}
	}
	// ================ END IF ELSE ====================================
	// =============== FOR LOOP ===============================
	private void getForStatements(String[] forCodes, int indexOne){
		int length = forCodes.length;
		counter = 0;
		String[] sh = new String[4];
		String concat = "";
		String concat1 = "";
		int u = indexOne;
		String[][] twoDarr;
		String[] arr;
 		boolean checkFor = false;
		while((u < length) && (checkFor == false)){
			String line = forCodes[u].trim().replaceAll("\\s", ""); 
			if(line.toLowerCase().contains("}") && (line.length() == 1)){
				result+= concat + concat1 + "\n";
				checkFor = true;
			
			}else if(line.toLowerCase().contains("}") && (line.length() > 1) && (line.charAt(0) == '}')){
				result += concat + concat1 + "\n";
				checkFor = true;
			}
			if(line.toLowerCase().contains("if(")){
				sh = tokenizeIf(line);
				result += "mov bx, "+ sh[0] +"\n";
				result += "cmp bx, "+ sh[1] +"\n "+ sh[3] +" If"+ u +"\n jmp Cons\n";
				result += "\n If" + u + ":\n";	
				concat1 += "Cons:";
				counter++;
			}
			if(line.toLowerCase().contains("for(")){
				if(line.toLowerCase().contains("for(int")){
					sh = tokenizeFor(line);
					if(sh[3] != " "){
						result += "mov "+ sh[4] +","+ sh[0] +"\nLoop" + u + ": \n"+ sh[3] +" "+ sh[4] +"\n";
						concat += "cmp "+ sh[4] +","+ sh[1] +"\n "+ sh[2]+ " Loop"+ u +"\n" ;
						counter++;
					}else{
						result += "mov "+ sh[4] +","+ sh[0] +"\nLoop" + u + ": \n";
						concat += "cmp "+ sh[4] +","+ sh[1] +"\n "+ sh[2]+ " Loop"+ u +"\n" ;
						counter++;
					}
				}else{
					sh = tokenizeFor(line);
					if(sh[3] != " "){
						result += "mov "+ sh[4] +","+ sh[0] +"\nLoop" + u + ": \n"+ sh[3] +" "+ sh[4] +"\n";
						concat += "cmp "+ sh[4] +","+ sh[1] +"\n "+ sh[2]+ " Loop"+ u +"\n" ;
						counter++;
					}else{
						result += "mov "+ sh[4] +","+ sh[0] +"\nLoop" + u + ": \n";
						concat += "cmp "+ sh[4] +","+ sh[1] +"\n "+ sh[2]+ " Loop"+ u +"\n" ;
						counter++;
					}
			}
		}	if(line.toLowerCase().contains("+=")){
				arr = tokenMore(line.replaceAll(" +", ""),"+=");
				result += "xor ax,ax \nxor bx,bx \nmov al,"+arr[0]+"\nmov bl,"+arr[1]+"\n";
				result += arr[2]+" al,bl \nmov "+arr[0]+",al\n";
			}
			if(line.toLowerCase().contains("-=")){
				arr = tokenMore(line.replaceAll(" +", ""),"-=");
				result += "xor ax,ax \nxor bx,bx \nmov al,"+arr[0]+"\nmov bl,"+arr[1]+"\n";
				result += arr[2]+" al,bl \nmov "+arr[0]+",al\n";
			} 
			if(line.toLowerCase().contains("int")){
				
				 if(line.toLowerCase().contains("system.out.println(\"")){
					result += "lea dx, var"+ varCount +"\n mov ah, 09h\n" + "int 21h\n\n"; 
					result += "mov dl, 10\nmov ah, 2\nint 21h\n\n";
					varCount++;
					counter++;
				}
				 
				else if(line.toLowerCase().contains("system.out.print(\"")){
					result += "lea dx, var"+ varCount +"\n mov ah, 09h\n" + "int 21h\n\n"; 
					varCount++;
					counter++;
				}
				 
				else if(line.toLowerCase().contains("system.out.println(")){
					String temp = tokenizeVar(line);
					String indexType = "";
					int indexNum = 0;
					for(int a = 0; a < index; a++){
						if(varNames[a].equals(temp)){
							indexType = varTypes[a];
							indexNum = a;
							}
						}
					
			if(indexType.equals("int") && isInteger(varValues[indexNum]) && ((Integer.valueOf(varValues[indexNum]) > 100) && (Integer.valueOf(varValues[indexNum]) < 256))){
						
						result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov bl, 100 \n" ;
						result += "div bl \nmov ch,ah \nadd al, 48 \nmov dl, al \n mov ah, 02h \nint 21h\n";
						result += "mov ah,0 \nmov al, ch \n mov ch,0 \nxor bl,bl \nmov bl,10 \ndiv bl \nmov ch,ah \nadd al,48 \n mov dl, al\n mov ah,02h\n int 21h\n";
						result += "mov ah,0 \n mov al,ch \nmov ch,0 \nadd al,48 \nmov dl,al \nmov ah,02h \n int 21h\nmov dl, 10 \nmov ah,02h \n int 21h\n\n";
						counter++;
			}
					
			if(indexType.equals("int") && isInteger(varValues[indexNum]) && ((Integer.valueOf(varValues[indexNum]) >= 10) && (Integer.valueOf(varValues[indexNum]) < 100))){
						result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov bl, 10 \n" ;
						result += "div bl \nmov ch,ah \nadd al, 48 \nmov dl, al \n mov ah, 02h \nint 21h\n";
						result += "mov ah,0 \n mov al,ch \nmov ch,0 \nadd al,48 \nmov dl,al \nmov ah,02h \n int 21h\nmov dl, 10 \nmov ah,02h \n int 21h\n\n";
						counter++;
			}
					
			if(indexType.equals("int") && isInteger(varValues[indexNum]) && ((Integer.valueOf(varValues[indexNum]) >= 0) && (Integer.valueOf(varValues[indexNum]) < 10))){
						
						result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + " \nadd al,48 \nmov dl, al \n";
						result += "mov ah,02h \nint 21h \nmov dl, 10 \nmov ah,02h \nint 21h\n\n";
						counter++;
			}
			if(indexType.equals("int") && (!isInteger(varValues[indexNum]))){
						result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov bl, 100 \n" ;
						result += "div bl \nmov ch,ah \nadd al, 48 \nmov dl, al \n mov ah, 02h \nint 21h\n";
						result += "mov ah,0 \nmov al, ch \n mov ch,0 \nxor bl,bl \nmov bl,10 \ndiv bl \nmov ch,ah \nadd al,48 \n mov dl, al\n mov ah,02h\n int 21h\n";
						result += "mov ah,0 \n mov al,ch \nmov ch,0 \nadd al,48 \nmov dl,al \nmov ah,02h \n int 21h\n\n";
						counter++;
			}
			if(indexType.equals("char")){
						result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov dl, al \n";
						result += "mov ah,02h \nint 21h \nmov dl, 10 \nmov ah,02h \nint 21h\n\n";
						counter++;
			}
			if(indexType.equals("string")){
						result += "xor ax,ax \nxor bx,bx \nmov dx, offset " + varNames[indexNum] + "\n";
						result += "mov ah,09h \nint 21h \nmov dl, 10 \nmov ah,02h \nint 21h\n\n";
						counter++;
			}	
		}else if(line.toLowerCase().contains("system.out.print(")){
			String temp = tokenizeVar(line);
			String indexType = "";
			int indexNum = 0;
			for(int a = 0; a < index; a++){
				if(varNames[a].equals(temp)){
					indexType = varTypes[a];
					indexNum = a;
				}
			}
			if(indexType.equals("int") && isInteger(varValues[indexNum]) && ((Integer.valueOf(varValues[indexNum]) > 100) && (Integer.valueOf(varValues[indexNum]) < 256))){
				result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov bl, 100 \n" ;
				result += "div bl \nmov ch,ah \nadd al, 48 \nmov dl, al \n mov ah, 02h \nint 21h\n";
				result += "mov ah,0 \nmov al, ch \n mov ch,0 \nxor bl,bl \nmov bl,10 \ndiv bl \nmov ch,ah \nadd al,48 \n mov dl, al\n mov ah,02h\n int 21h\n";
				result += "mov ah,0 \n mov al,ch \nmov ch,0 \nadd al,48 \nmov dl,al \nmov ah,02h \n int 21h\n\n";
				counter++;
			}
			
			if(indexType.equals("int") && isInteger(varValues[indexNum])&& ((Integer.valueOf(varValues[indexNum]) > 10) && (Integer.valueOf(varValues[indexNum]) < 100))){
				result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov bl, 10 \n" ;
				result += "div bl \nmov ch,ah \nadd al, 48 \nmov dl, al \n mov ah, 02h \nint 21h\n";
				result += "mov ah,0 \n mov al,ch \nmov ch,0 \nadd al,48 \nmov dl,al \nmov ah,02h \n int 21h\n\n";
				counter++;
			}
			
			if(indexType.equals("int") &&isInteger(varValues[indexNum]) && ((Integer.valueOf(varValues[indexNum]) >= 0) && (Integer.valueOf(varValues[indexNum]) < 10))){
				result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + " \nadd al,48 \nmov dl, al \n";
				result += "mov ah,02h \nint 21h \n\n";
				counter++;
			}
			if(indexType.equals("int") && (!isInteger(varValues[indexNum]))){
				result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov bl, 100 \n" ;
				result += "div bl \nmov ch,ah \nadd al, 48 \nmov dl, al \n mov ah, 02h \nint 21h\n";
				result += "mov ah,0 \nmov al, ch \n mov ch,0 \nxor bl,bl \nmov bl,10 \ndiv bl \nmov ch,ah \nadd al,48 \n mov dl, al\n mov ah,02h\n int 21h\n";
				result += "mov ah,0 \n mov al,ch \nmov ch,0 \nadd al,48 \nmov dl,al \nmov ah,02h \n int 21h\n\n";
				counter++;
			}
			if(indexType.equals("char")){
				result += "xor ax,ax \nxor bx,bx \nmov al," + varNames[indexNum] + "\nmov dl, al \n";
				result += "mov ah,02h \nint 21h \n\n";
				counter++;
			}
			if(indexType.equals("string")){
				result += "xor ax,ax \nxor bx,bx \nmov dx, offset " + varNames[indexNum] + "\n";
				result += "mov ah,09h \nint 21h \n\n";
				counter++;
			}
			else if(line.toLowerCase().contains("for(int")){
				continue;
					}
			else{
				String operator = "";
				String[] out = new String[3];
				twoDarr = tokenizeInt(line);
				for(int j =0 ; j < twoDarr.length; j++){
					if(!isInteger(twoDarr[j][1])){
						operator = "+";
						if(twoDarr[j][1].contains(operator)){
							out = tokenizeOp(twoDarr[j][1].replaceAll(" +",""), operator);
							result += "xor ax,ax \nxor bx,bx \nmov al,"+ out[0]+ "\nmov bl,"+out[1]+"\n";
							result += out[2] + " al,bl\n mov "+twoDarr[j][0]+",al";
							counter++;
						}
						operator = "-";
						if(twoDarr[j][1].contains(operator)){
							out = tokenizeOp(twoDarr[j][1].replaceAll(" +",""), operator);
							result += "xor ax,ax \nxor bx,bx \nmov al,"+ out[0]+ "\nmov bl,"+out[1]+"\n";
							result += out[2] + " al,bl\n mov "+twoDarr[j][0]+",al";
							counter++;
							}
						}
					}
				}
			}
		}	
			u++;	
			}
	}
	// ======================== END FOR LOOP ================================
	public void writeToASM(String outputPath){
		try {
			File file = new File(outputPath+"\\"+fileName+".asm");
 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(result);
			bw.flush();
			bw.close();
 
			System.out.println("Converted To ASM File in the Directory!");
 
		} catch (IOException e) {
			System.out.println("Error Writing!");
		}
		
	//================ TOKENIZERS FOR VARIABLES ====================================
	}
	private String tokenizeLine (String line){
		StringTokenizer x = new StringTokenizer(line,"\"");
		int xTokens = x.countTokens();
		String[] arr = new String[xTokens];
		int y = 0;
		while(x.hasMoreTokens()){
			arr[y] = x.nextToken();
			y++;
		}
		arr[1] = arr[1].replaceAll("\\\\n", "\",10,\"");
		
		return arr[1];
	}
	private String[][] tokenizeInt(String line){
		String[] arr1 = null;
		boolean check = false;
		if(line.contains(",")){
			check = false;
			StringTokenizer g = new StringTokenizer(line,",");
			int gTokens  = g.countTokens();
			arr1 = new String[gTokens];
			int h= 0;
			while(g.hasMoreElements()){
				arr1[h] = g.nextToken();
				h++;
			}	
		}else{
			check = true;
	}
		StringTokenizer y;
		String[][] output = null;
	if(check){
		if(!line.contains("=")){
			output = new String[1][3];
			arr1 = new String[3];
			arr1[0] = line.substring(3, line.length()-1);
			arr1[1] = "?";
			arr1[2] = "int";
			output[0] = arr1;
			return output;
		}
		y = new StringTokenizer(line,"=");
		int yTokens = y.countTokens();
		String[] arr2 = new String[yTokens + 1];
		int z = 0;
		output = new String[1][arr2.length];
		while(y.hasMoreElements()){
			arr2[z]=y.nextToken();
			z++;
		}
		arr2[0] = arr2[0].substring(3,(arr2[0].length()));	
		int len2 = arr2[1].length();
		arr2[1] = arr2[1].substring(0, len2-1);
		arr2[2] = "int";
		output[0] = arr2;
	}else{
		output = new String[arr1.length][3];
		for (int a = 0; a < arr1.length; a++){
			y = new StringTokenizer(arr1[a], "=");
			int yTokens = y.countTokens();
			String[] arr2 = new String[yTokens + 1];
			String[] temp = new String[yTokens + 1];
			int z = 0;
			while(y.hasMoreElements()){
				arr2[z]=y.nextToken();
				z++;
			}
			temp[2] = "int";
		if(a == 0){
			temp[0] = arr2[0].substring(3,(arr2[0].length()));
			int len2 = arr2[1].length();
			temp[1] = arr2[1].substring(0, len2);
		}
		else{
			temp[0] = arr2[0];
			int len2 = arr2[1].length();
			temp[1] = arr2[1].substring(0, len2-1);
		}
			output[a] = temp;
		}
	}
		return output;
}	
	private String[] tokenizeChar(String line){
		StringTokenizer y = new StringTokenizer(line,"=");
		int yTokens = y.countTokens();
		String[] arr2 = new String[yTokens + 1];
		int z = 0;
		while(y.hasMoreElements()){
			arr2[z]=y.nextToken();
			z++;
		}
		arr2[2] = "char";
		arr2[0] = arr2[0].substring(4,(arr2[0].length()));
		int len2 = arr2[1].length();
		arr2[1] = arr2[1].substring(1, len2-2);
		return arr2;
	}
	private String[] tokenizeString(String line){
		StringTokenizer y = new StringTokenizer(line,"=");
		int yTokens = y.countTokens();
		String[] arr2 = new String[yTokens + 1];
		int z = 0;
		while(y.hasMoreElements()){
			arr2[z]=y.nextToken();
			z++;
		}
		arr2[2] = "string";
		arr2[0] = arr2[0].trim().replaceAll(" +", "");
		arr2[0] = arr2[0].substring(6,(arr2[0].length()));
		int len2 = arr2[1].length();
		if(arr2[1].charAt(0) == ' '){
			arr2[1] = arr2[1].substring(1, (len2)-2);
		}else 
			arr2[1] = arr2[1].substring(0, (len2)-2);
		
		arr2[1] = arr2[1].replaceAll("\\\\n", "\",10,\"");
		return arr2;
	}
	
	private String[] tokenizeBoolean(String line){
		StringTokenizer y = new StringTokenizer(line,"=");
		int yTokens = y.countTokens();
		String[] arr2 = new String[yTokens];
		int z = 0;
		while(y.hasMoreElements()){
			arr2[z]=y.nextToken();
			z++;
		}
		arr2[0] = arr2[0].substring(7,(arr2[0].length()));
		int len2 = arr2[1].length();
		arr2[1] = arr2[1].substring(0, (len2)-1);
		return arr2;
	}
	
	private String tokenizeVar(String line){
		StringTokenizer p = new StringTokenizer(line, ")");
		String newStr = p.nextToken();
		StringTokenizer newP = new StringTokenizer(newStr, "(");
		newP.nextToken();
		return newP.nextToken();
	}
	// ========================= END TOKENIZE VARIABLES ================================
	// ========================= START TOKENIZE CONDITIONS =============================
	private String[] tokenizeFor(String line){
		String out[] = new String[7];
		StringTokenizer l = new StringTokenizer(line,";");
		int lTokens = l.countTokens();
		int a = 0;
		String temp[] = new String[lTokens];
		while(l.hasMoreTokens()){
			temp[a] = l.nextToken();
			a++;
		}
		String[] arr = new String[2];
		arr = tokenFor(temp[0]);
		out[0] = arr[1];
		String[] newArr = new String[3];
		newArr = tokenize(temp[1]);
		out[1] = newArr[1];
		out[2] = newArr[2];
		if(temp[2].contains("++")){
			out[3] = "inc";
		}else if(temp[2].contains("--")){
			out[3] = "dec";
		}else{
			out[3] = " ";
		}
		out[4] = arr[0];
		out[5] = "int";
		//out[0] = temp[0].substring(9, temp[0].length());
		//out[1] = temp[1].substring(2, temp[1].length());
		return out;
	}
	
	private String[] tokenizeIf(String line){
		String out[] = new String[4];
		line = line.replaceAll("\\)", "");
		line = line.replaceAll("\\(", "");
		int ptOftoken = 0;
		StringTokenizer l;
		if(line.contains(">")){
			if(line.contains(">=")){
			ptOftoken = line.charAt(line.indexOf(">"));
			l = new StringTokenizer(line,">=");
			out[3] = "jge";
		}else{
			ptOftoken = line.charAt(line.indexOf(">"));
			l = new StringTokenizer(line,">");
			out[3] = "jg";
			}
		}else if(line.contains("<")){
			if(line.contains("<=")){
			ptOftoken = line.charAt(line.indexOf("<="));
			l = new StringTokenizer(line,"<=");
			out[3] = "jle";
		}else{
			ptOftoken = line.charAt(line.indexOf("<"));
			l = new StringTokenizer(line,"<");
			out[3] = "jl";
			}
		}else{
			ptOftoken = line.charAt(line.indexOf("=="));
			l = new StringTokenizer(line,"==");
			out[3] = "je";
		}
		int lTokens = l.countTokens();
		int a = 0;
		String temp[] = new String[lTokens];
		while(l.hasMoreTokens()){
			temp[a] = l.nextToken();
			a++;
		}
		out[0] = temp[0].substring(2, temp[0].length());
		out[1] = temp[1].substring(0, temp[1].length() - 1);
		return out;
	}
	// =================================== END TOKENIZE CONDITIONS ===============================
	// =================================== CHECKING FUNCTIONS ===========================
	public void printValues(){
		for(int y = 0 ; y < index; y++){
			System.out.print(varNames[y] + " " + varValues[y] + " ");
			System.out.println(varTypes[y]);
			
		}
	}
	private String[] tokenize(String str){
		String x = null;
		String y = null;
		String ret[] = new String[3];
		if(str.contains(">=")){
			x = ">=";
			y = "jge";
		}else if(str.contains("<=")){
			x = "<=";
			y = "jle";
		}
		else if(str.contains(">")){
			x = ">";
			y = "jg";
		}
		else if(str.contains("<")){
			x = "<";
			y = "jl";
		}
		else if(str.contains("==")){
			x = "==";
			y = "je";
		}
		StringTokenizer a = new StringTokenizer(str,x);
		String[] temp = new String[a.countTokens()];
		int z = 0;
		while(a.hasMoreTokens()){
			ret[z] = a.nextToken();
			z++;
		}
			
			ret[2] = y;
		return ret;
	}
	private String[] tokenFor(String str){
		String[] array = new String[2];
		StringTokenizer a = new StringTokenizer(str,"=");
		String[] temp = new String[a.countTokens()];
		int b = 0;
		while(a.hasMoreTokens()){
			temp[b] = a.nextToken();
			b++;
		}
		if(str.contains("for(int")){
			array[0] = temp[0].substring(7, temp[0].length());
		}else if(str.contains("for(")){
			array[0] = temp[0].substring(4, temp[0].length());
		}
			array[1] = temp[1];
		return array;
		
	}
	private static boolean isInteger(String str) {
		if (str == null) {
			return false;
		}
		int length = str.length();
		if (length == 0) {
			return false;
		}
		int i = 0;
		if (str.charAt(0) == '-') {
			if (length == 1) {
				return false;
			}
			i = 1;
		}
		for (; i < length; i++) {
			char c = str.charAt(i);
			if (c <= '/' || c >= ':') {
				return false;
			}
		}
		return true;
	}
	private String[] tokenizeOp(String line, String del){
		String[] output = new String[3];
		StringTokenizer a = new StringTokenizer(line,del);
		output[0] = a.nextToken();
		output[1] = a.nextToken();
		if(del.equals("+")){
		output[2] = "add";
		}else if(del.equals("-")){
		output[2] = "sub";
		}
		return output;
		
	}
	private String[] tokenMore(String line, String del){
		String[] output = new String[3];
		StringTokenizer z = new StringTokenizer(line,del);
		output[0] = z.nextToken();
		output[1] = z.nextToken();
		output[1] = output[1].substring(0, (output[1].length()-1));
			if(del.equals("+=")){
				output[2] = "add";
			}else if(del.equals("-=")){
				output[2] = "sub";
			}
		return output;
	}
}