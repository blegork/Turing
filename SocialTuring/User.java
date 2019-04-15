package SocialTuring;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import Client.RemoteClientCallback;

public class User
{

    private final String username;
    private final String password; 
    private List<Document> document;
    private List<String> pendentNotify=new ArrayList<String>();
    private boolean online=false;
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final ReentrantLock lockNotify = new ReentrantLock();
    private RemoteClientCallback client;
    /**
     * Crea un nuovo utente con nome e password specificati.
     *
     * @param username il nome dell'utente
     * @param password la password dell'utente
     * @throws IllegalArgumentException se uno degli argomenti e' null
     */
    public User(String username, String password)
    {
        if (username == null || password == null)
            throw new IllegalArgumentException();
        this.username = username;
        this.password = password;
        this.document = new ArrayList<>();
    }
	public boolean isOnline()
	{
		readWriteLock.readLock().lock();
		try
		{
			return online;
		}
		finally 
        {
            readWriteLock.readLock().unlock();
        }
	}

	public void setOnline(boolean online)
	{
		readWriteLock.writeLock().lock();
		try
		{
			this.online = online;
		}
		finally 
        {
            readWriteLock.writeLock().unlock();
        }
	}
	
    public String getUsername()
    {
        return this.username;
    }

    public String getPassword()
    {
        return this.password;
    }

    /**
     * Aggiunge un elemento alla lista dei documenti modificabili dall'utente.
     *
     * @param il documento da aggiungere alla lista dei documenti, modificabili da this
     */
    public void addDocument(Document document)
    {
        this.document.add(document);
    }

    /**
     * Restituisce la lista di tutti i documenti modificabili dall'utente.
     *
     * @return lista non modificabile dei documenti dell'utente
     */
    public List<Document> getDocuments() 
    {
        return document;
        
    }
	public RemoteClientCallback getClientCallback() 
	{
		return client;
	}
	
	public void setClient(RemoteClientCallback client)
	{
		this.client = client;
	}
	
	
	
	public ArrayList<String> getPendentNotify()
	{
		try
		{
			lockNotify.lock();
			ArrayList<String> copy=   new ArrayList<String>(pendentNotify);
			pendentNotify.clear();
			return copy;
		}
		finally
		{
			lockNotify.unlock();
		}
	}
	public void addPendentNotify(String notify) 
	{
		try
		{
			lockNotify.lock();
			this.pendentNotify.add(notify);
		}
		finally
		{
			lockNotify.unlock();
		}
	}
	

}