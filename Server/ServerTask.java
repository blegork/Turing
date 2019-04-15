package Server;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;

import SocialTuring.Document;
import SocialTuring.User;

/**
 * Un ServerTask e' una componente del server che legge e risponde alle richieste di un singolo client.
 */
public class ServerTask implements Runnable
{
	
	
	private Server server;
	private SelectionKey key;
	private SocketChannel client;
	private AttachChannel attachKey;
	private ByteBuffer byteBufferInput;
	private ByteBuffer byteBufferOutput;
	private String file;
	private String pathFile;
	private User u;
    public ServerTask(Server server, SelectionKey key)
    {
        this.server = server;
        this.key = key;
    }
    
	@Override
	public void run() 
	{
		try 
		{
			
			client = (SocketChannel) key.channel();
			attachKey = (AttachChannel)key.attachment();
			byteBufferInput=attachKey.getInput();
			byteBufferOutput = attachKey.getOutput();
			byteBufferInput.flip();
			
			u = attachKey.getUtente();
			
			int action = byteBufferInput.get();
			switch (action) 
			{
				case RequestTypes.LOGIN:
					login();
						break;
				case RequestTypes.LOGOUT:
					logout();
						break;
				case RequestTypes.CREATE_DOCUMENT:
					createDocument();
					break;
				case RequestTypes.SHARE_DOCUMENT:
					shareDocument();
					break;
				case RequestTypes.START_EDIT_DOCUMENT:
					startEditDocument();
					break;
				case RequestTypes.END_EDIT_DOCUMENT:
					endEditDocument();
					break;
				case RequestTypes.SHOW_DOCUMENT:
					showDocument();
					break;
				case RequestTypes.SHOW_SECTION:
					showSection();
					break;
				case RequestTypes.LIST_DOCUMENT:
					listDocument();
					break;
				default:
						break;
			}
		}
		catch (IOException e)
		{
			
		}
		finally 
	    {}
		
	}


	/**
	 * Gestisce una richiesta di login. Si aspetta di ricevere ,
	 * nome + '\n' + password. Risponde con INVALID_CREDENTIALS o OK o USER_ONLINE.
	 *
	 * @throws IOException
	 */
	private void login() throws IOException 
	{
		StringBuilder builder=new StringBuilder();
		while(byteBufferInput.hasRemaining())
		{
			builder.append((char)byteBufferInput.get());
		}
		String fileName=builder.toString();
		String[] login = new String(fileName).split("\n", 2);

		
		u = Server.usersNetwork.getUser(login[0]);
		if (u == null || !u.getPassword().equals(login[1]))
		{
			sendQuickResponse(ResponseTypes.INVALID_CREDENTIALS);
		}
		else
		{
			if(u.isOnline())
			{
				sendQuickResponse(ResponseTypes.USER_ONLINE);
			}
			else
			{
				attachKey.setUtente(u);
				u.setOnline(true);
				sendQuickResponse(ResponseTypes.OK);
			    
				List<String> notify=u.getPendentNotify();
				server.log("[LOG]: l'utente "+u.getUsername()+" ha effettuato l'accesso");
				for(int i=0;i<notify.size();i++)
				{
					String notifica = notify.get(i);	
					u.getClientCallback().notifyShare(notifica);
				}
				
			}
		}
    	attachKey.setState(false);
	}
	
