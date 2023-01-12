import java.net.*;

// sert à ajouter tout nouveau client à une session en stockant ses info (addr, login, port)
public class Session {

    public String login;
    public int port;
    public InetAddress address;

    public Session(String login, int port, InetAddress address) {
        this.login = login;
        this.port = port;
        this.address = address;
    }
    
}
