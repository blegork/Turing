package Client.Gui;

import javax.swing.JFrame;

import Client.ThreadUdp;

import javax.swing.JButton;
import javax.swing.JTextArea;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;

public class ChatForm 
{

	JFrame frame;
    private int PORT = 8888;
	private Thread thread;
	private ThreadUdp t;
	public ChatForm(String utente, String adress) throws UnknownHostException 
	{
		
		InetAddress addr = InetAddress.getByName(adress);
		frame = new JFrame("Chat: "+utente);
		frame.setBounds(100, 100, 540, 353);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JTextArea chatArea = new JTextArea();
		chatArea.setEditable(false);
		
		JScrollPane scrollChatArea = new JScrollPane(chatArea);
		scrollChatArea.setBounds(10, 11, 492, 218);
		frame.getContentPane().add(scrollChatArea);
		
		JTextArea inputArea = new JTextArea();
		
		JScrollPane scrollInputArea = new JScrollPane(inputArea);
		scrollInputArea.setBounds(10, 247, 367, 53);
		frame.getContentPane().add(scrollInputArea);
		
		t = new ThreadUdp(null, chatArea, adress);
		thread = new Thread(t);
		thread.start();
		
		
		JButton btnNewButton = new JButton("Send Message");
		
		btnNewButton.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				try (DatagramSocket serverSocket = new DatagramSocket()) 
		        {
					String msg = utente+": "+inputArea.getText();
					inputArea.setText("");
		            DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(),msg.getBytes().length, addr, PORT);
		            serverSocket.send(msgPacket);
		            
		        } catch (IOException ex) 
		        {
		            ex.printStackTrace();
		        }
			}
		});
		btnNewButton.setBounds(387, 264, 129, 23);
		frame.getContentPane().add(btnNewButton);
		
		
		
	
	}
	public void interruptThread()
	{
		thread.interrupt();
	}
}
