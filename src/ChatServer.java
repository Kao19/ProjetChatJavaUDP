import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;

public class ChatServer implements  Runnable {
	public final static int PORT = 1;
	private final static int BUFFER = 1024;

	private DatagramSocket loginSocket;
	private DatagramSocket socket1;
	private DatagramSocket chatSocket;
	private ArrayList<Session> connectedClients;

	DatagramPacket packetIn;
	DatagramPacket pack;

	public ChatServer() throws IOException {
		loginSocket = new DatagramSocket(900);
		chatSocket = new DatagramSocket(10);
		socket1 = new DatagramSocket(PORT);
		System.out.println("Server is running and listening");
		connectedClients = new ArrayList();
	}

	// cette méthode est en charge à recevoir tous qui est en relation avec la gestion des utilisateurs avec autres utilisateurs
	public void receptForChatFunctionalities(){
		byte[] tampon =  new byte[BUFFER];
		try {
			pack = new DatagramPacket(tampon, tampon.length);
			chatSocket.receive(pack);
			System.out.println("un packet vient d etre recu de : " + pack.getSocketAddress());
			System.out.println("Le message recu est: "+new String(pack.getData()));
			
			
			String[] dataUser = new String(pack.getData()).split(",", 0);
			
			
			try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection connection = DriverManager.getConnection(
					"jdbc:mysql://localhost:3306/clts", "root", ""
			);
			

			// ajout d'ami
			if(dataUser[2].equals("addFriend")){
				String query = " insert into amis (user, ami)"+ " values (?, ?)";
		
					PreparedStatement preparedStmt = connection.prepareStatement(query);
					preparedStmt.setString (1,dataUser[0]);
					
					preparedStmt.setString (2,dataUser[1]);
		
					preparedStmt.execute();

					String msg = "sucess";
						byte[] buffer = msg.getBytes();
						System.out.println(msg.getBytes());
						byte[] buff = new byte[1024];
						int c=0;int nbr=1;
						for(int i=0;i<buffer.length;i++){
							buff[c] = buffer[i];
							c++;
							if(c==1024){
								DatagramPacket packet = new DatagramPacket(buff, buff.length, pack.getAddress(), pack.getPort());
								buff = new byte[1024];
								chatSocket.send(packet);
								System.out.println("Mini-packet Envoy� : "+nbr+++"  "+c);
								c=0;
							} 
						}   
						if(c>0) {
							DatagramPacket packet = new DatagramPacket(buff, c, pack.getAddress(), pack.getPort());
							chatSocket.send(packet);
							System.out.println("D�rnier mini-packet Envoy� : "+c);
						}
						DatagramPacket packet = new DatagramPacket(new byte[0], 0, pack.getAddress(), pack.getPort());
						chatSocket.send(packet);
						System.out.println("Tout est Envoy�");

		

			}
			
			// renvoie des utilisateurs non amis
			if(dataUser[2].equals("display")){

				String querySelect = "SELECT login FROM clients WHERE clients.login != ? and ( clients.login in (select user from amis where ami = ?) or clients.login in (select ami from amis where user = ?) )";
				
				String m = "sucess,"+dataUser[0];

				PreparedStatement preparedStmtSELECT = connection.prepareStatement(querySelect);
				preparedStmtSELECT.setString(1,dataUser[0]);
				preparedStmtSELECT.setString(2,dataUser[0]);
				preparedStmtSELECT.setString(3,dataUser[0]);
	
				
				ResultSet rsSelect = preparedStmtSELECT.executeQuery();
	
				while ( rsSelect.next() ) {
					m = m + "," + rsSelect.getString("login");
				}

				m = m + ",";
				
						byte[] buffer = m.getBytes();
						System.out.println(m.getBytes());
						byte[] buff = new byte[1024];
						int c=0;int nbr=1;
						for(int i=0;i<buffer.length;i++){
							buff[c] = buffer[i];
							c++;
							if(c==1024){
								DatagramPacket packet = new DatagramPacket(buff, buff.length, pack.getAddress(), pack.getPort());
								buff = new byte[1024];
								chatSocket.send(packet);
								System.out.println("Mini-packet Envoy� : "+nbr+++"  "+c);
								c=0;
							} 
						}   
						if(c>0) {
							DatagramPacket packet = new DatagramPacket(buff, c, pack.getAddress(), pack.getPort());
							chatSocket.send(packet);
							System.out.println("D�rnier mini-packet Envoy� : "+c);
						}
						DatagramPacket packet = new DatagramPacket(new byte[0], 0, pack.getAddress(), pack.getPort());
						chatSocket.send(packet);
						System.out.println("Tout est Envoy�");

		

			}

			// verifier la possibilité de chatter avec un user (l'utilisateur courrant choisi un/plusieurs utilisateurs) est le serveur récupère leurs ports
			if(dataUser[0].equals("isInSession")){
				String res = "connected";
				System.out.println("connected are: "+connectedClients.size());
				System.out.println(dataUser.length);
				
				for (int j = 0; j < connectedClients.size(); j++) {
					for (int k = 1; k < dataUser.length; k++) {
						if(connectedClients.get(j).login.equals(dataUser[k].trim())){
							res = res + "," + connectedClients.get(j).login + "," + connectedClients.get(j).port;
						}	
					}
				}
				res = res + ",";
				byte[] buffer = res.getBytes();
				System.out.println(res);
				byte[] buff = new byte[1024];
				int c=0;int nbr=1;
				for(int i=0;i<buffer.length;i++){
					buff[c] = buffer[i];
					c++;
					if(c==1024){
						DatagramPacket packet = new DatagramPacket(buff, buff.length, pack.getAddress(), pack.getPort());
						buff = new byte[1024];
						chatSocket.send(packet);
						System.out.println("Mini-packet Envoy� : "+nbr+++"  "+c);
						c=0;
					} 
				}   
				if(c>0) {
					DatagramPacket packet = new DatagramPacket(buff, c, pack.getAddress(), pack.getPort());
					chatSocket.send(packet);
					System.out.println("D�rnier mini-packet Envoy� : "+c);
				}
				DatagramPacket packet = new DatagramPacket(new byte[0], 0, pack.getAddress(), pack.getPort());
				chatSocket.send(packet);
				System.out.println("Tout est Envoy�");
	
			}
			
			// suppression d'un/des amis
			if(dataUser[0].equals("deleteFriend")){
				String res = "deleted,"+dataUser[1];
				
				

				for (int k = 2; k < dataUser.length; k++) {
					
					String query = " delete from amis where (user = ? and ami = ?) or (user = ? and ami = ?)";

					PreparedStatement preparedStmt = connection.prepareStatement(query);
					
					preparedStmt.setString(1,dataUser[1].trim());
					preparedStmt.setString(2,dataUser[k].trim());
					preparedStmt.setString(3,dataUser[k].trim());
					preparedStmt.setString(4,dataUser[1].trim());

					preparedStmt.execute();
				}

				
				String querySelect = "SELECT login FROM clients WHERE clients.login != ? and ( clients.login in (select user from amis where ami = ?) or clients.login in (select ami from amis where user = ?) )";
				
				PreparedStatement preparedStmtSELECT = connection.prepareStatement(querySelect);
				preparedStmtSELECT.setString(1,dataUser[1]);
				preparedStmtSELECT.setString(2,dataUser[1]);
				preparedStmtSELECT.setString(3,dataUser[1]);
	
				
				ResultSet rsSelect = preparedStmtSELECT.executeQuery();
	
				while ( rsSelect.next() ) {
					res = res + "," + rsSelect.getString("login");
				}

				

				res = res + ",";
				byte[] buffer = res.getBytes();
				System.out.println(res);
				byte[] buff = new byte[1024];
				int c=0;int nbr=1;
				for(int i=0;i<buffer.length;i++){
					buff[c] = buffer[i];
					c++;
					if(c==1024){
						DatagramPacket packet = new DatagramPacket(buff, buff.length, pack.getAddress(), pack.getPort());
						buff = new byte[1024];
						chatSocket.send(packet);
						System.out.println("Mini-packet Envoy� : "+nbr+++"  "+c);
						c=0;
					} 
				}   
				if(c>0) {
					DatagramPacket packet = new DatagramPacket(buff, c, pack.getAddress(), pack.getPort());
					chatSocket.send(packet);
					System.out.println("D�rnier mini-packet Envoy� : "+c);
				}
				DatagramPacket packet = new DatagramPacket(new byte[0], 0, pack.getAddress(), pack.getPort());
				chatSocket.send(packet);
				System.out.println("Tout est Envoy�");
	
			}
			connection.close();

		} catch (Exception e) {
			System.out.println(e);
		}
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
			
	}