	/**
	 * Gestisce una richiesta di logout. Si aspetta di ricevere ,
	 * nome. Risponde con o OK.
	 *
	 * @throws IOException
	 */
	private void logout() throws IOException
	{
		StringBuilder builder=new StringBuilder();
		while(byteBufferInput.hasRemaining())
		{
			builder.append((char)byteBufferInput.get());
		}
		String user=builder.toString();
		Server.usersNetwork.getUser(user).setOnline(false);
		sendQuickResponse(ResponseTypes.OK);
		attachKey.setState(false);
		server.log("[LOG]: l'utente "+u.getUsername()+" ha effettuato il logout");
	}
	
	
	/**
     * Gestisce una creazione documento. Si aspetta di ricevere, Nome_Docuemento+ '\n' + Numero_Sezioni
     * + '\n' + Nome_Utente. Risponde con OK o con FILE_ALREADY_EXISTS.
     *
     * @throws IOException
     */
	private void createDocument() throws IOException
	{
		StringBuilder builder=new StringBuilder();
		while(byteBufferInput.hasRemaining()) 
		{
			builder.append((char)byteBufferInput.get());
		}
		String fileName=builder.toString();
		String[] data = new String(fileName).split("\n", 2);
		
		
		pathFile=Server.theDir.getAbsolutePath()+File.separator+data[0];
		
		File dir = new File(pathFile);
		if(!dir.exists())
		{
			Server.documentNetwork.addDocument(u.getUsername(), pathFile, data[0], Integer.parseInt(data[1]));
			u.addDocument(Server.documentNetwork.getDocument(pathFile));
			
			dir.mkdir();
			File dir2;
			for(int i=0;i<Integer.parseInt(data[1]);i++)
			{
				dir2= new File(dir+File.separator+(i+1)+".txt");
				dir2.createNewFile();
			}
			server.log("[LOG]: l'utente "+u.getUsername()+" ha creato un documento: "+data[0]);
			sendQuickResponse(ResponseTypes.OK);
		}
		else
		{
			sendQuickResponse(ResponseTypes.FILE_ALREADY_EXISTS);
		}
    	attachKey.setState(false);
	}
	
	
	
	
	/**
     * Gestisce l'invio di un documento. Si aspetta di ricevere, Nome_Utente+ '\n' + Nome_File
     * + '\n' + Numero_Sezione. Risponde con OK e invia il file o con NOT_PERMISSION o con ALREADY_EDITING.
     *
     * @throws IOException
     * @see {@link #sendDocument(byte, String)}
     */
	private void startEditDocument() throws IOException
	{
		StringBuilder builder=new StringBuilder();
		while(byteBufferInput.hasRemaining())
		{
			builder.append((char)byteBufferInput.get());
		}
		String fileName=builder.toString();
		String[] data = new String(fileName).split("\n", 2);
		
		byteBufferInput.clear();
		byteBufferOutput.clear();
		
		
		file=Server.theDir.getAbsolutePath()+File.separator+data[0]+File.separator+data[1]+".txt";
		String path=Server.theDir.getAbsolutePath()+File.separator+data[0];
		
		Document document = Server.documentNetwork.getDocument(path);
		
		File f = new File(file);
		if(!f.exists())
		{
			sendQuickResponse(ResponseTypes.FILE_NOT_EXIST);
		}
		else
		{
//			//l'utente identificato con data[0] ha i permessi, e' autoree del file o colloboratore
			if(document.isAutore(u.getUsername()) || document.isCollaboratore(u.getUsername()))
			{
				if(!Server.editDocument.containsValue(file))
				{
					Server.editDocument.put(u.getUsername(), file);
					document.addEditSection(data[1]+".txt");
					server.log("[LOG]: l'utente "+u.getUsername()+" sta editando la sezione "+data[1]+" del file "+data[0]);
					sendDocument(ResponseTypes.OK, file, path);
				}
				else
				{
					sendQuickResponse(ResponseTypes.ALREADY_EDITING);
				}
			}
			else
			{
				sendQuickResponse(ResponseTypes.PERMISSSION_DENIED);
			}	
		}
		attachKey.setState(false);
	 }	
	

	
	/**
     * Gestisce la ricezione di un documento. Si aspetta di ricevere, SizeFile, sizeName e Nome_Utente
     * Risponde con OK.
     *
     * @throws IOException
     */
	private void endEditDocument() throws IOException
	{
		Long sizeFile = byteBufferInput.getLong();
		
		File file=new File(Server.editDocument.remove(u.getUsername()));
		
		Server.documentNetwork.getDocument(file.getParentFile().toString()).removeEditSection(file.getName());
		
		if(Server.documentNetwork.getDocument(file.getParentFile().toString()).removeEditor())
		{
			Server.ipMulticast.addIp(Server.documentNetwork.getDocument(file.getParentFile().toString()).getUDPMulticast());
			Server.documentNetwork.getDocument(file.getParentFile().toString()).setUDPMulticast(null);
		}
		
	    file.createNewFile();
	    long bytesRead=byteBufferInput.limit()-byteBufferInput.position();
	    
	    FileOutputStream bout = new FileOutputStream(file);
	    FileChannel sbc = bout.getChannel();
	    
	    if(sizeFile>0)
	    {
		    sbc.write(byteBufferInput);
		    while(bytesRead<sizeFile)
		    {
		    	byteBufferInput.clear();
		    	bytesRead+=client.read(byteBufferInput);
		    	byteBufferInput.flip();
		        sbc.write(byteBufferInput);
		    	
		    }
	    }
	    sbc.close();
		bout.close();
		server.log("[LOG]: l'utente "+u.getUsername()+" ha finito di editare");
		sendQuickResponse(ResponseTypes.OK);
		attachKey.setState(false);
		
	}
	
	
	/**
     * Gestisce l'invio della lista di documenti che un utente puo' editare.
     * Si aspetta di ricevere, Nome_Utente
     *Risponde con OK e invia la lista di documenti.
     *
     * @throws IOException
     * @see {@link #sendList(byte, String)}
     */
	private void listDocument() throws IOException
	{
		String listString=new String();
		
		List<Document> listFile = u.getDocuments();
		
		for(int i =0; i<listFile.size();i++)
		{
		
			listString+="Nome file: "+listFile.get(i).getNome()+"\n";
			listString+="Nome autore:"+listFile.get(i).getAutore()+"\n";
			listString+="Numero sezioni: "+listFile.get(i).getSection()+"\n";
			listString+="Collaboratori: "+listFile.get(i).getCollaboratori()+"\n\n";
			
		}
		server.log("[LOG]: l'utente "+u.getUsername()+" ha richiesto la lista documenti");
		sendList(ResponseTypes.OK,listString);
		attachKey.setState(false);
	}
	
	
	
