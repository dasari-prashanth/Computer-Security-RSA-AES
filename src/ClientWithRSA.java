import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.*;
import java.io.File;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.sql.Time;
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
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import java.security.*;

public class ClientWithRSA extends JFrame implements ActionListener {
	//Declare variables for Cipher
	
	private static KeyPair key;
	PrivateKey privateKey;
	KeyPairGenerator keyGen;
	private static String algorithm = "RSA";
    private static int size = 2048;
    private static Cipher cipher = null;
    private static byte[] encryptedBytes = null;
    private static byte[] decryptedBytes = null;
   
    //Path to the private key files
    public static final String PRIVATE_KEY_FILE_FOR_SERVER = "D:/WorkSpace/Computer Security Project/src/privateForServer.key";
    public static final String PRIVATE_KEY_FILE_FROM_SERVER = "D:/WorkSpace/Computer Security Project/src/privateForClient.key";
    //Declare the variable for Chatbox
	static Socket conn;
	JPanel panel;
	JTextField NewMsg;
	JTextArea ChatHistory;
	JButton Send;

	public ClientWithRSA() throws UnknownHostException, IOException {
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
		
		
		ObjectInputStream inputStream = null;
		//Generate key pair for RSA
		try {
			keyGen = KeyPairGenerator.getInstance(algorithm);
			keyGen.initialize(size);
		    key = keyGen.generateKeyPair();
		    
		    //Initialize the cipher to RSA
		    cipher = Cipher.getInstance("RSA");
		} catch (NoSuchAlgorithmException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Creating a new file to store private key for the Server
		File privateKeyFile = new File(PRIVATE_KEY_FILE_FOR_SERVER);
		if (privateKeyFile.getParentFile() != null) {
	        privateKeyFile.getParentFile().mkdirs();
	      }
	      privateKeyFile.createNewFile();
	     
	      // Saving the Private key in a file
	      ObjectOutputStream privateKeyOS = new ObjectOutputStream(
	      new FileOutputStream(privateKeyFile));
	      privateKeyOS.writeObject(key.getPrivate());
	      privateKeyOS.close();
	      
	      //Connecting to the server
	      conn = new Socket(InetAddress.getLocalHost(), 2000);
	      ChatHistory.setText("Connected to Server");
	      this.setTitle("Client");
		
	      //Reading the private key file fromm the server
		inputStream = new ObjectInputStream(new FileInputStream(PRIVATE_KEY_FILE_FROM_SERVER));
	      try {
			privateKey = (PrivateKey) inputStream.readObject();
		} catch (ClassNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	      
		while (true) {
			try {
				
				DataInputStream dis = new DataInputStream(conn.getInputStream());
				
			    //Reading the length of message
			    int length1  = dis.readInt();
				if(length1>0){
					//Decrypting the message using RSA and Server Private key
					byte[] Msg = new byte[length1];
					dis.readFully(Msg,0,length1);
					//System.out.println(Msg);(Un-comment to see the encrypted message from server)
					cipher.init(Cipher.DECRYPT_MODE, privateKey);
		            decryptedBytes = cipher.doFinal(Msg);
				}
				//Appending date and time
				DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
			    Date dateobj = new Date();
				//Display the decrypted message
				ChatHistory.setText(ChatHistory.getText() + "\n[" + df.format(dateobj) + "] Server:"
						+ new String(decryptedBytes));
				//Handle exceptions
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
	//Code to encrypt message and send
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if ((e.getSource() == Send) && (NewMsg.getText() != "")) {
			//Append date and time information
			DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
		    Date dateobj = new Date();
		    //Get length of message
		    String Len = NewMsg.getText();		    
		    try {
		    	//Encrypt using RSA and Client Public key
				cipher.init(Cipher.ENCRYPT_MODE, key.getPublic());
				encryptedBytes = cipher.doFinal(Len.getBytes());
			} catch (Exception e3) {
				// TODO Auto-generated catch block
				e3.printStackTrace();
			}
		    //Display the message for the client
			ChatHistory.setText(ChatHistory.getText() + "\n[" + df.format(dateobj) +"] " + "Me:"
					+ NewMsg.getText());
			try {
				DataOutputStream dos = new DataOutputStream(
						conn.getOutputStream());
				//Write the lenght of message and the encrypted bytes to the buffer
				dos.writeInt(encryptedBytes.length);
				dos.write(encryptedBytes);
				//Print to Console the encrypted message
				System.out.println("Encrypted Msg: " + new String(encryptedBytes));
				//Handle exceptions
			} catch (Exception e1) {
				ChatHistory.setText(ChatHistory.getText() + '\n'
						+ "Message sending fail:Network Error");
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
		ClientWithRSA chatForm = new ClientWithRSA();
	}
}