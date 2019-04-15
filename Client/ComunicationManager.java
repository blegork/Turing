package Client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import Server.*;
/**
 * Un ComunicationManager gestisce la connessione di un singolo utente in un Turing server.
 * In particolare si occupa della registraazione, login di un utente e di tutte le altre operazioni, 
 * quali creazione di un documento etc
 */
public class ComunicationManager
{
	private String username;
	private String password;
    private TcpConnectionFactory tcpConnectionFactory;
    private ByteBuffer byteBufferWrite=ByteBuffer.allocateDirect(1024);
    private ByteBuffer byteBufferRead=ByteBuffer.allocateDirect(1024);
    private SocketChannel socketChannel;
	private int response;
	private long sizeFile;
	private int sizeString;
    private String adress;
    
    /**
     * Crea un nuovo ComunicationManager.
     *
     * @param tcp, TcpConnectionFactory
     * @param username lo username dell'utente
     * @param password la password dell'utente
     * @throws IllegalArgumentException se uno degli argomenti e' null
     */
	public ComunicationManager(TcpConnectionFactory tcp, String username, String password)
            throws IllegalArgumentException 
    {
        if (tcp == null || username == null || password == null)
            throw new IllegalArgumentException();
        this.tcpConnectionFactory = tcp;
        this.username = username;
        this.password = password;
        socketChannel = tcpConnectionFactory.getSocketChannel();
    }
    
	 /**
     * Invia una richiesta di registrazione al Turing server.
     *
     * @param factory  la factory di TcpConnectionFactory
     * @param username lo username da registrare
     * @param password la password da registrare
     * 
     * @throws RemoteExceptionn se c'e' un problema di comunicazione col server
     * @throws ResponseException se il server ha risposto che un utente con quel nome gia' esiste
	 * @throws NotBoundException 
     */
	public static void register(String username, String password, RmiConnection rmi) throws RemoteException, NotBoundException, ResponseException
    {
    	int serverResponse =  rmi.getServerObject().registrationClient(username, password);
    	
    	switch (serverResponse)
        {
            case ResponseTypes.OK:
                break;
            case ResponseTypes.INVALID_CREDENTIALS:
                throw new ResponseException("Utente esiste gia'");
            default:
                throw new ResponseException();
        }
    }
    
    
    
    /**
     * Invia una richiesta di login al Turing server.
     *
     * @throws IOException se c'e' un problema di comunicazione col server
     * @throws ResponseException se il server ha risposto che password o nome utente/password non sono corretti
     */
    public void login() throws IOException, ResponseException
    {
    	byteBufferWrite.clear();
    	byteBufferRead.clear();
 
    	byteBufferWrite.put(RequestTypes.LOGIN);
    	byteBufferWrite.put(username.getBytes());
    	byteBufferWrite.put("\n".getBytes());
    	byteBufferWrite.put(password.getBytes());
    	
    	//passo da modalita scrittura a lettura
    	byteBufferWrite.flip();
    	while(byteBufferWrite.hasRemaining())
    	{
    		socketChannel.write(byteBufferWrite);
    	}
    	
    	socketChannel.read(byteBufferRead);
    	byteBufferRead.flip();
    	int serverResponse = byteBufferRead.getInt();
    	switch (serverResponse) 
        {
            case ResponseTypes.INVALID_CREDENTIALS:
                throw new ResponseException("Credenziali invalide");
            case ResponseTypes.USER_ONLINE:
                throw new ResponseException("Utente on-line");
            case ResponseTypes.OK:
            	break;
            default:
                throw new ResponseException();
        }
    }
    
