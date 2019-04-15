package SocialTuring;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Una DocumentNetwork rappresenta una rete di documenti con alcuni vincoli come l'univocita'  dei nomi.
 */
public class DocumentNetwork
{
    private final Map<String, Document> documentMap = new HashMap<>();
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public DocumentNetwork ()
    {}
    
    /**
     * Aggiunge un documento con autore, path di destinazione, nome File e numero di sezioni specificati e lo aggiunge.
     *
     * @param autore il nome dell'autore del file
     * @param path il percorso di memorizazione del file
     * @param nome del file
     * @param section numero di sezioni del file
     * 
     */
    public void addDocument(String autore, String path, String nome, int section)
    {
        readWriteLock.writeLock().lock();
        try
        {
        	if(documentMap.get(path)==null)
        	{
        		documentMap.put(path, new Document(autore, path,nome, section));
        	}
        }
        finally 
        {
            readWriteLock.writeLock().unlock();
        }
    }
    
    /**
     * Trova un file con path specificato o null se non esiste
     * 
     * @param path il percorso di memorizazione del file
     * 
     * @return documento o null se non trovato
     * 
     */
    public Document getDocument(String path)
    {
    	readWriteLock.readLock().lock();
    	try
    	{
    		return documentMap.get(path);
    	}
    	finally
    	{
    		readWriteLock.readLock().unlock();
    	}
    }
}
