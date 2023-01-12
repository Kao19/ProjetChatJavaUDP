import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;

public class ChatGUI extends JFrame {
	
	// liste pour stocker la liste des amis du client courrant
	ArrayList<FriendsOfUser> friendsArray = new ArrayList<FriendsOfUser>();

	String host_name;
	JTextPane message_field;
	JTextPane room_field;
	JTextPane friends_field;

	String message = "";
	boolean message_is_ready = false;

	DatagramSocket dataSocketClient;

	public ChatGUI(){
		try {
		dataSocketClient = new DatagramSocket();	
		} catch (Exception e) {
			// TODO: handle exception
		}
	} 


	// interface qui sera initier après le login
	public void gui() {

		JDialog hostNameDialog = new JDialog(this, "Enter server address: ", true);
		JTextField hostField = new JTextField("                            ");
		JButton ok = new JButton("OK");
		hostNameDialog.setLayout(new FlowLayout());
		hostNameDialog.add(hostField);
		hostNameDialog.add(ok);
		hostNameDialog.setSize(800, 600);
		hostNameDialog.setLocationRelativeTo(null);
		hostNameDialog.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		hostNameDialog.setResizable(false);
		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				host_name = hostField.getText().trim();
				hostNameDialog.dispose();
			}
		});
		hostNameDialog.setVisible(true);
		
		setSize(800, 600);
		setTitle("UDP Chat room");
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		room_field = new JTextPane();
		message_field = new JTextPane();
		friends_field = new JTextPane();
		room_field.setEditable(false);
		friends_field.setEditable(false);
		ScrollPane x = new ScrollPane();
		x.add(room_field);
		ScrollPane z = new ScrollPane();
		z.add(message_field);
		z.setPreferredSize(new Dimension(100, 100));
		ScrollPane f = new ScrollPane();
		f.add(friends_field);
		f.setPreferredSize(new Dimension(200, 100));
		add(x, BorderLayout.CENTER);
		add(z, BorderLayout.SOUTH);
		add(f, BorderLayout.WEST);

		setVisible(true);
		message_field.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {

				if (e.getKeyCode() == 10) {
					message_field.setCaretPosition(0); 
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {

				if (e.getKeyCode() == 10 && !message_is_ready) {
					message = message_field.getText().trim();
					message_field.setText(null);
					if (!message.equals(null) && !message.equals("")) {
						message_is_ready = true;
					}
				}
			}
		});
	}

	// fonction pour afficher les messages
	public void displayMessage(String receivedMessage) {
		StyledDocument doc = room_field.getStyledDocument();
		try {
			doc.insertString(doc.getLength(), receivedMessage + "\n", null);
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
	}

	// fonction qui sera appelé plusieur fois dans ce code, sert à envoyer des packet au serveur et recevoir ces réponses
	public void envoiMsgADD(String host, int port, String msg) {
		byte[] tampon = msg.getBytes();
		byte[] copy = Arrays.copyOf(tampon, 1024);
		InetAddress address;
		
		 try {
			address = InetAddress.getByName(host);
			boolean on = true;
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			
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

			String[] status = new String(os.toString().trim()).split(",", 0);
			System.out.println(new String(os.toString()));
			
			// si un ami/ plusieurs ont été ajouter, il faut les ajouter dans la liste des amis
			if(status[0].startsWith("sucess")){
				for (int i = 2; i < status.length; i++) {
					if(status[i].equals("") || status[i].equals(null)){
						continue;
					}
					else{
						friendsArray.add(new FriendsOfUser(status[1],status[i]));	
					}
				}
				
			}
			// si un/plusieurs amis ont été supprimer, il faut les retirer de la liste des amis
			if(status[0].startsWith("deleted")){
				for (int i = 2; i < status.length; i++) {
					if(status[i].equals("") || status[i].equals(null)){
						continue;
					}
					else{
						friendsArray.clear();
						friendsArray.add(new FriendsOfUser(status[1],status[i]));	
					}
				}
				
			}
			// si on lance une discussion avec un client
			else if(status[0].startsWith("connected")){
				for (int i = 1,j=2; i < status.length-1; i+=2,j+=2) {
					System.out.println(status[i]+" ");
					
					Receive receiver = new Receive(dataSocketClient, this);
					Send sender = new Send(dataSocketClient, host, this, Integer.parseInt(status[j].trim())); // on envoie le message au client choisi
					Thread receiverThread = new Thread(receiver);
					Thread senderThread = new Thread(sender);
					receiverThread.start();
					senderThread.start();
				}
				
			}
				
		} catch (IOException e) {
			e.printStackTrace();
		} 	
		
	}

	
	// affichage des users qui ne sont pas des amis pour pouvoir les ajouter
	public void displayNonFriends(String[] nonFriends, String Current) {

		StyledDocument doc = friends_field.getStyledDocument();
		
        Box box = Box.createVerticalBox();
       
		JPanel panel = new JPanel();
		panel.add(new JLabel("Add Friend"));
	  	box.add(panel);

		JCheckBox[] checkBoxList = new JCheckBox[nonFriends.length];

		for(int i = 0; i < nonFriends.length; i++) {
    		checkBoxList[i] = new JCheckBox(nonFriends[i]);
    		box.add(checkBoxList[i]);
		}

		 try {
			friends_field.setCaretPosition(friends_field.getDocument().getLength());
            friends_field.insertComponent(box);
            doc.insertString(doc.getLength(), "\n", null );

			ActionListener actionListener = new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
				  AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				  boolean selected = abstractButton.getModel().isSelected();
				  System.out.println(selected);
				  System.out.println(abstractButton.getText());
				  if(selected){
					System.out.println(abstractButton.getText());
						addFriend(abstractButton.getText(),Current);
						box.remove(abstractButton);
				  }
				}
			};
			for(int i = 0; i < checkBoxList.length; i++) {
				checkBoxList[i].addActionListener(actionListener);
			}

			JButton displayFriends = new JButton("Friends List");
			
			box.add(displayFriends);
			
			ActionListener listFrAction = new ActionListener(){
				public void actionPerformed(ActionEvent e){
					JButton actionSource = (JButton) e.getSource();
			
					if(actionSource.equals(displayFriends)){
						chooseFriendToText(Current);
					}
				}
			};

			displayFriends.addActionListener(listFrAction);

		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
		
	}

	// envoie un packet de client vers le serveur pour ajouter l'ami en parametre
	public void addFriend(String ami, String curr) {
		this.envoiMsgADD("localhost", 10, curr+","+ami+",addFriend,");	
	}

	// envoyer la liste des users cochée
	public void sendCheckedList(String string) {	
		this.envoiMsgADD("localhost", 10, string);
	}

	// interface pour choisir les amis (soit pour les contacter ou pour les supprimer)
	public void chooseFriendToText(String current){

		JFrame f = new JFrame("Friends List");

        JPanel p = new JPanel();

		JButton b1 = new JButton("start");
		JButton b2 = new JButton("delete friend");

		try {
			dataSocketClient = new DatagramSocket();	
		} catch (Exception e) {
		}
	
		friendsArray.clear();

		// affichage des amis
		this.envoiMsgADD("localhost", 10, current+",0,display,");	

		System.out.println(friendsArray.size());
			
			JCheckBox[] checkBoxList = new JCheckBox[friendsArray.size()];

			for(int i = 0; i < friendsArray.size(); i++) {
				if(friendsArray.get(i).ami.equals("") || friendsArray.get(i).ami.equals(null)){
					continue;
				}
				else{
					checkBoxList[i] = new JCheckBox(friendsArray.get(i).ami);
				}
			}
	  
	
			for(int i = 0; i < friendsArray.size(); i++) {
				if(checkBoxList[i].getText().equals("")||checkBoxList[i].getText().equals(null)){
					continue;
				}
				else{
					p.add(checkBoxList[i]);
				}
			}

			p.add(b1);
			p.add(b2);

			// traitement pour envoyer les users à discuter avec
			b1.addActionListener((e) -> {
				String n = "isInSession,";
				for(JCheckBox box: checkBoxList){
					if(box.isSelected()){
						n = n + box.getText() + ",";
					}
				}
				sendCheckedList(n);
				
				f.setVisible(false);
			});

			// traitement pour envoyer les users à supprimer
			b2.addActionListener((e) -> {
				String n = "deleteFriend,"+current+",";
				for(JCheckBox box: checkBoxList){
					if(box.isSelected()){
						n = n + box.getText() + ",";
					}
				}
				sendCheckedList(n);
				
				f.setVisible(false);
			});
			

			f.add(p);

			
			f.setSize(800,600); 
			f.setLocationRelativeTo(null);
			f.setVisible(true);
			f.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
	}


	public boolean isMessageReady() {
		return message_is_ready;
	}

	public void setMessageReady(boolean messageReady) {
		this.message_is_ready = messageReady;
	}

	public String getMessage() {
		return message;
	}

	public String getHostName() {
		return host_name;
	}

	public ArrayList<FriendsOfUser> getFriendsArray(){
		return friendsArray;
	}
}