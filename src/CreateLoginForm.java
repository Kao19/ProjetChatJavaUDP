import javax.swing.*;
import java.net.*;
import java.util.*;
import java.io.*;
import java.awt.event.*;

class CreateLoginForm extends JFrame implements ActionListener  
{  
    JButton b1;  
    JPanel newPanel;  
    JLabel userLabel, passLabel;  
    JTextField  textField1, textField2;  
    
    DatagramSocket dataSocketClient;

	DatagramPacket packetOut;
	String option;
	
	ChatGUI window = new ChatGUI();

	// constructeur contenant l'option (soit login soit register)
    CreateLoginForm(String option)  
    {     

		this.option = option;
             
		if(option.equals("register")){
			userLabel = new JLabel();  
			userLabel.setText("Username");      
			
			textField1 = new JTextField(15); 
			
			passLabel = new JLabel();  
			passLabel.setText("Password");      
			
			textField2 = new JPasswordField(15);    
			
			b1 = new JButton("REGISTER");
			
			newPanel = new JPanel();  
			newPanel.add(userLabel);      
			newPanel.add(textField1);     
			newPanel.add(passLabel);      
			newPanel.add(textField2);     
			newPanel.add(b1);             
			
			
			add(newPanel);  
			
			
			this.setSize(800,600); 
			setLocationRelativeTo(null);
			
			b1.addActionListener(this);       
			setTitle("REGISTER FORM");         
  
			this.setVisible(true);
		}else if(option.equals("login")){
			userLabel = new JLabel();  
			userLabel.setText("Username");      
			
			textField1 = new JTextField(15);   
			
			passLabel = new JLabel();  
			passLabel.setText("Password");      
			
			textField2 = new JPasswordField(15);    
			
			b1 = new JButton("LOGIN");
			

			newPanel = new JPanel();  
			newPanel.add(userLabel);      
			newPanel.add(textField1);     
			newPanel.add(passLabel);      
			newPanel.add(textField2);     
			newPanel.add(b1);             
			
			
			add(newPanel);  
			
			
			this.setSize(800,600);
			setLocationRelativeTo(null);
			
			b1.addActionListener(this);       
			setTitle("LOGIN FORM");         
   
			this.setVisible(true);
		}
        
    }  
      


	// cette méthod sera appelée dans plusieur stads dans ce code, elle sert à contacter le serveur et récuperer sa réponse 
    public void demandeLoginRegister(String host, int port, String msg) {
		byte[] tampon = msg.getBytes();
		byte[] copy = Arrays.copyOf(tampon, 1024);
		InetAddress address;
		
		try {
			address = InetAddress.getByName(host);
			boolean on = true;
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			
				dataSocketClient = new DatagramSocket();
				int nbr=1;
				while(on) {	
					DatagramPacket packett = new DatagramPacket(copy, copy.length, address, port);
					dataSocketClient.send(packett);
					dataSocketClient.receive(packett);
					System.out.println("Mini-packet num : "+nbr+++"  de taille "+packett.getLength());
					if(packett.getLength()>0) {
						os.write(packett.getData(), 0, packett.getData().length);
					}else {
						on = false;
					}
				}
								
			String[] status = new String(os.toString()).split(",", 0);
			System.out.println(new String(os.toString()));

			// dans ce cas, le login est correcte et le serveur renvoie la liste des clients non amis du client connecté	
			if(status[0].startsWith("sucess")){
				if(!status[1].equals("0")){
					displayGui(Arrays.copyOfRange(status, 2, 2 + Integer.parseInt(status[1])));
				}else{
					displayGui(Arrays.copyOfRange(status, 1, 1));
				}
			}
			// dans ce cas, le login est incorrect. Un alert sera ajouté
			else if(status[0].startsWith("failed")){
				displayGuiFail();
			}
			// danc ce cas, si le client s'inscrie, une interface de login sera ouverte directement après le register
			else if(status[0].startsWith("registered")){
				CreateLoginForm c = new CreateLoginForm("login");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
	}

	// selon le boutton choisi (soit login soit register) un different packet sera envoyé au server (en fait appel à la methode dessus)
    public void actionPerformed(ActionEvent ae){
        if(this.option.equals("login")){
			this.demandeLoginRegister("localhost", 900, textField1.getText()+","+textField2.getText()+",login,");
        	this.setVisible(false);
		}else if(this.option.equals("register")){
			this.demandeLoginRegister("localhost", 900, textField1.getText()+","+textField2.getText()+",register,");
        	this.setVisible(false);
		}
            
    }

	// interface du chat
	public void displayGui(String[] users){
		window.gui();
		String host = window.getHostName();
		window.setTitle("Chat Launched: " + host);
		window.displayNonFriends(users,textField1.getText());
		try { 
			dataSocketClient = new DatagramSocket();
			Receive receiver = new Receive(dataSocketClient, window);
			Thread receiverThread = new Thread(receiver);
			receiverThread.start();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	// alert en cas d'erreur de login
	public void displayGuiFail(){
		int input = JOptionPane.showOptionDialog(null, "login or password wrong", "fail login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);

		if(input == JOptionPane.OK_OPTION)
		{
    		CreateLoginForm c = new CreateLoginForm("login");
		}
	}

}  
