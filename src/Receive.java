import java.net.*;

// classe pour regrouper le traitement de reception des messages
class Receive implements Runnable {
	DatagramSocket socket;
	byte buffer[];
	ChatGUI window;

	Receive(DatagramSocket sock, ChatGUI win) {
		socket = sock;
		buffer = new byte[1024];
		window = win;
	}

	public void run() {
		while (true) {
			try {
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet);
				String received = new String(packet.getData(), 1, packet.getLength() - 1).trim();
				System.out.println(received);
				window.displayMessage(received);
			} catch (Exception e) {
				System.err.println(e);
			}
		}
	}
}
