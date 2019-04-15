package SocialTuring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Document
{
	private final int section;
	private String path;
	private String autore;
	private String nome;
	private byte[] IPMulticast;
	private List<String> collaboratori= new ArrayList<String>();
	private List<String> editSection= Collections.synchronizedList(new ArrayList<String>());
	
	private int editor;
	private byte[] response;
	private Lock lockEditor = new ReentrantLock();
	private Lock lockIp = new ReentrantLock();
	
	 /**
     * Crea un nuovo documento con autore, path di destinazione, nome File, numero di sezioni.
     *
     * @param autore il nome dell'utente che ha creato il documento
     * @param path, path di destinazione file
     * @param nome, nome del file
     * @param section, numero di sezioni del file
     * 
     */
	public	Document(String autore, String path, String nome, int section) 
    {
    	this.section=section;
    	this.path=path;
    	this.autore=autore;
    	this.nome=nome;
    	this.IPMulticast=null;
    }
	/**
     * Metodo per richiedere autore del file.
     *
     * @return autore del file
     * 
     */
	public String getAutore()
	{
		return autore;
	}
	/**
     * Metodo per richidere nome del file.
     *
     * @return nome del file
     * 
     */
	public String getNome()
	{
		return nome;
	}
	
	/**
     * Restituisce path del file.
     *
     * @return path del file
     * 
     */
	public String getPath()
	{
		return path;
	}
	
	/**
     * Restituisce un stringa con i collaboratori del file, separati da un "-" come delimitatore
     *
     * @return collaboratori file
     * 
     */
	public String getCollaboratori()
	{
		return String.join("-", collaboratori);
	}
	
	/**
     * Metodo per sapere se un utente e' collaboratore di un file
     *
     * @param nome utente
     * @return true, se un utente e' un collaboratore di un file
     * 
     */
	public boolean isCollaboratore(String utente)
	{
		return collaboratori.contains(utente);
	}
	
	/**
     * Metodo per sapere quali sezioni di un file, sono nello stato edit
     *
     * 
     * @return stringa con tutte le sezioni in edit
     * 
     */
	public String getEditSection()
	{
		return String.join("-", editSection);
	}
	
	public void removeEditSection(String section)
	{
		editSection.remove(section);
	}
	
	public void addEditSection(String section)
	{
		editSection.add(section);
	}
	
	public boolean isAutore(String autore)
	{
		return this.autore.equals(autore);
	}
	
	public void addCollaboratori(String collaboratore)
	{
		collaboratori.add(collaboratore);
	}

	public int getSection() 
	{
		return section;
	}

	public byte[] getUDPMulticast() 
	{
		lockIp.lock();
		response = IPMulticast;
		lockIp.unlock();
		
		return response;
	}

	public void setUDPMulticast(byte[] uDPMulticast) 
	{	
		lockIp.lock();
		IPMulticast = uDPMulticast;
		lockIp.unlock();
	}

	public void addEditor() 
	{
		lockEditor.lock();
			this.editor++;
		lockEditor.unlock();
	}
	
	public boolean removeEditor()
	{
		boolean empty = false;
		lockEditor.lock();
		this.editor--;
		if(this.editor==0)
		{
			empty = true;
		}
		lockEditor.unlock();
		return empty;
	}
}
