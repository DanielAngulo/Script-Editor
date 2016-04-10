import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JSpinner;
import javax.swing.JTextPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;


public class Test {

	private JFrame frmTextConverter;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Test window = new Test();
					window.frmTextConverter.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Test() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	
	JTextArea textArea;
	private JScrollPane scrollPane;
	private JButton btnNewButton;
	
	private void initialize() {
		frmTextConverter = new JFrame();
		frmTextConverter.setTitle("Text Converter");
		frmTextConverter.setBounds(100, 100, 305, 211);
		frmTextConverter.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmTextConverter.getContentPane().setLayout(null);
		Border border = BorderFactory.createLineBorder(Color.BLACK);
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 11, 265 , 116);
		frmTextConverter.getContentPane().add(scrollPane);
		
		textArea = new JTextArea();
		scrollPane.setViewportView(textArea);
		textArea.setLineWrap(true);
		textArea.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		
		btnNewButton = new JButton("Insert");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for(String line : textArea.getText().split("\n"))
					System.out.println(line);
			}
		});
		btnNewButton.setBounds(10, 138, 265, 23);
		frmTextConverter.getContentPane().add(btnNewButton);
	}
}
