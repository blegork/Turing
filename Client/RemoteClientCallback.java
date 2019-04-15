package Client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteClientCallback extends Remote 
{

    void notifyShare(String notifica) throws RemoteException;

}