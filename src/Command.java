
public class Command {
	String name = "";
	byte value = 0;
	String[] parameters;
	
	public Command(String n, byte v, String[] s){
		name = n;
		value = v;
		parameters = s;
	}
	
	public Command(String n, byte v){
		name = n;
		value = v;
	}
	
	public int getSize(){
		int count = 1;
		if(parameters != null){
			for(int i = 0; i < parameters.length; i++){
				String s = parameters[i].toLowerCase();
				if(s.equals("byte"))
					count++;
				else if(s.equals("pointer") || s.equals("rampointer") || s.equals("dword"))
					count += 4;
				else if(s.equals("word"))
					count += 2;
				else
					count++;
			}
		}
		return count;
	}
	
	public String toString(){
		String other = "";
		String str = name + " ";
		int bite = value;
		if(bite < 0)
			bite += 256;
		if(parameters != null){
			for(String x : parameters)
				other += x + " ";
		}
		return str + Integer.toHexString(bite) + " " + other.trim();
	}
}
