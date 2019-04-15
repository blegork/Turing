package Server;
import java.rmi.Remote;
import java.rmi.RemoteException;

import Client.RemoteClientCallback;

public interface RemoteRegistrationClient extends Remote
{
	
	int registrationClient(String Username, String Password) throws RemoteException;
	void register(RemoteClientCallback client, String utente) throws RemoteException;

}
