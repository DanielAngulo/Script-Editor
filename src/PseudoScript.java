public class PseudoScript {
	long address;
	String script;
	
	public PseudoScript(long l, String s){
		address = l;
		script = s;
	}
	
	public String toString(){
		String str = "offset: 0x" + Long.toHexString(address).toUpperCase() + "\n" + script + "\n";
		return str;
	}
}