	// cette méthode est en charge à recevoir les requètes en relation qu'avec le login et le register
	public void receptForLoginRegister() {
		byte[] tampon =  new byte[BUFFER];
		try {
			packetIn = new DatagramPacket(tampon, tampon.length);
			loginSocket.receive(packetIn);
			System.out.println("un packet vient d etre recu de : " + packetIn.getSocketAddress());
			System.out.println("Le message recu est: "+new String(packetIn.getData()));
			
			
			String[] dataUser = new String(packetIn.getData()).split(",", 0);
			
			// connection avec la base de données
			try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection connection = DriverManager.getConnection(
					"jdbc:mysql://localhost:3306/clts", "root", ""
			);
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery("select * from clients");
			
			// si la requete est pour le login, on verifie les textfiels avec les tuples de la base de données
			if(dataUser[2].equals("login")){

				while (resultSet.next()) {
	
					// si ils existent, on envoie un message ok ainsi que la liste des non amis
					if(resultSet.getString(1).equals(dataUser[0]) && resultSet.getString(2).equals(dataUser[1])){
 
						for (int i = 0; i < connectedClients.size(); i++) {
							if(connectedClients.get(i).login.equals(dataUser[0])){
								connectedClients.remove(i);
							}
						}

						connectedClients.add(new Session(dataUser[0], packetIn.getPort(), packetIn.getAddress()));

						String msg = "sucess";

            			String querySelect = "SELECT login FROM clients WHERE clients.login != ? and ( clients.login not in (select user from amis where ami = ?) and clients.login not in (select ami from amis where user = ?) )";
						
						PreparedStatement preparedStmtSELECT = connection.prepareStatement(querySelect);
						preparedStmtSELECT.setString(1,dataUser[0]);
						preparedStmtSELECT.setString(2,dataUser[0]);
						preparedStmtSELECT.setString(3,dataUser[0]);


						ResultSet rsSelect = preparedStmtSELECT.executeQuery();

						String querySelectCount = "SELECT count(login) as c FROM clients WHERE clients.login != ? and ( clients.login not in (select user from amis where ami = ?) and clients.login not in (select ami from amis where user = ?) )";
						
						PreparedStatement preparedStmtSELECTCount = connection.prepareStatement(querySelectCount);
						preparedStmtSELECTCount.setString(1,dataUser[0]);
						preparedStmtSELECTCount.setString(2,dataUser[0]);
						preparedStmtSELECTCount.setString(3,dataUser[0]);


						ResultSet rsSelectCount = preparedStmtSELECTCount.executeQuery();

						while ( rsSelectCount.next() ) {
							msg = msg + "," + rsSelectCount.getString("c");
            			}


            			while ( rsSelect.next() ) {
                			msg = msg + "," + rsSelect.getString("login");
            			}

						msg = msg + ",";
						byte[] buffer = msg.getBytes();
						System.out.println(msg.getBytes());
						byte[] buff = new byte[1024];
						int c=0;int nbr=1;
						for(int i=0;i<buffer.length;i++){
							buff[c] = buffer[i];
							c++;
							if(c==1024){
								DatagramPacket packet = new DatagramPacket(buff, buff.length, packetIn.getAddress(), packetIn.getPort());
								buff = new byte[1024];
								loginSocket.send(packet);
								System.out.println("Mini-packet Envoy� : "+nbr+++"  "+c);
								c=0;
							} 
						}   
						if(c>0) {
							DatagramPacket packet = new DatagramPacket(buff, c, packetIn.getAddress(), packetIn.getPort());
							loginSocket.send(packet);
							System.out.println("D�rnier mini-packet Envoy� : "+c);
						}
						DatagramPacket packet = new DatagramPacket(new byte[0], 0, packetIn.getAddress(), packetIn.getPort());
						loginSocket.send(packet);
						System.out.println("Tout est Envoy�");

		


					}
					
					// si non, le login n'existe pas, en envoie un message d'erreur pour que l'alert soit déclancher au niveau graphique du client
					else if(resultSet.isLast()){
						
						String msg = "failed";
						
						
				byte[] buffer = msg.getBytes();
						
				byte[] buff = new byte[1024];
				int c=0;int nbr=1;
				for(int i=0;i<buffer.length;i++){
					buff[c] = buffer[i];
					c++;
					if(c==1024){
						DatagramPacket packet = new DatagramPacket(buff, buff.length, packetIn.getAddress(), packetIn.getPort());
						buff = new byte[1024];
						loginSocket.send(packet);
						System.out.println("Mini-packet Envoy� : "+nbr+++"  "+c);
						c=0;
					} 
				}   
				if(c>0) {
					DatagramPacket packet = new DatagramPacket(buff, c, packetIn.getAddress(), packetIn.getPort());
					loginSocket.send(packet);
					System.out.println("D�rnier mini-packet Envoy� : "+c);
				}
				DatagramPacket packet = new DatagramPacket(new byte[0], 0, packetIn.getAddress(), packetIn.getPort());
				loginSocket.send(packet);
				System.out.println("Tout est Envoy�");


					}
				}
				connection.close();
			}
			
			// si le client demande s'inscrire, on insère les formation à la base 
			if(dataUser[2].equals("register")){

				String query = " insert into clients (login, password)"+ " values (?, ?)";

				PreparedStatement preparedStmt = connection.prepareStatement(query);
				preparedStmt.setString (1,dataUser[0]);
				preparedStmt.setString (2,dataUser[1]);

				preparedStmt.execute();
				
				String msg = "registered";
				
				byte[] buffer = msg.getBytes();
						
						byte[] buff = new byte[1024];
						int c=0;int nbr=1;
						for(int i=0;i<buffer.length;i++){
							buff[c] = buffer[i];
							c++;
							if(c==1024){
								DatagramPacket packet = new DatagramPacket(buff, buff.length, packetIn.getAddress(), packetIn.getPort());
								buff = new byte[1024];
								loginSocket.send(packet);
								System.out.println("Mini-packet Envoy� : "+nbr+++"  "+c);
								c=0;
							} 
						}   
						if(c>0) {
							DatagramPacket packet = new DatagramPacket(buff, c, packetIn.getAddress(), packetIn.getPort());
							loginSocket.send(packet);
							System.out.println("D�rnier mini-packet Envoy� : "+c);
						}
						DatagramPacket packet = new DatagramPacket(new byte[0], 0, packetIn.getAddress(), packetIn.getPort());
						loginSocket.send(packet);
						System.out.println("Tout est Envoy�");

		
				
				connection.close();
			}
			} catch (Exception e) {
				System.out.println(e);
			}
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	// c'est ici où le traitement de la messagrie prendre place
	public void run() {
		byte[] b = new byte[BUFFER];
		
		while (true) {
			try {

				// recevoir le packet
				Arrays.fill(b, (byte) 0);
				DatagramPacket packet1 = new DatagramPacket(b, b.length);
				socket1.receive(packet1);

				// récuperer le message
				String[] messageArray = new String(b, 0, b.length).trim().split(",", 0);
				String message = messageArray[0];
				// remarque: 
				// messageArray[1] contient les ports des destinations
				// messageArray[0] contient le message à envoyer



				// récuperer les info sur le sender
				InetAddress clientAddress = packet1.getAddress();
				int client_port = packet1.getPort();

				// générer un id
				String id = clientAddress.toString() + "|" + client_port;

				System.out.println(id + " : " + message);
				
				
				// récuperer l'addr des destinataires
				byte[] data = (id + " : " + message).getBytes();
				InetAddress addr=null;
				for (int i = 0; i < connectedClients.size(); i++) {
					if(connectedClients.get(i).port == Integer.parseInt(messageArray[1])){
						addr = connectedClients.get(i).address;
						System.out.println(addr);
						break;
					}
				}
				System.out.println("src2: "+packet1.getPort());
				
				// envoi le packet au sender ( pour afficher le message chez lui aussi)
				packet1 = new DatagramPacket(data, data.length, packet1.getAddress(), packet1.getPort());
				socket1.send(packet1);
				
				// envoi le packet au destinataires
				packet1 = new DatagramPacket(data, data.length, addr, Integer.parseInt(messageArray[1])+1);
				socket1.send(packet1);

				
			} catch (Exception e) {
				System.err.println(e);
			}
		}
	}

}