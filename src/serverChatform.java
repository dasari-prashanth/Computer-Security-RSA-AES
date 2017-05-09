import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.security.*;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.*;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import java.security.*;

public class serverChatform extends JFrame implements ActionListener {
	
	//initialize the variables for cipher algorithm
	private static String algorithm = "AES";
    private static int size = 128;
    
    private static Key key;
    private static Cipher cipher = null;
    private static byte[] encryptedBytes = null;
    private static byte[] decryptedBytes = null;
   
	//Declare variable for Client/Server GUI
	static ServerSocket server;
	static Socket conn;
	JPanel panel;
	JTextField NewMsg;
	JTextArea ChatHistory;
	JButton Send;
	DataInputStream dis;
	DataOutputStream dos;

	public serverChatform() throws UnknownHostException, IOException {
		
		//Initialize the variables.
		panel = new JPanel();
		NewMsg = new JTextField();
		ChatHistory = new JTextArea();
		Send = new JButton("Send");
		this.setSize(500, 500);
		this.setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		panel.setLayout(null);
		this.add(panel);
		ChatHistory.setBounds(20, 20, 450, 360);
		panel.add(ChatHistory);
		NewMsg.setBounds(20, 400, 340, 30);
		panel.add(NewMsg);
		Send.setBounds(375, 400, 95, 30);
		panel.add(Send);
		this.setTitle("Server");
		Send.addActionListener(this);
		server = new ServerSocket(2000, 1, InetAddress.getLocalHost());
		ChatHistory.setText("Waiting for Client\n");
		//Get connection to client
		conn = server.accept();	
		
		//Generate the Keys
		try{
			
		KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm);  
        keyGenerator.init(size);
        key = keyGenerator.generateKey();
        
        //Initialze the cipher
        cipher = Cipher.getInstance(algorithm);
		}
		catch (Exception e){}
		
		//Encode the Key and transmit over the connection.
        byte[] encodedKey = key.getEncoded();
		DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
		dos.writeInt(encodedKey.length);
		dos.write(encodedKey);
		
		//Declare the Data Input stream to receive data
		DataInputStream dis = new DataInputStream(conn.getInputStream());
		ChatHistory.setText(ChatHistory.getText() + '\n' + "Client Found");
		while (true) {
			try {
				
			    
			    //Receive the length of the encrypted message
			    int length1  = dis.readInt();
				if(length1>0){
					//Load the input into a byte stream and decrypt 
					byte[] Msg= new byte[length1];
					dis.readFully(Msg,0,length1);
					cipher.init(Cipher.DECRYPT_MODE, key);
		            decryptedBytes = cipher.doFinal(Msg);
				}
				//Append data and time information 
				DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
			    Date dateobj = new Date();
				//Append the decypted message to the chat box
				ChatHistory.setText(ChatHistory.getText() + "\n[" + df.format(dateobj) + "] Client:"
						+ new String(decryptedBytes));
				//Handle exceptions
			} catch (Exception e1) {
				ChatHistory.setText(ChatHistory.getText() + '\n'
						+ "Message sending fail:Network Error");
				try {
					Thread.sleep(3000);
					System.exit(0);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	//Code to encrypt and ssend data
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if ((e.getSource() == Send) && (NewMsg.getText() != "")) {
			//Displaying date and time for each message
			DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
		    Date dateobj = new Date();
		    //get the message length 
		    String Len = NewMsg.getText();		    
		    try {
		    	//Encrypt the message using AES 
				cipher.init(Cipher.ENCRYPT_MODE, key);
				encryptedBytes = cipher.doFinal(Len.getBytes());
			} catch (Exception e3) {
				// TODO Auto-generated catch block
				e3.printStackTrace();
			}
			ChatHistory.setText(ChatHistory.getText() + "\n[" + df.format(dateobj) + "] ME:"
					+ NewMsg.getText());
			try {
				DataOutputStream dos = new DataOutputStream(
						conn.getOutputStream());
				//Send the length of the message and the encypted bytes into the buffer
				dos.writeInt(encryptedBytes.length);
				dos.write(encryptedBytes);
				//Print to Console the encrypted message
				System.out.println("Encrypted Msg: " + new String(encryptedBytes));
				//Handle exceptions
			} catch (Exception e1) {
				try {
					Thread.sleep(3000);
					System.exit(0);
				} catch (InterruptedException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
			}
			NewMsg.setText("");
		}
	}

	public static void main(String[] args) throws UnknownHostException,
	IOException {
		new serverChatform();
	}
}


