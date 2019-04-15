package Client;

import java.rmi.RemoteException;
import java.util.concurrent.BlockingQueue;

/**
 * Crea una callback, che sara' richiamata dal server, per ricevere le notifiche.
 *
 * @param serverHostname l'hostname che verra  usato
 * @param serverPort la porta che verra' usata
 * @throws RemoteException, NotBoundException
 */
public class ClientCallbackImpl implements RemoteClientCallback
{
	private BlockingQueue<String> queue;
	public ClientCallbackImpl ()
	{
		
	}
	
	public ClientCallbackImpl (BlockingQueue<String> queue)
	{
		this.queue=queue;
	}
	
	public void notifyShare(String notifica) throws RemoteException 
	{
		queue.add(notifica);

	}
}
