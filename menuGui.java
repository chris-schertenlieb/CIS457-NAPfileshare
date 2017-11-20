import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JSeparator;
import javax.swing.JList;
import javax.swing.border.LineBorder;
import java.awt.Color;
import javax.swing.JTextArea;
import javax.swing.JTable;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;


public class menuGui {

	private JFrame frame;
	private JTextField serverHostName;
	private JTextField userName;
	private JTextField portNumber;
	private JTextField hostName;
	private JComboBox speed;
	
	private JTextField searchKeyWord;
	//private JList list;
	private JTable table;
	
	
	private JTextField command;
	private JTextArea textArea;
	
	private String message;
	private ArrayList<String> results;
	
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					menuGui window = new menuGui();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public menuGui() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		Host host = new Host();
		
		frame = new JFrame();
		frame.setBounds(200, 200, 550, 500);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JButton btnConnectButton = new JButton("Connect");
		btnConnectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//code for connect
				try{
					
					String PORT =  portNumber.getText();
					String ServerHOST = serverHostName.getText();
					message = "CONNECT " + ServerHOST + " " + PORT;

					host.setCommand(message);
					results = host.getResponse();
					printResults();

				}
				catch(Exception e){
					
				}
			}
		});
		btnConnectButton.setBounds(387, 11, 137, 23);
		frame.getContentPane().add(btnConnectButton);
		
		serverHostName = new JTextField();
		serverHostName.setBounds(120, 12, 159, 20);
		frame.getContentPane().add(serverHostName);
		serverHostName.setColumns(10);
		
		JLabel lblServerHostName = new JLabel("Server Host Name:");
		lblServerHostName.setBounds(10, 15, 133, 14);
		frame.getContentPane().add(lblServerHostName);
		
		JLabel lblNewLabel = new JLabel("Username:");
		lblNewLabel.setBounds(10, 43, 75, 14);
		frame.getContentPane().add(lblNewLabel);
		
		userName = new JTextField();
		userName.setBounds(85, 40, 95, 20);
		frame.getContentPane().add(userName);
		userName.setColumns(10);
		
		JLabel lblPort = new JLabel("Port:");
		lblPort.setBounds(289, 15, 34, 14);
		frame.getContentPane().add(lblPort);
		
		portNumber = new JTextField();
		portNumber.setBounds(325, 12, 52, 20);
		frame.getContentPane().add(portNumber);
		portNumber.setColumns(10);
		
		JLabel lblHostname = new JLabel("Hostname:");
		lblHostname.setBounds(190, 43, 69, 14);
		frame.getContentPane().add(lblHostname);
		
		hostName = new JTextField();
		hostName.setBounds(267, 40, 110, 20);
		frame.getContentPane().add(hostName);
		hostName.setColumns(10);
		
		JLabel lblSpeed = new JLabel("Speed:");
		lblSpeed.setBounds(382, 43, 52, 14);
		frame.getContentPane().add(lblSpeed);
		
		speed = new JComboBox();
		speed.setBounds(429, 40, 95, 20);
		speed.addItem("Ethernet");
		speed.addItem("WiFi");
		speed.addItem("T1");
		frame.getContentPane().add(speed);
		
		JButton btnRegisterButton = new JButton("Register");
		btnRegisterButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				//code for register
				try{
					
					String HOST = hostName.getText();
					String NAME = userName.getText();
					String SPEED = (String) speed.getSelectedItem();
					message = "REGISTER " + NAME + " " + HOST + " " + SPEED;
						
					host.setCommand(message);
					results = host.getResponse();
					printResults();
					//try connection
				}
				catch(Exception e){
							
				}
			}
		});
		btnRegisterButton.setBounds(10, 68, 149, 23);
		frame.getContentPane().add(btnRegisterButton);
		
		
		JLabel lblKeyword = new JLabel("Keyword:");
		lblKeyword.setBounds(10, 119, 60, 14);
		frame.getContentPane().add(lblKeyword);
		
		searchKeyWord = new JTextField();
		searchKeyWord.setBounds(71, 116, 294, 20);
		frame.getContentPane().add(searchKeyWord);
		searchKeyWord.setColumns(10);
		
		JButton btnSearch = new JButton("Search");
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try{
					message = "SEARCH " + searchKeyWord.getText();
					host.setCommand(message);
					results = host.getResponse();
					printResults();
				}
				catch(Exception e){
					JOptionPane.showMessageDialog(null, "Enter a Keyword!");	
				}
				//run search with key
				
			}
		});
		btnSearch.setBounds(375, 115, 149, 23);
		frame.getContentPane().add(btnSearch);
		
		table = new JTable();
		table.setBorder(new LineBorder(new Color(0, 0, 0), 3));
		table.setBounds(10, 285, 514, -168);
//		Column aColumn = new Column;
//		table.addColumn(aColumn);
		frame.getContentPane().add(table);
		
		//list = new JList();
		//list.setBorder(new LineBorder(Color.BLUE, 2, true));
		//list.setBounds(10, 285, 514, -160);
		//frame.getContentPane().add(list);
		
		JLabel lblEnterCommand = new JLabel("Enter Command:");
		lblEnterCommand.setBounds(10, 322, 95, 14);
		frame.getContentPane().add(lblEnterCommand);
		
		command = new JTextField();
//		@Override
//		public void keyPressed(KeyEvent e) {
//		    if (e.getKeyCode()==KeyEvent.VK_ENTER){
//		        
//		    }
//
//		}
		command.setBounds(115, 319, 310, 20);
		frame.getContentPane().add(command);
		command.setColumns(10);
		
		
		JButton btnGo = new JButton("Go");
		btnGo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try{
					String COMMAND = command.getText();
					if(COMMAND.length() > 0){
						textArea.append(">>>" + COMMAND + "\n");
						message = COMMAND;
						host.setCommand(message);
						results = host.getResponse();
						printResults();
						command.setText("");
					}
					else{
						command.setText("");
						JOptionPane.showMessageDialog(null, "Enter a Command!");	
					}
				}
				catch(Exception e1){
					
				}
				//run command
			}
		});
		btnGo.setBounds(441, 318, 83, 23);
		frame.getContentPane().add(btnGo);
		
		textArea = new JTextArea();
		textArea.setBounds(10, 347, 514, 103);
		frame.getContentPane().add(textArea);
		
		
		frame.getRootPane().setDefaultButton(btnGo);
		
		JButton btnUnRegister = new JButton("Un-register");
		btnUnRegister.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try{
					
					String NAME = userName.getText();
					message = "UNREGISTER " + NAME;
					host.setCommand(message);
					results = host.getResponse();
					printResults();
				}
				catch(Exception e2){};
			}
		});
		btnUnRegister.setBounds(387, 68, 137, 23);
		frame.getContentPane().add(btnUnRegister);
		
		JButton btnQuitButton = new JButton("Quit");
		btnQuitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				message = "QUIT";
				host.setCommand(message);
				results = host.getResponse();
			}
		});
		btnQuitButton.setBounds(169, 68, 208, 23);
		frame.getContentPane().add(btnQuitButton);
		
	}
	
	printResults(){
		if(results != null){
			textArea.append(">>>" + message);
			for(int i = 0; i < results.size(); i++){
				textArea.append(results[i]);
			}
		}
	}
}