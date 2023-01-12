public class mainServer {
    
    public static void main(String args[]) throws Exception {
		ChatServer server_thread = new ChatServer();

		// lancer le Thread des logins
		Thread loginThread = new Thread() {
            // run() method of a thread
            public void run()
            {
				while(true){
					server_thread.receptForLoginRegister();
				}
            }
        };

		loginThread.start();

		// lancer le Thread de chat
		Thread chatThread = new Thread() {
            // run() method of a thread
            public void run()
            {
				while(true){
					server_thread.receptForChatFunctionalities();
				}
            }
        };

		chatThread.start();
		server_thread.run();
	}

}
