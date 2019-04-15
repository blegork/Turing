package Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

import javax.swing.JTextArea;

public class ThreadUdp implements Runnable
{
	
	private String adress;
	private int PORT = 8888;
    private JTextArea textArea;
    String utente;
//    private boolean stop=false;
	
    
	public ThreadUdp(String utente, JTextArea textArea, String adress)
	{
		this.textArea=textArea;
		this.utente=utente;
		this.adress= adress;
	}
	
	public void run() 
	{

        InetAddress address=null;
		try {
			address = InetAddress.getByName(adress);
		} catch (UnknownHostException e) 
		{
			e.printStackTrace();
		}
        
        byte[] buf = new byte[256];

        
        try (MulticastSocket clientSocket = new MulticastSocket(PORT))
        {

            clientSocket.joinGroup(address);
      
            while (!Thread.interrupted()) 
            {
            	
            	DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
                clientSocket.receive(msgPacket);
                
 
                String msg = new String(buf, 0, msgPacket.getLength());
                textArea.append(msg+"\n");
            }
        } 
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
		
		
	}

}
