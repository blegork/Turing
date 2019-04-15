package Server;

import java.nio.ByteBuffer;

import SocialTuring.User;

public class AttachChannel
{
	private static final int MAXIN = 2048;
	private static final int MAXOUT = 2048;
	private boolean state = false;
	private User utente=null;
	private static final ByteBuffer input = ByteBuffer.allocate(MAXIN);
	private static final ByteBuffer output = ByteBuffer.allocate(MAXOUT);
	
	
	public AttachChannel(boolean state)
	{
		this.state=state;
	}
	
	public synchronized void setState(boolean state)
	{
		this.state=state;
	}
	
	public synchronized boolean getState()
	{
		return this.state;
	}

	public ByteBuffer getInput() 
	{
		return input;
	}
	public ByteBuffer getOutput() 
	{
		return output;
	}

	public User getUtente() 
	{
		return utente;
	}

	public void setUtente(User utente) 
	{
		this.utente = utente;
	}
	
}