    /**
     * Invia una richiesta di logout al Turing server.
     *
     * @throws IOException se c'e' un problema di comunicazione col server
     * @throws ResponseException
     */
    public void logout() throws IOException, ResponseException
    {
    	byteBufferWrite.clear();
    	byteBufferRead.clear();
 
    	byteBufferWrite.put(RequestTypes.LOGOUT);
    	byteBufferWrite.put(username.getBytes());
    	
    	//passo da modalita scrittura a lettura
    	byteBufferWrite.flip();
    	while(byteBufferWrite.hasRemaining())
    	{
    		socketChannel.write(byteBufferWrite);
    	}
    	
    	socketChannel.read(byteBufferRead);
    	byteBufferRead.flip();
    	int serverResponse = byteBufferRead.getInt();
    	switch (serverResponse) 
        {
           case ResponseTypes.OK:
            	break;
            default:
                throw new ResponseException();
        }
    }

	
    /**
     * Invia una richiesta di creazione di un documento al Turing server.
     *
     * @param data, composta da nameFile+"\n"+section+"\n"+username
     * 
     * @throws ResponseException se il server ha risposto che un file con quel nome gia' esiste
	 * @throws IOException se c'e' un problema di comunicazione col server
     */
	public void createDocument(String data) throws IOException, ResponseException 
	{
		
		byteBufferWrite.clear();
    	byteBufferRead.clear();
 
//    	byteBufferWrite.position(4);
    	byteBufferWrite.put(RequestTypes.CREATE_DOCUMENT);
    	byteBufferWrite.put(data.getBytes());
//    	byteBufferWrite.putInt(0,byteBufferWrite.position());
    	
    	//passo da modalita scrittura a lettura
    	byteBufferWrite.flip();
    	while(byteBufferWrite.hasRemaining())
    	{
    		socketChannel.write(byteBufferWrite);
    	}
    	
    	socketChannel.read(byteBufferRead);
    	byteBufferRead.flip();
    	int serverResponse = byteBufferRead.getInt();
    	switch (serverResponse) 
        {
            case ResponseTypes.OK:
            	break;
            default:
                throw new ResponseException();
        }
		
	}
	
	 /**
     * Invia una richiesta per editare un file, e riceve il file. Si apre in automatico l'applicazione di default per i file .txt
	 *
	 *
     * @param data, composta da username+"\n"+nameFile+"\n"+section;
     * 
     * @return adress della chat multi-cast
     * 
     * @throws ResponseException se il File non esiste, se l'utente non ha i permessi o l'utente invitato non esiste
	 * @throws IOException
     */
	public String receiveFile(String data) throws IOException, ResponseException
	{
		byteBufferWrite.clear();
    	byteBufferRead.clear();
    	
    	byteBufferWrite.put(RequestTypes.START_EDIT_DOCUMENT);
    	byteBufferWrite.put(data.getBytes());
    	
    	//passo da modalita scrittura a lettura
    	byteBufferWrite.flip();
    	while(byteBufferWrite.hasRemaining())
    	{
    		socketChannel.write(byteBufferWrite);
    	}
    	
    	
		//ricevo il documento dal server, per iniziare l'edit di quest'ultimo
	    socketChannel.read(byteBufferRead);
	    byteBufferRead.flip();
	    response = byteBufferRead.getInt();
	    
	    switch (response) 
		{
			case ResponseTypes.FILE_NOT_EXIST:
				 throw new ResponseException("File non esiste");
			case ResponseTypes.PERMISSSION_DENIED:
				throw new ResponseException("non hai i permessi");
			 case ResponseTypes.ALREADY_EDITING:
	                throw new ResponseException("Un altro utente sta editando questa sezione");
			case ResponseTypes.OK:
				{
					
					byte [] bytes = new byte[4];
					bytes[0]=byteBufferRead.get();
					bytes[1]=byteBufferRead.get();
					bytes[2]=byteBufferRead.get();
					bytes[3]=byteBufferRead.get();
					
					adress = InetAddress.getByAddress(bytes).getHostAddress();
					sizeFile = byteBufferRead.getLong();
					
					File dir=new File("Turing-"+username);
				    dir.mkdir();
				    File file=new File("Turing-"+username+File.separator+"edit.txt");
				    file.createNewFile();
				    
				    
				    byteBufferRead.compact();
				    
				    FileOutputStream bout = new FileOutputStream(file);
				    FileChannel sbc = bout.getChannel();
				    if(sizeFile>0)
				    {
					    sbc.transferFrom(socketChannel, 0, sizeFile);
				    }
				    try
					{
						sbc.close();
						bout.close();
					}
					catch(IOException e1)
					{
						
					}
			   	 	java.awt.Desktop.getDesktop().edit(file);
			   	 	break;
				}
			default:
				throw new ResponseException();
		}
	    
	    return adress;
	
	}
	
