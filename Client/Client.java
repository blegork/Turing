package Client;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

/**
* Un oggetto Client, tramite una connessione TCP, inoltra a un Turing server le richieste di modifica, creazione
* lettura, di un file. L'utente e' identificato tramite un ComunicationManager.
*/
public class Client 
{

	private String username;
	private String data;
	private ComunicationManager comunicationManager;
	/**
     * Crea un nuovo oggetto Client, avvia un thread che legge le notifiche e registra una callback per rivevere le notifiche
     * 
     *
     * @param comunicationManager ComunicationManager, non null
     * @param queue BlockingQueue<String>, non null
	 * @param rmi, per registrare la callback non null
	 * @throws ResponseException se l'username e la password sono errati
	 * @throws IOException
     */
	public Client(ComunicationManager comunicationManager, BlockingQueue<String> queue, RmiConnection rmi) throws IOException, ResponseException 
	{
		if (comunicationManager == null)
            throw new IllegalArgumentException();
		
		this.comunicationManager = comunicationManager;
        this.username = comunicationManager.getUsername();
        
        rmi.registrer(this.username, queue);
		comunicationManager.login();
        Thread t = new Thread(new ThreadNotify(queue));
        t.start();
        
	}
	/**
     * Crea un nuovo documento
     * 
     *
     * @param nameFile, nome documento da creare
     * @param section, numero di sezioni di un documento
	 * @throws ResponseException se un file con nameFile esiste gia'
	 * @throws IOException
     */
	public void createDocument(String nameFile, int section) throws IOException, ResponseException
	{
		data=nameFile+"\n"+section;
		comunicationManager.createDocument(data);
	}
	/**
     * Condivide un documento
     * 
     *
     * @param nameFile, nome documento da condividere
     * @param utenteInvitato, nome utente invitato a collaborare a un documento
	 * @throws ResponseException se il File non esiste, se l'utente non ha i permessi o se l'utente invitato non esiste
	 * @throws IOException
     */
	public void shareDocument(String utenteInvitato, String nameFile) throws IOException, ResponseException
	{
		data=nameFile+"\n"+utenteInvitato;
		comunicationManager.shareDocument(data);
	}
	
	/**
     * Edita un documento
     * 
     *
     * @param nameFile, nome documento da editare
     * @param section, numero sezione da editare
	 * @throws ResponseException se il File non esiste, se l'utente non ha i permessi o un altro utente sta editando questa sezione
	 * @throws IOException
     */
	public String startEditDocument(String nameFile, int section) throws IOException, ResponseException
	{
		
		data = nameFile+"\n"+section;
		return comunicationManager.receiveFile(data);
	}
	
	/**
     * Finisce di editare un docuemento
     * 
     *
	 * @throws IOException
     */
	public void endEditDocument() throws IOException
	{
		comunicationManager.endEditDocument();
	}
	/**
     * Mostra lista documenti, a cui un utente puo' collaborare
     * 
     *
	 * @throws IOException
     */
	public String listDocument() throws IOException
	{
		return comunicationManager.listDocument();
	}
	
	/**
     * Mostra sezione di un documento
     * 
     *
     * @param nameFile, nome documento
     * @param section, numero sezione da mostrare
	 * @throws ResponseException se il File non esiste, se l'utente non ha i permessi
	 * @throws IOException
     */
	public String showSection(String nameFile, int section) throws IOException, ResponseException 
	{
		return comunicationManager.showSection(nameFile, section);
	}
	/**
     * Mostra un documento
     * 
     *
     * @param nameDocument nome documento
	 * @throws ResponseException se il File non esiste, se l'utente non ha i permessi
	 * @throws IOException
     */
	public String showDocument(String nameDocument) throws IOException, ResponseException 
	{
		return comunicationManager.showDocument(nameDocument);
	}
	/**
     * Effetua logout
     * 
     *
	 * @throws ResponseException se il File non esiste, se l'utente non ha i permessi
	 * @throws IOException
     */
	public void logout() throws IOException, ResponseException 
	{
		comunicationManager.logout();
	}
	
	public TcpConnectionFactory getConnection()
	{
		return comunicationManager.getConnection();
	}
	
	public String getUsername() 
	{
        return username;
    }
}
