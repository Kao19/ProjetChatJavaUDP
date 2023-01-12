import javax.swing.*;
import java.awt.event.*;

public class ChatClient extends JFrame implements ActionListener {

	private JButton login;
	private JButton register;
	JPanel panel;
	JFrame frame;

	// Interface de login et register
	ChatClient(){
		
		frame = new JFrame("first page");

      	panel = new JPanel();	
		frame.add(panel);

        this.login = new JButton("login");
        panel.add(login);

		this.register = new JButton("register");
        panel.add(register);


		this.login.addActionListener(this);
        this.register.addActionListener(this);
		
		
		frame.setSize(800,600);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	// selon le choix du client on instancie un objet d'une autre classe s'appelle CreateLoginForm
	public void actionPerformed(ActionEvent e){
		JButton actionSource = (JButton) e.getSource();

    	if(actionSource.equals(login)){
        	CreateLoginForm c = new CreateLoginForm("login");
			frame.setVisible(false);
    	} else if (actionSource.equals(register)) {
			CreateLoginForm c = new CreateLoginForm("register");
			frame.setVisible(false);
    	}
	}
	
	public static void main(String args[]) throws Exception {
		
		ChatClient cl = new ChatClient();		
	}
}