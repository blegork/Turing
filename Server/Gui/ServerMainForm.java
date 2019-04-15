package Server.Gui;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.awt.event.ActionEvent;

import Server.Server;

import java.awt.TextArea;

public class ServerMainForm 
{
	private TextArea textArea;
	private JFrame frame;
	
	public void appendToTextArea(String string)
	{
		textArea.append(string);
	}
	

	/**
	 * Create the application.
	 */
	public ServerMainForm() 
	{
		
		// Dirotta lo stdout e stderr sulla textarea
        OutputStream out = new OutputStream() 
        {
            @Override
            public void write(final int b) throws IOException 
            {
                appendToTextArea(String.valueOf(b));
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException 
            {
                appendToTextArea(new String(b, off, len, StandardCharsets.UTF_8));
            }

            @Override
            public void write(byte[] b) throws IOException 
             {
                write(b, 0, b.length);
            }
        };

        PrintStream ps = new PrintStream(out, true);
        Server server = new Server(ps);
        System.setErr(ps);
        System.setOut(ps);
		
		
		frame = new JFrame("Server");
		frame.setBounds(100, 100, 600, 386);
		frame.setMinimumSize(new Dimension(450, 300));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JButton startButton = new JButton("Start");
		JButton stopButton = new JButton("Stop");
		stopButton.setEnabled(false);
		stopButton.addActionListener(new ActionListener() 
		{
			 
			public void actionPerformed(ActionEvent e) 
			{
				startButton.setEnabled(true);
	            stopButton.setEnabled(false);
	            server.close();
			}
			
			});
		startButton.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				startButton.setEnabled(false);
	            stopButton.setEnabled(true);
	            
	            new Thread() 
	            {
	                @Override
	                public void run() 
	                {
	    				System.out.println("[LOG]: Server avviato");
	    				try {
	    					server.startServer();
	    				} 
	    				catch(RemoteException e2)
	    				{
	    					server.log("[Error]: Errore RMI");
	    					e2.getMessage();
	    				}
	    				catch (IOException e1) 
	    				{
	    					server.log("[Error]: crezione socket");
	    					e1.getMessage();
	    				}
	                }
	            }.start();
			}
		});
		startButton.setBounds(44, 309, 98, 26);
		frame.getContentPane().add(startButton);
		
		
		stopButton.setBounds(425, 309, 98, 26);
		frame.getContentPane().add(stopButton);
		
		textArea = new TextArea();
		textArea.setBounds(24, 25, 540, 252);
		textArea.setEditable(false);
		textArea.setFocusable(false);
		frame.getContentPane().add(textArea);
	}
	
	
	public static void main(String[] args) 
	{
		ServerMainForm window = new ServerMainForm();
		window.frame.setVisible(true);
	}
}
