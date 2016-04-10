import java.awt.EventQueue;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JMenuItem;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JRadioButton;


public class Main {

	private JFrame frmDannysScriptEditor;
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main window = new Main();
					window.frmDannysScriptEditor.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public Main() throws FileNotFoundException {
		initialize();
	}
	
	//IO
	ROM tools = new ROM();
	File ROM;
	long offset;
	
	//Scripts
	ArrayList<Command> Commands = new ArrayList<Command>(); //All read from commands.txt
	ArrayList<PseudoScript> preCompiled = new ArrayList<PseudoScript>(); //Scripts with the commands before being compiled
	ArrayList<CompiledScript> Compiled = new ArrayList<CompiledScript>();//above, but compiled
	
	//GUI components
	private JMenuBar menuBar;
	private JTabbedPane Tabs;
	private JPanel FirstTab;
	private JScrollPane scrollPane;
	private JMenuItem LoadROM;
	private JTextArea ScriptBox;
	private JMenuItem Compile;
	private JRadioButton XSERB;
	private JRadioButton ProgrammingRB;
	
	private void initialize() throws FileNotFoundException {
		frmDannysScriptEditor = new JFrame();
		frmDannysScriptEditor.setResizable(false);
		frmDannysScriptEditor.setTitle("Danny's Script Editor");
		frmDannysScriptEditor.setBounds(100, 100, 452, 607);
		frmDannysScriptEditor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmDannysScriptEditor.getContentPane().setLayout(null);
		
		setCommands();
		
		menuBar = new JMenuBar();
		menuBar.setBounds(0, 0, 446, 21);
		frmDannysScriptEditor.getContentPane().add(menuBar);
		
		LoadROM = new JMenuItem("Load ROM");
		LoadROM.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(tools.fileLoader()){
					ROM = tools.getFile();
					Compile.setEnabled(true);
				}
			}
		});
		menuBar.add(LoadROM);
		
		Compile = new JMenuItem("Compile");
		Compile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					compile();
				} catch (NumberFormatException | IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		Compile.setEnabled(false);
		menuBar.add(Compile);
		
		Tabs = new JTabbedPane(JTabbedPane.TOP);
		Tabs.setBounds(10, 63, 414, 497);
		frmDannysScriptEditor.getContentPane().add(Tabs);
		
		FirstTab = new JPanel();
		FirstTab.setToolTipText("Script 1");
		Tabs.addTab("Script 1", null, FirstTab, null);
		FirstTab.setLayout(null);
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 0, 409, 469);
		FirstTab.add(scrollPane);
		
		ScriptBox = new JTextArea();
		scrollPane.setViewportView(ScriptBox);
		
		XSERB = new JRadioButton("XSE Syntax");
		XSERB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ProgrammingRB.setSelected(!XSERB.isSelected());
			}
		});
		XSERB.setSelected(true);
		XSERB.setBounds(10, 28, 109, 23);
		frmDannysScriptEditor.getContentPane().add(XSERB);
		
		ProgrammingRB = new JRadioButton("Programming Syntax");
		ProgrammingRB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				XSERB.setSelected(!ProgrammingRB.isSelected());
			}
		});
		ProgrammingRB.setBounds(132, 28, 171, 23);
		frmDannysScriptEditor.getContentPane().add(ProgrammingRB);
	}
	
	
	public void setCommands() throws FileNotFoundException{ //Reads the command from commands.txt and saves them
		File commandsFile = new File(System.getProperty("user.dir") + "\\" + "commands.txt");
		Scanner scan = new Scanner(commandsFile);
		while(scan.hasNextLine()){
			String current = scan.nextLine();
			if(!current.startsWith("//") && !current.trim().equals("")){ //so, if it's valid
				String name;
				byte value;
				String[] split = current.split(" ");
				name = split[0].toLowerCase().trim();
				value = (byte)Integer.parseInt(split[1]);
				if(split.length > 2){
					String other = "";
					for(int i = 2; i < split.length; i++)
						other += split[i] + " "; //parameters
					Commands.add(new Command(name, value, other.split(" "))); //if it has parameters
				}
				else{
					Commands.add(new Command(name, value)); //if it doesn't have parameters
				}
			}
		}
		scan.close();
	}
	
	public void compile() throws NumberFormatException, IOException{
		preCompiled = new ArrayList<PseudoScript>(); //must clear everything from previous compiled times
		Compiled = new ArrayList<CompiledScript>();  //^
		String[] array = ScriptBox.getText().split("#org ");
		//array is the script text, before being compiled
		offset = getOffset(array[0]); //will make this more dynamic, hahaha
		ArrayList<String> list = new ArrayList<String>(Arrays.asList(array));
		list.remove(0); //what was before the first #org @ is not needed
		array = list.toArray(new String[0]);
		array = trim(array);
		for(String str : array){
			offset = tools.findFF(offset + getSize(str) + 1, getSize(str));
			preCompiled.add(new PseudoScript(offset, str));
		}
		for(PseudoScript PS : preCompiled){
			CompiledScript CS = new CompiledScript(PS.address);
			byte[] bytes = compile(PS.script);
			CS.data = bytes;
			Compiled.add(CS); //"CompiledScript" contains an address with it's corresponding byte value
		}
		for(CompiledScript cs : Compiled){
			tools.setBytes(cs.address, cs.data);
			System.out.println(cs);
		}
	}
	
	public byte[] compile(String str){
		ArrayList<Byte> Bytes = new ArrayList<Byte>();
		String[] script = str.split("\\r?\\n"); //by lines
		for(int i = 1; i < script.length; i++){ //1 because the first line contains "org @"
			String[] line = script[i].split(" ");
			line = lower(line); //sets everything to lowercase, makes comparisons easier
			if(line[0].equals("#raw")){
				
			}
			else{
				Command c = getCommand(line[0]);
				Bytes.add(c.value);
				if(c.parameters != null){
					String[] parameters = lower(c.parameters);
					for(int x = 0; x < parameters.length; x++){
						String current = parameters[x];
						if(current.equals("byte"))
							Bytes.add(toByte(line[x + 1]));
						else if(current.equals("word")){
							byte[] word = toWord(line[x + 1]);
							Bytes.add(word[0]);
							Bytes.add(word[1]);
						}
						else if(current.equals("pointer")){
							byte[] word = toPointer(line[x + 1]);
							Bytes.add(word[0]);
							Bytes.add(word[1]);
							Bytes.add(word[2]);
							Bytes.add(word[3]);
						}
						else if(current.equals("dword")){
							Bytes.add((byte) 0);
							Bytes.add((byte) 0);
							Bytes.add((byte) 0);
							Bytes.add((byte) 0);					
						}
					}
				}
			}
		}
		byte[] compiledBytes = new byte[Bytes.size()];
		for(int i = 0; i < compiledBytes.length; i++)
			compiledBytes[i] = Bytes.get(i);
		return compiledBytes;
	}
	
	public byte toByte(String str){
		str = str.toLowerCase();
		if(str.startsWith("0x"))
			return (byte)Byte.parseByte(str.substring(2), 16);
		return (byte)Byte.parseByte(str);
	}
	
	public byte[] toWord(String str){
		str = str.toLowerCase();
		if(str.startsWith("0x")){
			byte[] bytes = new byte[2];
			str = str.substring(2);
			int temp = Integer.parseInt(str, 16);
			bytes[0] = (byte) (temp & 0xFF);
			bytes[1] = (byte) (temp >> 8);
			return bytes;
		}
		return toWord("0x" + Integer.toHexString(Integer.parseInt(str)));
	}
	
	public byte[] toPointer(String str){
		if(str.startsWith("@")){
			for(PseudoScript PS : preCompiled)
				if(PS.script.split("\\r?\\n")[0].trim().equals(str.trim()))
					return toPointer(PS.address + ""); //if the @ is valid and in the script
			throw new IllegalArgumentException(); //otherwise... I'll have something else here
		}
		else{
			str = str.toLowerCase();;
			if(str.startsWith("0x"))
				str = str.substring(2);
			else
				return toPointer("0x" + Integer.toHexString(Integer.parseInt(str)));
			byte[] bytes = new byte[4];
			int temp = Integer.parseInt(str, 16);
			temp += 0x08000000;
			bytes[0] = (byte) (temp & 0xFF);
			bytes[1] = (byte) (temp >> 0x8);
			bytes[2] = (byte) (temp >> 0x10);
			bytes[3] = (byte) (temp >> 0x18);
			return bytes;
		}
	}
	
	//helper methods
	
	public long getOffset(String str) throws NumberFormatException, IOException{
		str = str.replace("#dynamic ", "").toLowerCase().trim();
		if(str.startsWith("0x")){
			return tools.findFF(Long.parseLong(str.substring(2), 16), 1); //will change 10 to something else
		}
		return Long.parseLong(str);
	}
	
	public int getSize(String str){
		int count = 1;
		String[] script = str.split("\\r?\\n");
		for(int i = 1; i < script.length; i++){
			Command c = getCommand(script[i].split(" ")[0]);
			count += c.getSize();
		}
		return count;
	}
	
	
	public Command getCommand(String name){
		for(Command c : Commands){
			if(c.name.equals(name))
				return c;
		}
		throw new IllegalArgumentException();
	}
	
	public String[] trim(String[] s){
		for(int i = 0; i < s.length; i++)
			s[i] = s[i].trim();
		return s;
	}
	
	public String[] lower(String[] a){
		for(int i = 0; i < a.length; i++)
			a[i] = a[i].toLowerCase();
		return a;
	}

}
