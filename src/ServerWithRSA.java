import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.*;
import java.io.File;
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
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.*;
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

public class ServerWithRSA extends JFrame implements ActionListener {
	//Declare variables for Cipher
	private static KeyPair key;
	PrivateKey privateKey;
	KeyPairGenerator keyGen;
	private static String algorithm = "RSA";
    private static int size = 2048;
    private static Cipher cipher = null;
    private static byte[] encryptedBytes = null;
    private static byte[] decryptedBytes = null;
    
    //Path to Private key files.(Change to local directory while executing)
    public static final String PRIVATE_KEY_FILE_FOR_CLIENT = "D:/WorkSpace/Computer Security Project/src/privateForClient.key";
    public static final String PRIVATE_KEY_FILE_FROM_CLIENT = "D:/WorkSpace/Computer Security Project/src/privateForServer.key";
	//Declare the chat box variables.
	static ServerSocket server;
	static Socket conn;
	JPanel panel;
	JTextField NewMsg;
	JTextArea ChatHistory;
	JButton Send;
	DataInputStream dis;
	DataOutputStream dos;

	public ServerWithRSA() throws UnknownHostException, IOException {
		//Initialize the variables
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
		//Look for inoming connections
		server = new ServerSocket(2000, 1, InetAddress.getLocalHost());
		ChatHistory.setText("Waiting for Client");
		
		ObjectInputStream inputStream = null;
		//Generate Key pair for encryption/decryption
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
		//Write the private key file into a new file for the client.		
		 File privateKeyFile = new File(PRIVATE_KEY_FILE_FOR_CLIENT);
		 if (privateKeyFile.getParentFile() != null) {
		        privateKeyFile.getParentFile().mkdirs();
		      }
		 privateKeyFile.createNewFile();
		 ObjectOutputStream privateKeyOS = new ObjectOutputStream(
		new FileOutputStream(privateKeyFile));
		privateKeyOS.writeObject(key.getPrivate());
		privateKeyOS.close();
		//Connect to client
		conn = server.accept();
		
		//Read the private key file from the client.
		inputStream = new ObjectInputStream(new FileInputStream(PRIVATE_KEY_FILE_FROM_CLIENT));
	      try {
			privateKey = (PrivateKey) inputStream.readObject();
		} catch (ClassNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		ChatHistory.setText(ChatHistory.getText() + '\n' + "Client Found");
		while (true) {
			try {
				
			    DataInputStream dis = new DataInputStream(conn.getInputStream());
			    
			    //Read the length of incoming message
			    int length1  = dis.readInt();
				if(length1>0){
					//Load the messsage into a byte array and decrypt
					byte[] Msg= new byte[length1];
					dis.readFully(Msg,0,length1);
					cipher.init(Cipher.DECRYPT_MODE, privateKey);
		            decryptedBytes = cipher.doFinal(Msg);
				}
				//Add Date and time
				DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
			    Date dateobj = new Date();
				//Display the message to the user
				ChatHistory.setText(ChatHistory.getText() + "\n[" + df.format(dateobj) + "] Client:"
						+ new String(decryptedBytes));
				//Handle any exceptions
			} catch (Exception e1) {
				ChatHistory.setText(ChatHistory.getText() + 'n'
						+ "Message sending fail:Network Error");
				e1.printStackTrace();
				try {
					Thread.sleep(10000);
					System.exit(0);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
//Code to encrypt and send
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
		    	//Enrypt the message
				cipher.init(Cipher.ENCRYPT_MODE, key.getPublic());
				encryptedBytes = cipher.doFinal(Len.getBytes());
			} catch (Exception e3) {
				// TODO Auto-generated catch block
				e3.printStackTrace();
			}
		    //Append to server chat box
			ChatHistory.setText(ChatHistory.getText() + "\n[" + df.format(dateobj) + "] ME:"
					+ NewMsg.getText());
			try {
				DataOutputStream dos = new DataOutputStream(
						conn.getOutputStream());
				//Write the length of message and encrypted bytes to buffer
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
		new ServerWithRSA();
	}
}