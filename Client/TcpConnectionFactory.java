package Client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;

public class TcpConnectionFactory 
{

	    private InetAddress serverAddress;
	    private int serverPort;
	    private SocketChannel socketChannel;

	    /**
	     * Crea una nuova TcpPConnectionFactory configurata con hostname e porta specificati.
	     *
	     * @param serverHostname l'hostname che verra  usato
	     * @param serverPort la porta che verra' usata
	     * @throws UnknownHostException
	     */
	    public TcpConnectionFactory(String serverHostname, int serverPort) throws UnknownHostException 
	    {
	        this.serverAddress = InetAddress.getByName(serverHostname);
	        this.setServerPort(serverPort);
	    }

	    /**
	     * Restituisce l'indirizzo col quale Ã¨ stata configurata la factory.
	     *
	     * @return l'indirizzo
	     */
	    public InetAddress getServerAddress() 
	    {
	        return serverAddress;
	    }

		public int getServerPort() 
		{
			return serverPort;
		}

		public void setServerPort(int serverPort)
		{
			this.serverPort = serverPort;
		}
		
		public void makeConnection() throws IOException 
		{
			SocketAddress address=new InetSocketAddress(serverAddress, serverPort);
			socketChannel=SocketChannel.open();
			socketChannel.connect(address);
		}
		
		public SocketChannel getSocketChannel() 
		{
			return socketChannel;
		}
		
		public void close() throws IOException
		{
			socketChannel.close();
		}

}