	/**
    * Gestisce la condivisione di un documento. Si aspetta di ricevere, Nome_Utente+ '\n' + Nome_File
   	* Risponde con OK o con NOT_PERMISSION o con USER_NOT_FOUND.
	* @throws IOException 
    * 
    */
	private void shareDocument() throws IOException
	{
		StringBuilder builder=new StringBuilder();
		while(byteBufferInput.hasRemaining())
		{
			builder.append((char)byteBufferInput.get());
		}
		String fileName=builder.toString();
		String[] data = new String(fileName).split("\n", 2);
		
		//data[0]=> file
		//data[1]=> utente da inviatare
		
		
		String path=Server.theDir.getAbsolutePath()+File.separator+data[0];
		File f = new File(path);
		User uInvitato = Server.usersNetwork.getUser(data[1]);
		Document document = Server.documentNetwork.getDocument(path);
		//il file esiste?
		if(!f.exists())
		{
			sendQuickResponse(ResponseTypes.FILE_NOT_EXIST);
		}
		else
		{
			//l'utente e' il creatore?
			if(document.isAutore(u.getUsername()))
			{
				//l'utente invitato esiste?
				if(uInvitato==null)
				{
					sendQuickResponse(ResponseTypes.USER_NOT_FOUND);
				}
				else
				{
					//l'utente invitato e' diverso dall'autore e non e' gia' presente tra i collaboratori?
					if(document.isAutore(data[1]) || document.isCollaboratore(data[1]))
					{
						sendQuickResponse(ResponseTypes.BAD_REQUEST);
					}
					else
					{
						sendQuickResponse(ResponseTypes.OK);
						server.log("[LOG]: l'utente "+u.getUsername()+" ha condiviso il documento "+data[1]+"con l'utente "+uInvitato.getUsername());
						document.addCollaboratori(data[1]);
						uInvitato.addDocument(document);
						
						//l'utente e' on-line?
						if(uInvitato.isOnline())
						{
							
							uInvitato.getClientCallback().notifyShare("L'utente: "+u.getUsername()+", ha condiviso con te il file: "+data[1]);
						}
						else
						{
							uInvitato.addPendentNotify("L'utente: "+u.getUsername()+", ha condiviso con te il file: "+data[0]);
						}
					}
				}
			}
			else
			{
				sendQuickResponse(ResponseTypes.PERMISSSION_DENIED);
			}	
		
		}
		attachKey.setState(false);
	}
	
	
	