	 /**
     * Invia il file editato.
     *
	 * @throws IOException
     */
	public void endEditDocument() throws IOException
	{
		
		byteBufferWrite.clear();
    	byteBufferRead.clear();
		File file=new File("Turing-"+username+File.separator+"edit.txt");
		FileInputStream fis = new FileInputStream(file);
		FileChannel channel = fis.getChannel();
		
		
		byteBufferWrite.put(RequestTypes.END_EDIT_DOCUMENT);
		byteBufferWrite.putLong(channel.size());
		byteBufferWrite.flip();
    	while(byteBufferWrite.hasRemaining())
    	{
    		socketChannel.write(byteBufferWrite);
    	}
		channel.transferTo(0, channel.size(), socketChannel);
		channel.close();
		fis.close();
		
		socketChannel.read(byteBufferRead);
    	byteBufferRead.flip();
	}
	
	 /**
     * Invia una richiesta per condividere un file con un altro utente
	 * 
     *
     * @param data, composta da username+"\n"+nameFile+"\n"+section;
     * 
     * @throws ResponseException se il File non esiste, se l'utente non ha i permessi o utente invitato non esiste
	 * @throws IOException
     */
	public void shareDocument(String data) throws ResponseException, IOException
	{
		byteBufferWrite.clear();
    	byteBufferRead.clear();
 
    	byteBufferWrite.put(RequestTypes.SHARE_DOCUMENT);
    	byteBufferWrite.put(data.getBytes());
    	
    	//passo da modalita scrittura a lettura
    	byteBufferWrite.flip();
    	while(byteBufferWrite.hasRemaining())
    	{
    		socketChannel.write(byteBufferWrite);
    	}
    	
    	socketChannel.read(byteBufferRead);
    	byteBufferRead.flip();
    	int serverResponse = byteBufferRead.getInt();
    	switch (serverResponse) 
        {
            case ResponseTypes.FILE_NOT_EXIST:
                throw new ResponseException("File non esiste");
            case ResponseTypes.PERMISSSION_DENIED:
                throw new ResponseException("Non hai i permessi");
            case ResponseTypes.USER_NOT_FOUND:
                throw new ResponseException("Utente non trovato");
            case ResponseTypes.BAD_REQUEST:
                throw new ResponseException("Richiesta non valida");
            case ResponseTypes.OK:
            	break;
            default:
                throw new ResponseException();
        }
		
	}

	/**
     * Invia una richiesta per conoscere la lista di docuementi a cui un utente puo' collaborare
	 * 
	 * 
     * @param data, composta da username
     * @return stringa contente lista dei file
     * 
	 * @throws IOException
     */
	public String listDocument() throws IOException
	{
		byteBufferWrite.clear();
    	byteBufferRead.clear();
 
    	byteBufferWrite.put(RequestTypes.LIST_DOCUMENT);
    	
    	//passo da modalita scrittura a lettura
    	byteBufferWrite.flip();
    	while(byteBufferWrite.hasRemaining())
    	{
    		socketChannel.write(byteBufferWrite);
    	}
    	
    	socketChannel.read(byteBufferRead);
    	byteBufferRead.flip();
    	byteBufferRead.getInt();
//    	int serverResponse = byteBufferRead.getInt();
    	StringBuilder builder=new StringBuilder();
		while(byteBufferRead.hasRemaining())
		{
			builder.append((char)byteBufferRead.get());
		}
		return builder.toString();
		
	}
	
