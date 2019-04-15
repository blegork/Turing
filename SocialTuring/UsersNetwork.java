package SocialTuring;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Una UsersNetwork rappresenta una rete di utenti con alcuni vincoli come l'univocita'  dei nomi.
 */
public class UsersNetwork
{

    private final Map<String, User> usersMap = new HashMap<>();
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    
    
    public UsersNetwork()
    {
    	
    }
    /**
     * Crea un utente con nome e password specificati e lo aggiunge alla rete. Restituisce l'utente appena creato oppure
     * null se un utente con quel nome gia'  esiste nella rete.
     *
     * @param username il nome dell'utente
     * @param password la password dell'utente
     * @return l'utente appena creato oppure null
     */
    public User addUser(String username, String password)
    {
        readWriteLock.writeLock().lock();
        try 
        {
            if (getUser(username) != null)
                return null;
            User u = new User(username, password);
            usersMap.put(username, u);
            return u;
        } 
        finally 
        {
            readWriteLock.writeLock().unlock();
        }
    }
    
    /**
     * Trova e restituisce un utente col nome specificato. Se un utente con quel nome non e' registrato alla rete
     * restituisce null.
     *
     * @param username il nome dell'utente da cercare
     * @return l'utente col nome specificato oppure null
     */
    public User getUser(String username)
    {
        readWriteLock.readLock().lock();
        try 
        {
            return usersMap.get(username);
        } 
        finally 
        {
            readWriteLock.readLock().unlock();
        }
    }
}
