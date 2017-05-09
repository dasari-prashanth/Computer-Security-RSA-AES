import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import java.security.*;


public class ClientChatForm extends JFrame implements ActionListener {
	
	//Initialize the variables for Cipher
	private static String algorithm = "AES";
    private static int size = 128;
    
    private static Key key;
    private static Cipher cipher = null;
    private static byte[] encryptedBytes = null;
    private static byte[] decryptedBytes = null;
    private static byte[] Reckey = null;
    
	//Declare variables for Chatbox
	
    static Socket conn;
	JPanel panel;
	JTextField NewMsg;
	JTextArea ChatHistory;
	JButton Send;

	public ClientChatForm() throws UnknownHostException, IOException {
		//Initialize the chat box variable
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
		Send.addActionListener(this);
		//Connect to the Server socket
		conn = new Socket(InetAddress.getLocalHost(), 2000);
		
		DataInputStream dis = new DataInputStream(conn.getInputStream());
		//Read the length of the key 
		int length  = dis.readInt();
		if(length>0){
			
			//Read the bytes and length of the key from buffer 
			byte[] recd= new byte[length];
			dis.readFully(recd,0,length);
			
			//re-generating the sent by the server 
			key = new SecretKeySpec(recd, "AES");
			try {
				//Initialize the algorithm
				cipher = Cipher.getInstance(algorithm);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchPaddingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		
		ChatHistory.setText("Connected to Server\n");
		this.setTitle("Client");
		while (true) {
			try {
							    
			    //Reading length of the message
			    int length1  = dis.readInt();
				if(length1>0){
					byte[] Msg= new byte[length1];
					//Reading the message and decrypting
					dis.readFully(Msg,0,length1);
					cipher.init(Cipher.DECRYPT_MODE, key);
		            decryptedBytes = cipher.doFinal(Msg);
				}
				//Adding Date and time information
				DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
			    Date dateobj = new Date();
				//Displaying the decrypted message
				ChatHistory.setText(ChatHistory.getText() + "\n[" + df.format(dateobj) + "] Server:"
						+ new String(decryptedBytes));
				//Handling exceptions
			} catch (Exception e1) {
				ChatHistory.setText(ChatHistory.getText() + '\n'
						+ "Message sending fail:Network Error");
				e1.printStackTrace();
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
	//Code to encrpt and send data
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if ((e.getSource() == Send) && (NewMsg.getText() != "")) {
			
			DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
		    Date dateobj = new Date();
		    //Get lenght of message
		    String Len = NewMsg.getText();		    
		    try {
		    	//Encrypt using AES and key from server
				cipher.init(Cipher.ENCRYPT_MODE, key);
				encryptedBytes = cipher.doFinal(Len.getBytes());
			} catch (Exception e3) {
				// TODO Auto-generated catch block
				e3.printStackTrace();
			}
		    //Appending text to chatbox
			ChatHistory.setText(ChatHistory.getText() + "\n[" + df.format(dateobj) + "] Me:"
					+ NewMsg.getText());
			try {
				DataOutputStream dos = new DataOutputStream(
						conn.getOutputStream());
				//Write the length of the message and the encrypted message to the buffer
				dos.writeInt(encryptedBytes.length);
				dos.write(encryptedBytes);
				//Write to console the encrypted message
				System.out.println("Encrypted Msg: " + new String(encryptedBytes));
				//Handle exceptions
			} catch (Exception e1) {
				ChatHistory.setText(ChatHistory.getText() + '\n'
						+ "Message sending fail:Network Error");
				e1.printStackTrace();
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
		//Call to the class
		ClientChatForm chatForm = new ClientChatForm();
	}
}