	/**
	    * Gestisce la richiesta di visione di una sezione, relativa a un file. 
	    * Si aspetta di ricevere, NameFile+ '\n' + NumeroSezione
	   	* Risponde con OK o con PERMISSSION_DENIED o con FILE_NOT_EXIST.
		* @throws IOException 
	    * 
	    */
	private void showSection() throws IOException 
	{
		
		StringBuilder builder=new StringBuilder();
		while(byteBufferInput.hasRemaining())
		{
			builder.append((char)byteBufferInput.get());
		}
		String fileName=builder.toString();
		String[] data = new String(fileName).split("\n", 2);
		
		byteBufferInput.clear();
		byteBufferOutput.clear();
		
		
		//data[0] Nome_File
		//data[1] Numero_Sezione
		
		
		file=Server.theDir.getAbsolutePath()+File.separator+data[0]+File.separator+data[1]+".txt";
		String path=Server.theDir.getAbsolutePath()+File.separator+data[0];
		Document document = Server.documentNetwork.getDocument(path);
		
		File f = new File(file);
		if(!f.exists())
		{
			sendQuickResponse(ResponseTypes.FILE_NOT_EXIST);
		}
		else
		{
//			l'utente ha i permessi, e' autoree del file o colloboratore
			if(document.isAutore(u.getUsername()) || document.isCollaboratore(u.getUsername()))
			{
				sendShowSection(ResponseTypes.OK, file, path);
				server.log("[LOG]: l'utente "+u.getUsername()+" ha richiesto di visualizzare una sezione");
			}
			else
			{
				sendQuickResponse(ResponseTypes.PERMISSSION_DENIED);
			}	
		}
		attachKey.setState(false);
	}

	
	
	/**
	 *  Gestisce la richiesta di visione di un documento. 
	 *  Si aspetta di ricevere, NameFile
	 *  Risponde con OK o con PERMISSSION_DENIED o con FILE_NOT_EXIST..
	 *  @throws IOException 
	 * 
	 */
	private void showDocument() throws IOException
	{
		StringBuilder builder=new StringBuilder();
		while(byteBufferInput.hasRemaining())
		{
			builder.append((char)byteBufferInput.get());
		}
		String fileName=builder.toString();
		
		byteBufferInput.clear();
		byteBufferOutput.clear();
		
		
		String path=Server.theDir.getAbsolutePath()+File.separator+fileName;
		Document document = Server.documentNetwork.getDocument(path);
		
		File f = new File(path);
		if(!f.exists())
		{
			sendQuickResponse(ResponseTypes.FILE_NOT_EXIST);
		}
		else
		{
//			//l'utente identificato con data[0] ha i permessi, e' autoree del file o colloboratore
			if(document.isAutore(u.getUsername()) ||document.isCollaboratore(u.getUsername()))
			{
				sendShowDocument(ResponseTypes.OK, path);
				server.log("[LOG]: l'utente "+u.getUsername()+" ha richiesto di visualizzare una documento");
			}
			else
			{
				sendQuickResponse(ResponseTypes.PERMISSSION_DENIED);
			}	
		}
		attachKey.setState(false);
	}

	
	
	
	private void sendQuickResponse(byte response) throws IOException
	{
		byteBufferOutput.clear();
		byteBufferInput.clear();
		byteBufferOutput.putInt(response);
		byteBufferOutput.flip();
    	while(byteBufferOutput.hasRemaining())
    	{
    		client.write(byteBufferOutput);
    	}
	}
	
	
	
