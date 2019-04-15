 package Client.Gui;


import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.awt.event.ActionEvent;
import javax.swing.JPasswordField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;



import Client.ComunicationManager;
import Client.Client;
import Client.ResponseException;
import Client.RmiConnection;
import Client.TcpConnectionFactory;
import static Server.Server.SERVER_PORT;

import java.awt.Font;
import javax.swing.JPanel;

public class LoginMainForm 
{

	private JFrame frame;
	private JPasswordField passwordField;
	private JTextField usernameField;
	private TcpConnectionFactory factory;
	private ComunicationManager comunicationManager;
	private Client client;
	private RmiConnection rmi;
	private HomeForm windowMain;
	private JPanel panel = new JPanel();
	private BlockingQueue<String> queue;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) 
	{
		
		LoginMainForm window = new LoginMainForm();
		window.frame.setVisible(true);
	}

	/**
	 * Create the application.
	 * @throws IOException 
	 */
	public LoginMainForm()
	{
		frame = new JFrame("Login - Turing");
		frame.setBounds(800, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		
		
		
		JButton loginButton = new JButton("Login");
		
		panel.setBounds(0, 32, 54, -32);
		frame.getContentPane().add(panel);
		loginButton.setBounds(71, 191, 98, 26);
		frame.getContentPane().add(loginButton);
		
		passwordField = new JPasswordField(20);
		passwordField.setBounds(260, 89, 91, 20);
		frame.getContentPane().add(passwordField);
		
		JLabel lblNewLabel = new JLabel("Username");
		lblNewLabel.setFont(new Font("Dialog", Font.BOLD, 16));
		lblNewLabel.setBounds(78, 43, 91, 20);
		frame.getContentPane().add(lblNewLabel);
		
		JLabel lblPassword = new JLabel("Password");
		lblPassword.setFont(new Font("Dialog", Font.BOLD, 16));
		lblPassword.setBounds(78, 89, 91, 20);
		frame.getContentPane().add(lblPassword);
		
		JButton registerButton = new JButton("Register");
		registerButton.setBounds(260, 191, 98, 26);
		frame.getContentPane().add(registerButton);
		
		usernameField = new JTextField(20);
		usernameField.setBounds(260, 43, 91, 20);
		frame.getContentPane().add(usernameField);
		usernameField.setColumns(10);
		
		
		
		try 
		{
			rmi = new RmiConnection("TURING-SERVER",6001);
		} 
		catch (RemoteException | NotBoundException e3) 
		{
			JOptionPane.showMessageDialog(frame,"Errore di  connessione");
		}
		
		registerButton.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				if (usernameField.getText().isEmpty()) 
	            {
					JOptionPane.showMessageDialog(frame, "Utente non puo' essere vuoto");
	                
	                return;
	            }

				{
					try 
					{
						String password = new String(passwordField.getPassword());
						String username = usernameField.getText();
						ComunicationManager.register(username, password, rmi);
						JOptionPane.showMessageDialog(frame, "Registrazione avvenuta, con successo");
					}
					catch (NotBoundException | RemoteException e1)
					{
						JOptionPane.showMessageDialog(frame, "Errore di connesione: " + e1.getMessage());
					}
					catch(NullPointerException e1)
					{
						JOptionPane.showMessageDialog(frame, "Errore di connesione");
					}
					catch(ResponseException e1)
					{
						JOptionPane.showMessageDialog(frame, e1.getMessage());
					}
				}
			}
			
		});
		
		loginButton.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				if (usernameField.getText().isEmpty()) 
	            {
					JOptionPane.showMessageDialog(frame, "Utente non puo' essere vuoto");
	                return;
	            }
				String password = new String(passwordField.getPassword());
				try 
				{
					
					
					queue= new LinkedBlockingQueue<String>();
					factory = new TcpConnectionFactory("localhost", SERVER_PORT);  
					factory.makeConnection();
					comunicationManager = new ComunicationManager(factory, usernameField.getText(), password);
					client = new Client(comunicationManager, queue, rmi);
					windowMain = new HomeForm(usernameField.getText(),client, frame);
					
					
					windowMain.frame.setVisible(true);
					frame.setVisible(false);
				} 
				catch (ResponseException e1) 
	            {
	                
	                try 
	                {
						factory.close();
					} 
	                catch (IOException e2) 
	                {
						e2.printStackTrace();
					}
	                JOptionPane.showMessageDialog(frame,e1.getMessage());
	                
	            }
	            catch (IOException e1) 
	            {
	            	JOptionPane.showMessageDialog(frame, e1.getMessage());
	            }
			}
		});
	}
}