	/**
     * Invia una richiesta per visuallizare una sezione di un file
	 * 
     *
     * @param nameDocument, nome del documento
	 * @param section , numero di sezione
	 * @param nomeutente, utente che invia la richiesta
     * 
     * @return responseServer, indica se qualcunos sta editando il file
     * @throws ResponseException se il File non esiste, se l'utente non ha i permessi
	 * @throws IOException
     */
	public String showSection(String nameDocument, int section) throws IOException, ResponseException 
	{
		String data=nameDocument+"\n"+section;
		String responseServer;

		
		byteBufferWrite.clear();
    	byteBufferRead.clear();
    	
    	byteBufferWrite.put(RequestTypes.SHOW_SECTION);
    	byteBufferWrite.put(data.getBytes());
    	
    	//passo da modalita scrittura a lettura
    	byteBufferWrite.flip();
    	while(byteBufferWrite.hasRemaining())
    	{
    		socketChannel.write(byteBufferWrite);
    	}
    	
    	
		//ricevo il documento dal server, per iniziare l'edit di quest'ultimo
	    socketChannel.read(byteBufferRead);
	    byteBufferRead.flip();
	    response = byteBufferRead.getInt();
	    
	    
	    switch (response) 
		{
			case ResponseTypes.FILE_NOT_EXIST:
				 throw new ResponseException("File non esiste");
			case ResponseTypes.PERMISSSION_DENIED:
				throw new ResponseException("Non hai i permessi");
			case ResponseTypes.OK:
				{
					if(byteBufferRead.getInt()==0)
					{
						responseServer = "Nessuno sta editando questa sezione";
					}
					else
					{
						responseServer = "Qualcuno sta editando questa sezione";
					}
					sizeFile = byteBufferRead.getLong();
					File dir=new File("Turing-"+username);
				    dir.mkdir();
				    File file=new File("Turing-"+username+File.separator+nameDocument+"-"+section+".txt");
				    file.createNewFile();
				    
				    byteBufferRead.compact();
				    
				    FileOutputStream bout = new FileOutputStream(file);
				    FileChannel sbc = bout.getChannel();
				    if(sizeFile>0)
				    {
					    sbc.transferFrom(socketChannel, 0, sizeFile);
				    }
				    try
					{
						sbc.close();
						bout.close();
					}
					catch(IOException e1)
					{
						
					}
			   	 	java.awt.Desktop.getDesktop().edit(file);
			   	 	break;
				}
			default:
				throw new ResponseException();
		}
		return responseServer;
	}
	
	/**
     * Invia una richiesta per visuallizare un documento
	 * 
     *
     * @param nameDocument, nome del docuemento
     * @param data, composta da username+"\n"+nameFile+"\n"+section;
     * 
     * @return responseServer, indica se qualcuno sta editando il file
     * @throws ResponseException se il File non esiste, se l'utente non ha i permessi
	 * @throws IOException
     */
	public String showDocument(String nameDocument) throws IOException, ResponseException 
	{
		String responseServer=null;

		byteBufferWrite.clear();
    	byteBufferRead.clear();
    	
    	byteBufferWrite.put(RequestTypes.SHOW_DOCUMENT);
    	byteBufferWrite.put(nameDocument.getBytes());
    	
    	//passo da modalita scrittura a lettura
    	byteBufferWrite.flip();
    	while(byteBufferWrite.hasRemaining())
    	{
    		socketChannel.write(byteBufferWrite);
    	}
    	
    	
		//ricevo il documento dal server
	    socketChannel.read(byteBufferRead);
	    byteBufferRead.flip();
	    response = byteBufferRead.getInt();
	    
	    
	    switch (response) 
		{
			case ResponseTypes.FILE_NOT_EXIST:
				 throw new ResponseException("File non esiste");
			case ResponseTypes.PERMISSSION_DENIED:
				throw new ResponseException("Non hai i permessi");
			case ResponseTypes.OK:
				{
					sizeString = byteBufferRead.getInt();
					
					if(sizeString !=0)
					{
						StringBuilder builder=new StringBuilder();
						while(sizeString>0)
						{
							builder.append((char)byteBufferRead.get());
							sizeString--;
						}
						responseServer="Sezioni editate in questo momento: "+builder.toString();
					}
					else
					{
						responseServer="Nessuno sta editando, alcuna sezione";
					}
					
					
					
					sizeFile = byteBufferRead.getLong();
					File dir=new File("Turing-"+username);
				    dir.mkdir();
				    File file=new File("Turing-"+username+File.separator+nameDocument+".txt");
				    file.createNewFile();
				    
				    byteBufferRead.compact();
				    
				    FileOutputStream bout = new FileOutputStream(file);
				    FileChannel sbc = bout.getChannel();
				    if(sizeFile>0)
				    {
					    sbc.transferFrom(socketChannel, 0, sizeFile);
				    }
				    try
					{
						sbc.close();
						bout.close();
					}
					catch(IOException e1)
					{
						
					}
			   	 	java.awt.Desktop.getDesktop().edit(file);
			   	 	break;
				}
			default:
				throw new ResponseException();
		}
		return responseServer;
	}
	
	public String getUsername() 
	{
		return username;
	}
	public TcpConnectionFactory getConnection()
	{
		return tcpConnectionFactory;
	}
}
