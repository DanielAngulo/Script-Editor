import java.util.ArrayList;
import java.util.Arrays;


public class CompiledScript {
	long address;
	byte[] data;
	
	public CompiledScript(long l){
		address = l;
	}
	
	public CompiledScript(long l, byte[] b){
		address = l;
		data = b;
	}
	
	public String toString(){
		ArrayList<Byte> list = new ArrayList<Byte>();
		for(byte b : data)
			list.add(b);
		return "0x" + Long.toHexString(address) + "\n" + list;
	}
}
