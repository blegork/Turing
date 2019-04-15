package Client;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.BlockingQueue;

import Server.RemoteRegistrationClient;

public class RmiConnection
{
	private RemoteRegistrationClient serverObject;
	private Remote RemoteObject;
	
	/**
     * Crea il riferimento all’oggetto remoto effettivamente attivo su server Turing.
     *
     * @param serverHostname l'hostname che verra  usato
     * @param serverPort la porta che verra' usata
     * @throws RemoteException, NotBoundException
     */
	public RmiConnection(String serverHostname,int serverPort) throws RemoteException, NotBoundException
	{
		Registry r = LocateRegistry.getRegistry(serverPort);
		RemoteObject = r.lookup(serverHostname);
		serverObject = (RemoteRegistrationClient) RemoteObject;
	}
	/**
     * Crea il riferimento alla callBack ed effettua la regitrazione di quest'utlima sul server.
     *
     * @param user nome utente
     * @param queue coda notifica
     * @throws RemoteException
     */
	public void registrer(String user,BlockingQueue<String> queue) throws RemoteException
	{
		RemoteClientCallback clientCallback = new ClientCallbackImpl(queue);
		UnicastRemoteObject.exportObject(clientCallback ,0);
		serverObject.register(clientCallback , user);
	}
	
	public RemoteRegistrationClient getServerObject()
	{
		return serverObject;
	}
}