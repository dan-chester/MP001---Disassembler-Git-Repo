package disassembler;
import java.io.*;
import java.util.*;

public class AssemblyToJava {

	private String pathName;
	private String result;
	private String fileName;
	private int ifnum = 0, max;
	
	public AssemblyToJava (String pathName) throws IOException{
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
	
	public String javaStart() {
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
		fileName = fileName.substring(0, (fileName.length() - 4));
		result = "class " + fileName + "{\n\tpublic static void main(String[] args){"; 
		return result;
	}
	public String getVariable(String[] lineOfCodes){
		result += "\n\t\t";
		int length = lineOfCodes.length;
		for(int i = 1; i < length; i++){
			lineOfCodes[i] = lineOfCodes[i].trim().replaceAll(", '", ",'");
			if(lineOfCodes[i].toLowerCase().contains(" db '")){
				result += "String ";
				StringTokenizer tokens = new StringTokenizer(lineOfCodes[i]);
				while(tokens.hasMoreTokens()){
					result += tokens.nextToken();
					break;
				}
				result += " = \"";
				tokens = new StringTokenizer(lineOfCodes[i], "'");
				int tokenCount = tokens.countTokens();
				String[] line = new String[tokenCount];
				int index = 0;
				String fixString = "";
				while(tokens.hasMoreTokens()){
					line[index] = tokens.nextToken();
					index++;
				}
				for(int k=1; k<index-1; k++)
					fixString += line[k];
				result += removeConcat(fixString) + "\";\n\t\t";
			}
			else if(lineOfCodes[i].toLowerCase().contains(" db ?")){
				result += "int ";
				StringTokenizer tokens = new StringTokenizer(lineOfCodes[i]);
				while(tokens.hasMoreTokens()){
					result += tokens.nextToken();
					break;
				}
				result += ";\n\t\t";
			}
			else if(lineOfCodes[i].toLowerCase().contains(" db ")){
				result += "int ";
				StringTokenizer tokens = new StringTokenizer(lineOfCodes[i]);
				while(tokens.hasMoreTokens()){
					result += tokens.nextToken();
					break;
				}
				result += " = ";
				int tokenCount = tokens.countTokens();
				String[] line = new String[tokenCount];
				int index = 0;
				while(tokens.hasMoreTokens()){
					line[index] = tokens.nextToken();
					index++;
				}
				result += line[1] + ";\n\t\t";
			}
			if(lineOfCodes[i].toLowerCase().contains("endif_"))
				ifnum += 1;
		}
		max = ifnum;
		return result;
	}
	public String getMain(String[] lineOfCodes, int tabnum){
		int temp = tabnum;
		result += "\n\t\t";
		while(tabnum!=0){
			result += "\t";
			tabnum--;
		}
		tabnum = temp;
		int length = lineOfCodes.length;
		String ax = "", bx = "", cx = "", dx = "";
		for(int i = 0; i < length; i++){
			lineOfCodes[i] = lineOfCodes[i].trim().replaceAll(",", ", ");
			lineOfCodes[i] = lineOfCodes[i].trim().replaceAll(", '", ",'");
			lineOfCodes[i] = lineOfCodes[i].trim().replaceAll(",'0", ", '0");
			StringTokenizer tokens = new StringTokenizer(lineOfCodes[i]);
			int tokenCount = tokens.countTokens();
			String[] line = new String[tokenCount];
			
			if(lineOfCodes[i].toLowerCase().contains("@data") || lineOfCodes[i].toLowerCase().contains("mov ds") || lineOfCodes[i].toLowerCase().contains("4c00h") || lineOfCodes[i].toLowerCase().contains("; ")){
				continue;
			}
			else if(lineOfCodes[i].toLowerCase().contains("beginfor_:")){
				while(!lineOfCodes[i].toLowerCase().contains("cmp")){
					i++;
				}
				tokens = new StringTokenizer(lineOfCodes[i], " ");
				tokenCount = tokens.countTokens();
				line = new String[tokenCount];
				int index = 0;
				while(tokens.hasMoreTokens()){
					line[index] = tokens.nextToken();
					index++;
				}
				result += "for(int " + line[1].replaceAll(",", "") + " = 0; " + line[1].replaceAll(",", "")  + " " + findOperator(lineOfCodes[i+1]);
				if(findOperator(lineOfCodes[i+1]) != "== 0" || findOperator(lineOfCodes[i+1]) != "!= 0"){
					result += " " + line[2];
				}
				//result += "){";
				System.out.println(result);
				int j = i + 2, k = 0;
				
				int count = 0;
				while((!lineOfCodes[j].toLowerCase().contains("inc") || !lineOfCodes[j].toLowerCase().contains("dec")) && j < length){
					count += 1;
					j++;
				}
				j = i + 2;
				String[] insideLine = new String[count];
				while((!lineOfCodes[j].toLowerCase().contains("inc") || !lineOfCodes[j].toLowerCase().contains("dec")) && j < length){
					insideLine[k] = lineOfCodes[j];
					j++;
					k++;
				}
				if(lineOfCodes[j-1].toLowerCase().contains("inc"))
					result += "; " + line[1].replaceAll(",", "") + "++){";
				else if(lineOfCodes[j-1].toLowerCase().contains("dec"))
					result += "; " + line[1].replaceAll(",", "") + "--){";//fuhudhusgufgdufhudfhuhdfudfhudfs
				getMain(insideLine, tabnum+1);
				i += 3 + count;
				result += "\n\t\t}\n\t\t";
				temp = tabnum;
				while(tabnum!=0){
					result += "\t";
					tabnum--;
				}
				tabnum = temp;
			}
			else if(lineOfCodes[i].toLowerCase().contains("cmp")){	
				tokens = new StringTokenizer(lineOfCodes[i], " ");
				tokenCount = tokens.countTokens();
				line = new String[tokenCount];
				int index = 0;
				while(tokens.hasMoreTokens()){
					line[index] = tokens.nextToken();
					index++;
				}
				if(max == ifnum){
					ifnum--;
					result += "if(" + line[1].replaceAll(",", "") + " " + findOperator(lineOfCodes[i+1]);
					if(findOperator(lineOfCodes[i+1]) != "== 0" || findOperator(lineOfCodes[i+1]) != "!= 0"){
						result += " " + line[2];
					}
					result += "){";
				}
				else if(ifnum == 1){
					ifnum--;
					result += "else{";
				}
				else if(ifnum == 0){
					
				}
				else{
					ifnum--;
					result += "else if(" + line[1].replaceAll(",", "") + " " + findOperator(lineOfCodes[i+1]);
					if(findOperator(lineOfCodes[i+1]) != "== 0" || findOperator(lineOfCodes[i+1]) != "!= 0"){
						result += " " + line[2];
					}
					result += "){";
				}
				int j = i + 2, k = 0;
				
				int count = 0;
				while(!lineOfCodes[j].toLowerCase().contains("endif_")  && j < length){
					count += 1;
					j++;
				}
				j = i + 2;
				String[] insideLine = new String[count];
				while(!lineOfCodes[j].toLowerCase().contains("endif_") && j < length){
					insideLine[k] = lineOfCodes[j];
					j++;
					k++;
				}
				getMain(insideLine, tabnum+1);
				i += 2 + count;
				result += "\n\t\t}\n\t\t";
				temp = tabnum;
				while(tabnum!=0){
					result += "\t";
					tabnum--;
				}
				tabnum = temp;
			}
			else if(lineOfCodes[i].toLowerCase().contains("lea dx") || lineOfCodes[i].toLowerCase().contains("offset")){
				result += "System.out.print(";
				int index = 0;
				if(tokenCount == 3){
					tokens = new StringTokenizer(lineOfCodes[i], ",");
					tokenCount = tokens.countTokens();
					line = new String[tokenCount];
					index = 0;
					while(tokens.hasMoreTokens()){
						line[index] = tokens.nextToken();
						index++;
					}
					result += line[index-1].replaceAll(" ", "");
				}
				else if(tokenCount == 4){
					tokens = new StringTokenizer(lineOfCodes[i], " ");
					tokenCount = tokens.countTokens();
					line = new String[tokenCount];
					index = 0;
					while(tokens.hasMoreTokens()){
						line[index] = tokens.nextToken();
						index++;
					}
					for(int k=3; k<index; k++){
						result += line[k].replaceAll("offset", "");
					}
				}
				else{
					result += "\"";
					tokens = new StringTokenizer(lineOfCodes[i], "'");
					tokenCount = tokens.countTokens();
					line = new String[tokenCount];
					index = 0;
					String fixString = "";
					while(tokens.hasMoreTokens()){
						line[index] = tokens.nextToken();
						index++;
					}
					for(int k=1; k<index-1; k++)
						fixString += line[k];
					result += removeConcat(fixString) + "\"";
				}
				result += ");\n\t\t";
				temp = tabnum;
				while(tabnum!=0){
					result += "\t";
					tabnum--;
				}
				tabnum = temp;
			}
			else if(lineOfCodes[i].toLowerCase().contains("mov ah") || lineOfCodes[i].toLowerCase().contains("mov al") || lineOfCodes[i].toLowerCase().contains("mov ax") || lineOfCodes[i].toLowerCase().contains("mov eax") ||
					lineOfCodes[i].toLowerCase().contains("mov bh") || lineOfCodes[i].toLowerCase().contains("mov bl") || lineOfCodes[i].toLowerCase().contains("mov bx") || lineOfCodes[i].toLowerCase().contains("mov ebx") ||
					lineOfCodes[i].toLowerCase().contains("mov ch") || lineOfCodes[i].toLowerCase().contains("mov cl") || lineOfCodes[i].toLowerCase().contains("mov cx") || lineOfCodes[i].toLowerCase().contains("mov ecx") ||
					lineOfCodes[i].toLowerCase().contains("mov dh") || lineOfCodes[i].toLowerCase().contains("mov dl") || lineOfCodes[i].toLowerCase().contains("mov dx") || lineOfCodes[i].toLowerCase().contains("mov edx")){
				tokens = new StringTokenizer(lineOfCodes[i], " ");
				tokenCount = tokens.countTokens();
				line = new String[tokenCount];
				int index = 0;
				while(tokens.hasMoreTokens()){
					line[index] = tokens.nextToken();
					index++;
				}
				if(line[1].toLowerCase().contains("ah") || line[1].toLowerCase().contains("al") || line[1].toLowerCase().contains("ax")){
					ax = line[2];
				}
				if(line[1].toLowerCase().contains("bh") || line[1].toLowerCase().contains("bl") || line[1].toLowerCase().contains("bx")){
					bx = line[2];
				}
				if(line[1].toLowerCase().contains("ch") || line[1].toLowerCase().contains("cl") || line[1].toLowerCase().contains("cx")){
					cx = line[2];
				}
				if(line[1].toLowerCase().contains("dh") || line[1].toLowerCase().contains("dl") || line[1].toLowerCase().contains("dx")){
					dx = line[2];
				}
			}
			else if(lineOfCodes[i].toLowerCase().contains("add ah") || lineOfCodes[i].toLowerCase().contains("add al") || lineOfCodes[i].toLowerCase().contains("add ax") || lineOfCodes[i].toLowerCase().contains("add eax") ||
					lineOfCodes[i].toLowerCase().contains("add bh") || lineOfCodes[i].toLowerCase().contains("add bl") || lineOfCodes[i].toLowerCase().contains("add bx") || lineOfCodes[i].toLowerCase().contains("add ebx") ||
					lineOfCodes[i].toLowerCase().contains("add ch") || lineOfCodes[i].toLowerCase().contains("add cl") || lineOfCodes[i].toLowerCase().contains("add cx") || lineOfCodes[i].toLowerCase().contains("add ecx") ||
					lineOfCodes[i].toLowerCase().contains("add dh") || lineOfCodes[i].toLowerCase().contains("add dl") || lineOfCodes[i].toLowerCase().contains("add dx") || lineOfCodes[i].toLowerCase().contains("add edx") ||
					lineOfCodes[i].toLowerCase().contains("sub ah") || lineOfCodes[i].toLowerCase().contains("sub al") || lineOfCodes[i].toLowerCase().contains("sub ax") || lineOfCodes[i].toLowerCase().contains("sub eax") ||
					lineOfCodes[i].toLowerCase().contains("sub bh") || lineOfCodes[i].toLowerCase().contains("sub bl") || lineOfCodes[i].toLowerCase().contains("sub bx") || lineOfCodes[i].toLowerCase().contains("sub ebx") ||
					lineOfCodes[i].toLowerCase().contains("sub ch") || lineOfCodes[i].toLowerCase().contains("sub cl") || lineOfCodes[i].toLowerCase().contains("sub cx") || lineOfCodes[i].toLowerCase().contains("sub ecx") ||
					lineOfCodes[i].toLowerCase().contains("sub dh") || lineOfCodes[i].toLowerCase().contains("sub dl") || lineOfCodes[i].toLowerCase().contains("sub dx") || lineOfCodes[i].toLowerCase().contains("sub edx")){
				tokens = new StringTokenizer(lineOfCodes[i], " ");
				tokenCount = tokens.countTokens();
				line = new String[tokenCount];
				int index = 0;
				while(tokens.hasMoreTokens()){
					line[index] = tokens.nextToken();
					index++;
				}
				if(line[1].toLowerCase().contains("ah") || line[1].toLowerCase().contains("al") || line[1].toLowerCase().contains("ax")){
					if(lineOfCodes[i].toLowerCase().contains("add"))
						ax = ax + " + " + line[2];
					else
						ax = ax + " - " + line[2];
				}
				if(line[1].toLowerCase().contains("bh") || line[1].toLowerCase().contains("bl") || line[1].toLowerCase().contains("bx")){
					if(lineOfCodes[i].toLowerCase().contains("add"))
						bx = bx + " + " + line[2];
					else
						bx = bx + " - " + line[2];
				}
				if(line[1].toLowerCase().contains("ch") || line[1].toLowerCase().contains("cl") || line[1].toLowerCase().contains("cx")){
					if(lineOfCodes[i].toLowerCase().contains("add"))
						cx = cx + " + " + line[2];
					else
						cx = cx + " - " + line[2];
				}
				if(line[1].toLowerCase().contains("dh") || line[1].toLowerCase().contains("dl") || line[1].toLowerCase().contains("dx")){
					if(lineOfCodes[i].toLowerCase().contains("add"))
						dx = dx + " + " + line[2];
					else
						dx = dx + " - " + line[2];
				}
			}
			else if(lineOfCodes[i].toLowerCase().contains("mov")){
				tokens = new StringTokenizer(lineOfCodes[i], " ");
				tokenCount = tokens.countTokens();
				line = new String[tokenCount];
				int index = 0;
				while(tokens.hasMoreTokens()){
					line[index] = tokens.nextToken();
					index++;
				}
				result += line[1].replaceAll(",", "") + " = ";
				if(line[2].toLowerCase().contains("ah") || line[2].toLowerCase().contains("al") || line[2].toLowerCase().contains("ax")){
					result += ax;
				}
				if(line[2].toLowerCase().contains("bh") || line[2].toLowerCase().contains("bl") || line[2].toLowerCase().contains("bx")){
					result += bx;
				}
				if(line[2].toLowerCase().contains("ch") || line[2].toLowerCase().contains("cl") || line[2].toLowerCase().contains("cx")){
					result += cx;
				}
				if(line[2].toLowerCase().contains("dh") || line[2].toLowerCase().contains("dl") || line[2].toLowerCase().contains("dx")){
					result += dx;
				}
				result += ";\n\t\t";
				temp = tabnum;
				while(tabnum!=0){
					result += "\t";
					tabnum--;
				}
				tabnum = temp;
			}
			else if(lineOfCodes[i].toLowerCase().contains("add") || lineOfCodes[i].toLowerCase().contains("sub")){
				tokens = new StringTokenizer(lineOfCodes[i], " ");
				tokenCount = tokens.countTokens();
				line = new String[tokenCount];
				int index = 0;
				while(tokens.hasMoreTokens()){
					line[index] = tokens.nextToken();
					index++;
				}
				if(line[2].toLowerCase().contains("ah") || line[2].toLowerCase().contains("al") || line[2].toLowerCase().contains("ax")){
					result += line[1].replaceAll(",", "");
					if(lineOfCodes[i].toLowerCase().contains("add"))
						result += " += ";
					else
						result += " -= ";
					result += ax;
				}
				else if(line[2].toLowerCase().contains("bh") || line[2].toLowerCase().contains("bl") || line[2].toLowerCase().contains("bx")){
					result += line[1].replaceAll(",", "");
					if(lineOfCodes[i].toLowerCase().contains("add"))
						result += " += ";
					else
						result += " -= ";
					result += bx;
				}
				else if(line[2].toLowerCase().contains("ch") || line[2].toLowerCase().contains("cl") || line[2].toLowerCase().contains("cx")){
					result += line[1].replaceAll(",", "");
					if(lineOfCodes[i].toLowerCase().contains("add"))
						result += " += ";
					else
						result += " -= ";
					result += bx;
				}
				else if(line[2].toLowerCase().contains("dh") || line[2].toLowerCase().contains("dl") || line[2].toLowerCase().contains("dx")){
					result += line[1].replaceAll(",", "");
					if(lineOfCodes[i].toLowerCase().contains("add"))
						result += " += ";
					else
						result += " -= ";
					result += dx;
				}
				else if(line[2].toLowerCase().contains("48") || line[2].toLowerCase().contains("'0'")){
					result += "System.out.print(" + line[1].replaceAll(",", "") + ")";
				}
				else{
					result += line[1].replaceAll(",", "");
					if(lineOfCodes[i].toLowerCase().contains("add"))
						result += " += ";
					else
						result += " -= ";
					result += line[2];
				}
				result += ";";
			}
		}
		if(tabnum==0){
			result += "\n\t}\n}";
		}
		System.out.println(result);
		return result;
	}
	
	
	
	/*private void getForStatements(String[] forCodes, int index){
		int length = forCodes.length;
		String[] sh = new String[2];
		String concat = "";
		int u = index, v = 0;
		boolean checkFor = false;
		while((u < length) && (checkFor == false)){
			String line = forCodes[u].trim().replaceAll("\\s", ""); 
			if(line.toLowerCase().contains("for(")){
				sh = tokenizeFor(line);
				result += "mov bx, "+ sh[0] +"\nLoop" + u + ": \ninc bx\n";
				concat += "cmp bx, "+ sh[1] +"\n jl Loop"+ u ;
				counter++;
			}
			if(line.toLowerCase().contains("system.out.println(\"")){
				result += "lea dx, var"+ v +"\n mov ah, 09h\n" + "int 21h\n\n"; 
				result += "mov dl, 10\nmov ah, 2\nint 21h\n\n";
				v++;
				counter++;
			}
			
			if(line.toLowerCase().contains("system.out.print(\"")){
				result += "lea dx, var"+ v +"\n mov ah, 09h\n" + "int 21h\n\n"; 
				v++;
				counter++;
			}
			if(line.toLowerCase().contains("}") && (line.length() == 1)){
				result+= concat;
				counter++;
				checkFor = true;
			
			}else if(line.toLowerCase().contains("}") && (line.length() > 1) && (line.charAt(0) == '}')){
				result += concat;
				checkFor = true;
			}
				u++;	
		}
	}
	
	*/
	public void writeToJAVA(String outputPath){
		try {
			File file = new File(outputPath+"\\"+fileName+".java");
 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(result);
			bw.flush();
			bw.close();
 
			System.out.println("Done");
 
		} catch (IOException e) {
			System.out.println("Error Writing!");
		}
		
	
	}
	/*private String tokenizeLine (String line){
		StringTokenizer x = new StringTokenizer(line,"'");
		int xTokens = x.countTokens();
		
		String[] arr = new String[xTokens];
		int y = 0;
		while(x.hasMoreTokens()){
			arr[y] = x.nextToken();
			y++;
		}
		return arr[2];
	}
	private String[] tokenizeInt(String line){
		StringTokenizer y = new StringTokenizer(line);
		int yTokens = y.countTokens();
		String[] arr2 = new String[yTokens];
		int z = 0;
		while(y.hasMoreElements()){
			arr2[z]=y.nextToken();
			z++;
		}
		arr2[0] = arr2[0].substring(3,(arr2[0].length()));
		System.out.println(arr2[0]);	
		int len2 = arr2[1].length();
		arr2[1] = arr2[1].substring(0, len2-1);
		return arr2;
	}
	private String[] tokenizeChar(String line){
		StringTokenizer y = new StringTokenizer(line,"=");
		int yTokens = y.countTokens();
		String[] arr2 = new String[yTokens];
		int z = 0;
		while(y.hasMoreElements()){
			arr2[z]=y.nextToken();
			z++;
		}
		arr2[0] = arr2[0].substring(4,(arr2[0].length()));
		int len2 = arr2[1].length();
		arr2[1] = arr2[1].substring(1, len2-2);
		return arr2;
	}
	private String[] tokenizeString(String line){
		StringTokenizer y = new StringTokenizer(line,"=");
		int yTokens = y.countTokens();
		String[] arr2 = new String[yTokens];
		int z = 0;
		while(y.hasMoreElements()){
			arr2[z]=y.nextToken();
			z++;
		}
		System.out.println(arr2[0]);
		System.out.println(arr2[1]);
		arr2[0] = arr2[0].substring(6,(arr2[0].length()));
		int len2 = arr2[1].length();
		arr2[1] = arr2[1].substring(1, (len2)-2);
		return arr2;
	}
	private String[] tokenizeFor(String line){
		String out[] = new String[2];
		StringTokenizer l = new StringTokenizer(line,";");
		int lTokens = l.countTokens();
		int a = 0;
		String temp[] = new String[lTokens];
		while(l.hasMoreTokens()){
			temp[a] = l.nextToken();
			a++;
		}
		out[0] = temp[0].substring(9, temp[0].length());
		out[1] = temp[1].substring(2, temp[1].length());
		return out;
	}*/
	private String removeConcat(String c){
		String temp = "";
		int count = 0;
		for(int k=0; k<c.length(); k++){
			if(c.charAt(k) == ','){
				if(k+7 < c.length()){
					if(((c.charAt(k+2) == '0' || c.charAt(k+2) == '1' || c.charAt(k+2) == '2' || c.charAt(k+2) == '3' ||
							c.charAt(k+2) == '4' || c.charAt(k+2) == '5' || c.charAt(k+2) == '6' || c.charAt(k+2) == '7' ||
							c.charAt(k+2) == '8' || c.charAt(k+2) == '9') || (c.charAt(k+3) == '0' || c.charAt(k+3) == '1' ||
							c.charAt(k+3) == '2' || c.charAt(k+3) == '3' || c.charAt(k+3) == '4' || c.charAt(k+3) == '5' ||
							c.charAt(k+3) == '6' || c.charAt(k+3) == '7' || c.charAt(k+3) == '8' || c.charAt(k+3) == '9'))
							&& (c.charAt(k+4) == ',' || c.charAt(k+5) == ',' || c.charAt(k+6) == ',' || c.charAt(k+7) == ',')){
						count += 1;
					}
				}
				else if(k+6 < c.length()){
					if(((c.charAt(k+2) == '0' || c.charAt(k+2) == '1' || c.charAt(k+2) == '2' || c.charAt(k+2) == '3' ||
							c.charAt(k+2) == '4' || c.charAt(k+2) == '5' || c.charAt(k+2) == '6' || c.charAt(k+2) == '7' ||
							c.charAt(k+2) == '8' || c.charAt(k+2) == '9') || (c.charAt(k+3) == '0' || c.charAt(k+3) == '1' ||
							c.charAt(k+3) == '2' || c.charAt(k+3) == '3' || c.charAt(k+3) == '4' || c.charAt(k+3) == '5' ||
							c.charAt(k+3) == '6' || c.charAt(k+3) == '7' || c.charAt(k+3) == '8' || c.charAt(k+3) == '9'))
							&& (c.charAt(k+4) == ',' || c.charAt(k+5) == ',' || c.charAt(k+6) == ',')){
						count += 1;
					}
				}
				else if(k+5 < c.length()){
					if(((c.charAt(k+2) == '0' || c.charAt(k+2) == '1' || c.charAt(k+2) == '2' || c.charAt(k+2) == '3' ||
							c.charAt(k+2) == '4' || c.charAt(k+2) == '5' || c.charAt(k+2) == '6' || c.charAt(k+2) == '7' ||
							c.charAt(k+2) == '8' || c.charAt(k+2) == '9') || (c.charAt(k+3) == '0' || c.charAt(k+3) == '1' ||
							c.charAt(k+3) == '2' || c.charAt(k+3) == '3' || c.charAt(k+3) == '4' || c.charAt(k+3) == '5' ||
							c.charAt(k+3) == '6' || c.charAt(k+3) == '7' || c.charAt(k+3) == '8' || c.charAt(k+3) == '9'))
							&& (c.charAt(k+4) == ',' || c.charAt(k+5) == ',')){
						count += 1;
					}
				}
				else if(k+4 < c.length()){
					if(((c.charAt(k+2) == '0' || c.charAt(k+2) == '1' || c.charAt(k+2) == '2' || c.charAt(k+2) == '3' ||
							c.charAt(k+2) == '4' || c.charAt(k+2) == '5' || c.charAt(k+2) == '6' || c.charAt(k+2) == '7' ||
							c.charAt(k+2) == '8' || c.charAt(k+2) == '9') || (c.charAt(k+3) == '0' || c.charAt(k+3) == '1' ||
							c.charAt(k+3) == '2' || c.charAt(k+3) == '3' || c.charAt(k+3) == '4' || c.charAt(k+3) == '5' ||
							c.charAt(k+3) == '6' || c.charAt(k+3) == '7' || c.charAt(k+3) == '8' || c.charAt(k+3) == '9'))
							&& (c.charAt(k+4) == ',')){
						count += 1;
					}
				}
			}
		}
		for(int j=0; j<count; j++){
			for(int k=0; k<c.length(); k++){
				if(c.charAt(k) == ','){
					if(k+7 < c.length()){
						if(((c.charAt(k+2) == '0' || c.charAt(k+2) == '1' || c.charAt(k+2) == '2' || c.charAt(k+2) == '3' ||
								c.charAt(k+2) == '4' || c.charAt(k+2) == '5' || c.charAt(k+2) == '6' || c.charAt(k+2) == '7' ||
								c.charAt(k+2) == '8' || c.charAt(k+2) == '9') || (c.charAt(k+3) == '0' || c.charAt(k+3) == '1' ||
								c.charAt(k+3) == '2' || c.charAt(k+3) == '3' || c.charAt(k+3) == '4' || c.charAt(k+3) == '5' ||
								c.charAt(k+3) == '6' || c.charAt(k+3) == '7' || c.charAt(k+3) == '8' || c.charAt(k+3) == '9'))
								&& (c.charAt(k+4) == ',' || c.charAt(k+5) == ',' || c.charAt(k+6) == ',' || c.charAt(k+7) == ',')){
							temp = c.substring(k+1);
							c = c.substring(0, k);
						}
					}
					else if(k+6 < c.length()){
						if(((c.charAt(k+2) == '0' || c.charAt(k+2) == '1' || c.charAt(k+2) == '2' || c.charAt(k+2) == '3' ||
								c.charAt(k+2) == '4' || c.charAt(k+2) == '5' || c.charAt(k+2) == '6' || c.charAt(k+2) == '7' ||
								c.charAt(k+2) == '8' || c.charAt(k+2) == '9') || (c.charAt(k+3) == '0' || c.charAt(k+3) == '1' ||
								c.charAt(k+3) == '2' || c.charAt(k+3) == '3' || c.charAt(k+3) == '4' || c.charAt(k+3) == '5' ||
								c.charAt(k+3) == '6' || c.charAt(k+3) == '7' || c.charAt(k+3) == '8' || c.charAt(k+3) == '9'))
								&& (c.charAt(k+4) == ',' || c.charAt(k+5) == ',' || c.charAt(k+6) == ',')){
							temp = c.substring(k+1);
							c = c.substring(0, k);
						}
					}
					else if(k+5 < c.length()){
						if(((c.charAt(k+2) == '0' || c.charAt(k+2) == '1' || c.charAt(k+2) == '2' || c.charAt(k+2) == '3' ||
								c.charAt(k+2) == '4' || c.charAt(k+2) == '5' || c.charAt(k+2) == '6' || c.charAt(k+2) == '7' ||
								c.charAt(k+2) == '8' || c.charAt(k+2) == '9') || (c.charAt(k+3) == '0' || c.charAt(k+3) == '1' ||
								c.charAt(k+3) == '2' || c.charAt(k+3) == '3' || c.charAt(k+3) == '4' || c.charAt(k+3) == '5' ||
								c.charAt(k+3) == '6' || c.charAt(k+3) == '7' || c.charAt(k+3) == '8' || c.charAt(k+3) == '9'))
								&& (c.charAt(k+4) == ',' || c.charAt(k+5) == ',')){
							temp = c.substring(k+1);
							c = c.substring(0, k);
						}
					}
					else if(k+4 < c.length()){
						if(((c.charAt(k+2) == '0' || c.charAt(k+2) == '1' || c.charAt(k+2) == '2' || c.charAt(k+2) == '3' ||
								c.charAt(k+2) == '4' || c.charAt(k+2) == '5' || c.charAt(k+2) == '6' || c.charAt(k+2) == '7' ||
								c.charAt(k+2) == '8' || c.charAt(k+2) == '9') || (c.charAt(k+3) == '0' || c.charAt(k+3) == '1' ||
								c.charAt(k+3) == '2' || c.charAt(k+3) == '3' || c.charAt(k+3) == '4' || c.charAt(k+3) == '5' ||
								c.charAt(k+3) == '6' || c.charAt(k+3) == '7' || c.charAt(k+3) == '8' || c.charAt(k+3) == '9'))
								&& (c.charAt(k+4) == ',')){
							temp = c.substring(k+1);
							c = c.substring(0, k);
						}
					}
				}
			}
			for(int k=0; k<temp.length(); k++){
				if(temp.charAt(k) == ','){
					c += "@@@" + temp.substring(k+1);
					temp = temp.substring(0, k);
				}
			}
			temp = temp.replaceAll(" ", "");
			String asciiEq = "";
			
			switch (temp){
				case "10": 	asciiEq = "\\n";
							break;
				case "32": 	asciiEq = " ";
							break;
				case "33": 	asciiEq = "!";
							break;
				case "34": 	asciiEq = "\"";
							break;
				case "35": 	asciiEq = "#";
							break;
				case "36": 	asciiEq = "$";
							break;
				case "37": 	asciiEq = "%";
							break;
				case "38": 	asciiEq = "&";
							break;
				case "39": 	asciiEq = "'";
							break;
				case "40": 	asciiEq = "(";
							break;
				case "41": 	asciiEq = ")";
							break;
				case "42": 	asciiEq = "*";
							break;
				case "43": 	asciiEq = "+";
							break;
				case "44": 	asciiEq = ",";
							break;
				case "45": 	asciiEq = "-";
							break;
				case "46": 	asciiEq = ".";
							break;
				case "47": 	asciiEq = "/";
							break;
				case "48": 	asciiEq = "0";
							break;
				case "49": 	asciiEq = "1";
							break;
				case "50": 	asciiEq = "2";
							break;
				case "51": 	asciiEq = "3";
							break;
				case "52": 	asciiEq = "4";
							break;
				case "53": 	asciiEq = "5";
							break;
				case "54": 	asciiEq = "6";
							break;
				case "55": 	asciiEq = "7";
							break;
				case "56": 	asciiEq = "8";
							break;
				case "57": 	asciiEq = "9";
							break;
				case "58": 	asciiEq = ":";
							break;
				case "59": 	asciiEq = ";";
							break;
				case "60": 	asciiEq = "<";
							break;
				case "61": 	asciiEq = "=";
							break;
				case "62": 	asciiEq = ">";
							break;
				case "63": 	asciiEq = "?";
							break;
				case "64": 	asciiEq = "@";
							break;
				case "65": 	asciiEq = "A";
							break;
				case "66": 	asciiEq = "B";
							break;
				case "67": 	asciiEq = "C";
							break;
				case "68": 	asciiEq = "D";
							break;
				case "69": 	asciiEq = "E";
							break;
				case "70": 	asciiEq = "F";
							break;
				case "71": 	asciiEq = "G";
							break;
				case "72": 	asciiEq = "H";
							break;
				case "73": 	asciiEq = "I";
							break;
				case "74": 	asciiEq = "J";
							break;
				case "75": 	asciiEq = "K";
							break;
				case "76": 	asciiEq = "L";
							break;
				case "77": 	asciiEq = "M";
							break;
				case "78": 	asciiEq = "N";
							break;
				case "79": 	asciiEq = "O";
							break;
				case "80": 	asciiEq = "P";
							break;
				case "81": 	asciiEq = "Q";
							break;
				case "82": 	asciiEq = "R";
							break;
				case "83": 	asciiEq = "S";
							break;
				case "84": 	asciiEq = "T";
							break;
				case "85": 	asciiEq = "U";
							break;
				case "86": 	asciiEq = "V";
							break;
				case "87": 	asciiEq = "W";
							break;
				case "88": 	asciiEq = "X";
							break;
				case "89": 	asciiEq = "Y";
							break;
				case "90": 	asciiEq = "Z";
							break;
				case "91": 	asciiEq = "[";
							break;
				case "92": 	asciiEq = "\\";
							break;
				case "93": 	asciiEq = "]";
							break;
				case "94": 	asciiEq = "^";
							break;
				case "95": 	asciiEq = "_";
							break;
				case "96": 	asciiEq = "`";
							break;
				case "97": 	asciiEq = "a";
							break;
				case "98": 	asciiEq = "b";
							break;
				case "99": 	asciiEq = "c";
							break;
				case "100": asciiEq = "d";
							break;
				case "101": asciiEq = "e";
							break;
				case "102": asciiEq = "f";
							break;
				case "103": asciiEq = "g";
							break;
				case "104": asciiEq = "h";
							break;
				case "105": asciiEq = "i";
							break;
				case "106": asciiEq = "j";
							break;
				case "107": asciiEq = "k";
							break;
				case "108": asciiEq = "l";
							break;
				case "109": asciiEq = "m";
							break;
				case "110": asciiEq = "n";
							break;
				case "111": asciiEq = "o";
							break;
				case "112": asciiEq = "p";
							break;
				case "113": asciiEq = "q";
							break;
				case "114": asciiEq = "r";
							break;
				case "115": asciiEq = "s";
							break;
				case "116": asciiEq = "t";
							break;
				case "117": asciiEq = "u";
							break;
				case "118": asciiEq = "v";
							break;
				case "119": asciiEq = "w";
							break;
				case "120": asciiEq = "x";
							break;
				case "121": asciiEq = "y";
							break;
				case "122": asciiEq = "z";
							break;
				case "123": asciiEq = "{";
							break;
				case "124": asciiEq = "|";
							break;
				case "125": asciiEq = "}";
							break;
				case "126": asciiEq = "~";
							break;
				default:	asciiEq = "";
							break;
			}
			for(int k=0; k<c.length(); k++){
				if(c.charAt(k) == '@'){
					c = c.substring(0, k) + asciiEq + c.substring(k+3);
				}
			}
		}
		return c;
	}
	private String findOperator(String f){
		StringTokenizer tokens = new StringTokenizer(f);
		tokens = new StringTokenizer(f, " ");
		int tokenCount = tokens.countTokens();
		String[] line = new String[tokenCount];
		int index = 0;
		String temp = "";
		while(tokens.hasMoreTokens()){
			line[index] = tokens.nextToken();
			index++;
		}
		switch (line[0]){
			case "je":	f = "==";
						break;
			case "jne":	f = "!=";
						break;
			case "jg":	f = ">";
						break;
			case "jl":	f = "<";
						break;
			case "jge":	f = ">=";
						break;
			case "jle":	f = "<=";
						break;
			case "jz":	f = "== 0";
						break;
			case "jnz":	f = "!= 0";
						break;
			default:	f = "";
						break;
		}
		return f;
	}
}
