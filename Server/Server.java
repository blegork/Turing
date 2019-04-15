package Server;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import SocialTuring.DocumentNetwork;
import SocialTuring.IpMulticastChat;

import SocialTuring.UsersNetwork;

public class Server
{
	//Logica del programma
	static UsersNetwork usersNetwork = new UsersNetwork();
	static DocumentNetwork documentNetwork = new DocumentNetwork();
	static IpMulticastChat ipMulticast = new IpMulticastChat();
	static Map<String, String> editDocument = Collections.synchronizedMap(new HashMap<String, String>());
	private boolean closed;
	private final PrintStream console;
	private ExecutorService executor;
	private SelectionKey key=null;
	
	//Logica di rete
	private ServerSocketChannel serverChannel;
	Selector selector;
	private static ServerRMI statsService;
	private static Registry reg;
	private static Registry r;
	public static final int SERVER_PORT = 6000;
	public static File theDir = null;
	
	public void startServer() throws RemoteException, IOException
    {
		createDir();
		log("[INFO] Server started");
		Server.startRMI();
		log("[INFO] Server-RMI started");
		startLoop();
    }
	public void log(String s) 
	{
        if (console != null)
            console.println(s);
    }
	
	public Server(PrintStream console)
	{
        this.console = console;
	}
	
	UsersNetwork getUsersNetwork()
    {
        return usersNetwork;
    }
	
	public static void startRMI() throws RemoteException
	{
		statsService = new ServerRMI(usersNetwork);
		RemoteRegistrationClient stub = (RemoteRegistrationClient)UnicastRemoteObject.exportObject(statsService, 0);
		reg = LocateRegistry.createRegistry(6001);
		r=LocateRegistry.getRegistry(6001);
		r.rebind("TURING-SERVER", stub);
	}
	private void startLoop() throws IOException 
	{
		closed = false;
		executor= Executors.newFixedThreadPool(4);
		
		serverChannel = ServerSocketChannel.open();
		serverChannel.socket().bind(new InetSocketAddress(SERVER_PORT));
		serverChannel.configureBlocking(false);
		selector = Selector.open();
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		int n=0;
		while (!closed) 
		{
			try 
			{
				n=selector.select();
			}
			catch (ClosedSelectorException e)
			{
				if (!closed)
				{
					log("[ERROR] Close Selector: " + e.getMessage());
					e.printStackTrace();
				}
				break;
			}
			catch (IOException ex) 
			{
				ex.printStackTrace();
				break;
			}
			if (n==0) {continue;};
			Set<SelectionKey> readyKeys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = readyKeys.iterator();
			try
			{
				while (iterator.hasNext()) 
				{
					key = iterator.next();
					iterator.remove();
	
					if (key.isAcceptable())
					{
						ServerSocketChannel server = (ServerSocketChannel) key.channel();
						SocketChannel client = server.accept();
						log("[LOG] Accepted new connection");
						client.configureBlocking(false);
						key = client.register(selector,SelectionKey.OP_READ);
						key.attach(new AttachChannel(false));
					}
					else if (key.isReadable() && !(((AttachChannel)key.attachment()).getState()))
					{
						((AttachChannel)key.attachment()).setState(true);
						int i= ((SocketChannel) key.channel()).read(((AttachChannel)key.attachment()).getInput());
						if(i>0)
						{
							((AttachChannel)key.attachment()).setState(true);
							ServerTask task = new ServerTask(this, key);
							executor.submit(task);
						}
						else if(i==-1)
						{
							key.cancel();
							key.channel().close();
						}
						else
						{
							((AttachChannel)key.attachment()).setState(false);
						}
				    }
				}
			}
			catch (IOException ex) 
			{
				
				try 
				{
					key.cancel();
				    key.channel().close();
					if(((AttachChannel)key.attachment()).getUtente()!=null)
					{
						log("[LOG]: Utente disconesso: "+((AttachChannel)key.attachment()).getUtente().getUsername());
						((AttachChannel)key.attachment()).getUtente().setOnline(false);
						editDocument.remove(((AttachChannel)key.attachment()).getUtente().getUsername());
					}
				} 
				catch (IOException cex) 
				{
					ex.getStackTrace();
				} 
			}
		}
	}
	public void close()
	{
		closed=true;
			try 
			{
				if(serverChannel!=null)
				{
					reg.unbind("TURING-SERVER");
					UnicastRemoteObject.unexportObject(reg, true);
					
					serverChannel.socket().setReuseAddress(true);
					selector.close();
					serverChannel.close();
					serverChannel.socket().close();
				}
				executor.shutdown();
				log("[INFO]: Server successfully stopped.");
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
	}
	private void createDir()
	{
		theDir = new File("Document");
		
		if(!theDir.exists())
		{
			theDir.mkdir();
			log("[INFO]: Cartella 'Documenti' creata");
		}
		else
		{
			String[]entries = theDir.list();
			for(String s: entries)
			{
			    File currentFile = new File(theDir.getPath(),s);
			    deleteDir(currentFile);
			}
		}
	}
	
	//cancella file e directory
	void deleteDir(File file) 
	{
	    File[] contents = file.listFiles();
	    if (contents != null) {
	        for (File f : contents)
	        {
	            deleteDir(f);
	        }
	    }
	    file.delete();
	}
}
