package Server;

import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;

import Client.RemoteClientCallback;
import SocialTuring.UsersNetwork;

public class ServerRMI extends RemoteServer implements RemoteRegistrationClient
{
	private static final long serialVersionUID = 1L;
	private UsersNetwork usersNetwork;
	public static RemoteClientCallback client;
	
	ServerRMI(UsersNetwork usersNetwork)
	{
		this.usersNetwork=usersNetwork;
	}
	public int registrationClient(String Username, String Password) throws RemoteException
	{
		if (null == usersNetwork.addUser(Username, Password))
		{
			return ResponseTypes.INVALID_CREDENTIALS;
        }
		else
		{
			System.out.println("[INFO] New user: " + Username);
			return ResponseTypes.OK;
		}
	}
	
	public void register(RemoteClientCallback client, String utente) throws RemoteException 
	{
		if(usersNetwork.getUser(utente)!=null)
			usersNetwork.getUser(utente).setClient(client);
	}

}
