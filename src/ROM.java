import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
public class ROM{

	RandomAccessFile raf; //Used for reading and writing || Usa esto para leer o escribir a la ROM
	File ROM;
	int[] romArray;
	
	private ArrayList<String> text = new ArrayList<String>();
	private ArrayList<Integer> hex = new ArrayList<Integer>();
	
	public ROM(File rom){
		ROM = rom;
	}
	
	public ROM(){
		
	}
	
	public File getFile(){
		return ROM;
	}
	
	public boolean fileLoader(){ //true if they loaded a ROM, false if they did not || verdadero si cargaron su ROM, falso si no
		JFileChooser fileChooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("GBA ROMs", "GBA", "gba");
		fileChooser.setFileFilter(filter);
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		int result = fileChooser.showOpenDialog(null);
		if(result == JFileChooser.APPROVE_OPTION){
			ROM = fileChooser.getSelectedFile();
			return true;
		}
		return false;
	}
	
	public int[] getRomArray() throws IOException{ //returns an array with all of the bytes in the ROM || devuelve un array que tiene todas las bytes en el ROM
		romArray = new int[(int) ROM.length()];
		raf = new RandomAccessFile(ROM, "rw");
		for(int i = 0; i < romArray.length; i++){
			raf.seek(i);
			romArray[i] = raf.readByte();
			if(romArray[i] < 0)
				romArray[i] += 256;
		}
		raf.close();
		return romArray;
	}
	
	public int getByte(long offset) throws IOException{ //returns a byte at specified offset || devuelve una byte en un offset especificado
		raf = new RandomAccessFile(ROM, "rw");
		raf.seek(offset);
		int myByte = (int)raf.readByte();
		if(myByte < 0)
			myByte += 256;
		raf.close();
		return myByte;
	}
	
	public int[] getBytes(long offset, int amount) throws IOException{
		int[] bytes = new int[amount];
		raf = new RandomAccessFile(ROM, "rw");
		for(int i = 0; i < amount; i++){
			raf.seek(offset + i);
			bytes[i] = raf.readByte();
		}
		raf.close();
		bytes = cleanArray(bytes);
		return bytes;
	}
	
	public void setByte(long offset, int writeByte) throws IOException{
		raf = new RandomAccessFile(ROM, "rw");
		raf.seek(offset);
		raf.write(writeByte);
		raf.close();
	}
	
	public void setBytes(long offset, byte[] data) throws IOException{
		raf = new RandomAccessFile(ROM, "rw");
		for(int i = 0; i < data.length; i++){
			raf.seek(offset + i);
			raf.write(data[i]);
		}
		raf.close();
	}
	
	public int[] cleanArray(int[] array){
		for(int i = 0; i < array.length; i++){
			if(array[i] < 0)
				array[i] += 256;
		}
		return array;
	}
	
	public String getGameCode() throws IOException{
		raf = new RandomAccessFile(ROM, "rw");
		raf.seek(0xAC);
		String GameCode = raf.readLine();
		GameCode = GameCode.substring(0, 4);
		raf.close();
		return GameCode;
	}
	
	public long toPointer(long address) throws IOException{
		String str = "";
		int[] bytes = getBytes(address, 4);
		for(int i = 2; i >= 0; i--){
			String temp = Integer.toHexString(bytes[i]).toUpperCase();
			if(temp.length() == 1)
				temp = "0" + temp;
			str += temp;
		}
		long offset = Long.parseLong(str, 16);
		if(bytes[3] > 8)
			offset += 0x1000000;
		return offset;
	}

	public String hexToText(int[] data){
		String str = "";
		for(int i = 0; i < data.length; i++){
			for(int x = 0; x < hex.size(); x++){
				if(hex.get(x) == data[i])
					str += text.get(x);
			}
		}
		return str;
	}
	
	public int[] textToHex(String str, int length){
		int[] name = new int[length];
		int index = 0;
		for(int i = 0; i < str.length(); i++){
			if(str.charAt(i) != '['){
				for(int x = 0; x < hex.size(); x++){
					if(str.charAt(i) == text.get(x).charAt(0)){
						name[index] = hex.get(x);
						x = hex.size();
					}
				}
			}
			else{
				if(getNext(i, str, ']') > -1){
					String part = str.substring(i, getNext(i, str, ']')).trim();
					for(int x = 0; x < hex.size(); x++){
						if(str.equals(text.get(x))){
							name[index] = hex.get(x);
							x = hex.size();
						}
					}
					i += part.length();
				}
				else{
					throw new IllegalArgumentException();
				}
			}
			index++;
		}
		return name;
	}
	
	public void setTables(File file) throws FileNotFoundException{
		Scanner scan = new Scanner(file);
		text.add(" ");
		hex.add(0);
		scan.nextLine();
		while(scan.hasNextLine()){
			String[] split = scan.nextLine().split("=");
			text.add(split[1].trim());
			hex.add(Integer.parseInt(split[0].trim(), 16));
		}
		scan.close();
	}
	
	private int getNext(int i, String str, char c){
		for(int x = i; x < str.length(); x++){
			if(str.charAt(x) == c)
				return x;
		}
		return - 1;
	}
	
	public int[] untilFF(long offset) throws IOException{
		ArrayList<Integer> hex = new ArrayList<Integer>();
		boolean done = false;
		int i = 0;
		while(!done){
			if(getByte(offset + i) == 0xFF){
				done = true;
				break;
			}
			else{
				hex.add(getByte(offset + i));
			}
			i++;
		}
		int[] bytes = new int[hex.size()];
		for(i = 0; i < bytes.length; i++)
			bytes[i] = hex.get(i);
		return bytes;
	}
	
	public long findFF(long offset, int amount) throws IOException{
		int count = 0;
		RandomAccessFile raf = new RandomAccessFile(ROM, "rw");
		boolean found = false;
		while(!found){
			if(count == amount){
				raf.close();
				return offset;
			}
			else{
				raf.seek(offset + count);
				if(raf.readByte() != (byte)0xFF){
					offset += count;
					if(count == 0){
						offset++;
					}
					if(offset + count >= ROM.length()){
						System.out.println("Error. No free space found.");
						throw new IOException();
					}
					count = 0;
				}
				else{
					count++;
				}
			}
		}
		raf.close();
		return offset;
	}
}