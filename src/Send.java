import java.net.*;
import java.util.*;

// classe pour regrouper le traitement d'envoi de message

class Send implements Runnable {
	public final static int PORT = 1;
	private DatagramSocket socket;
	private String hostName;
	private ChatGUI window;
	private ArrayList<Integer> destinationPort = new ArrayList<>();

	Send(DatagramSocket sock, String host, ChatGUI win, int destPort) {
		socket = sock;
		hostName = host;
		window = win;
		this.destinationPort.add(destPort);
	}

	private void sendMessage(String s,String destPort) throws Exception {
		String msg = s+","+destPort;
		byte buffer[] = msg.getBytes();
		InetAddress address = InetAddress.getByName(hostName);
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, PORT);
		socket.send(packet);
	}

	public void run() {
		boolean connected = false;
		String dests = "";
		do {
			try {
				for(int i=0; i<destinationPort.size();i++){
					dests = destinationPort.get(i) + ",";
				}
				sendMessage("New client connected - welcome!",dests);
				connected = true;
			} catch (Exception e) {
				window.displayMessage(e.getMessage());
			}
		} while (!connected);
		while (true) {
			try {
				while (!window.message_is_ready) {
					Thread.sleep(100);
				}
				for(int i=0; i<destinationPort.size();i++){
					dests = destinationPort.get(i) + ",";
				}
				sendMessage(window.getMessage(),dests);
				window.setMessageReady(false);
			} catch (Exception e) {
				window.displayMessage(e.getMessage());
			}
		}
	}
}