	private void sendShowSection(byte response, String file, String path) throws IOException
	{
		
		byteBufferOutput.clear();
		byteBufferInput.clear();
		
		FileInputStream fis = new FileInputStream(file);
		FileChannel channel = fis.getChannel();
		
		
		byteBufferOutput.putInt(response);
		if(Server.editDocument.containsValue(file))
		{
			byteBufferOutput.putInt(1);
		}
		else
		{
			byteBufferOutput.putInt(0);
		}
		
		byteBufferOutput.putLong(channel.size());
		
		
		byteBufferOutput.flip();
    	while(byteBufferOutput.hasRemaining())
    	{
    		client.write(byteBufferOutput);
    	}
		channel.transferTo(0, channel.size(), client);
		channel.close();
		fis.close();
	}
	
	private void sendShowDocument(byte response, String path) throws IOException
	{
		File theDir = new File(path);
		String[]entries = theDir.list();
		byteBufferOutput.clear();
		byteBufferInput.clear();
		
		File tmp = new File(path+File.separator+"temp.txt");
		tmp.createNewFile();
		
		OutputStream out = new FileOutputStream(tmp);
		
	    byte[] buf = new byte[1024];
		for(int i =0; i<entries.length;i++)
		{
		    File currentFile = new File(theDir.getPath(),entries[i]);
		    FileInputStream in = new FileInputStream(currentFile);
		    
	        int b = 0;
	        while ( (b = in.read(buf)) >= 0) 
	        {
	            out.write(buf, 0, b);
	            out.flush();
	        }
	        in.close();
		}
		out.close();
		
		
		FileInputStream fis = new FileInputStream(tmp);
		FileChannel channel = fis.getChannel();
		
		String sezioniEditate= Server.documentNetwork.getDocument(path).getEditSection();
		byteBufferOutput.putInt(response);
		byteBufferOutput.putInt(sezioniEditate.length());
		byteBufferOutput.put(sezioniEditate.getBytes());
		
		byteBufferOutput.putLong(channel.size());
		
		
		byteBufferOutput.flip();
    	while(byteBufferOutput.hasRemaining())
    	{
    		client.write(byteBufferOutput);
    	}
		channel.transferTo(0, channel.size(), client);
		channel.close();
		fis.close();
		tmp.delete();
	}
	
	
	
	
	private void sendDocument(byte response, String file, String path) throws IOException
	{
		Server.documentNetwork.getDocument(path).addEditor();
		
		byteBufferOutput.clear();
		byteBufferInput.clear();
		
		FileInputStream fis = new FileInputStream(file);
		FileChannel channel = fis.getChannel();
		
		
		byteBufferOutput.putInt(response);
		
		
		
		
		byte[] bytes = Server.documentNetwork.getDocument(path).getUDPMulticast();
		if(bytes==null)
		{
			bytes=Server.ipMulticast.createNewIp();
			byteBufferOutput.put(bytes);
			Server.documentNetwork.getDocument(path).setUDPMulticast(bytes);
		}
		else
		{
			byteBufferOutput.put(bytes);
		}
		byteBufferOutput.putLong(channel.size());
		
		
		byteBufferOutput.flip();
    	while(byteBufferOutput.hasRemaining())
    	{
    		client.write(byteBufferOutput);
    	}
		channel.transferTo(0, channel.size(), client);
		channel.close();
		fis.close();
	}
	
	
	
	
	private void sendList(byte response, String list) throws IOException
	{
		byteBufferOutput.clear();
		byteBufferInput.clear();
		
		byteBufferOutput.putInt(response);
		byteBufferOutput.put(list.getBytes());
		byteBufferOutput.flip();
    	while(byteBufferOutput.hasRemaining())
    	{
    		client.write(byteBufferOutput);
    	}
		
	}
}