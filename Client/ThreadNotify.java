package Client;

import java.util.concurrent.BlockingQueue;
import javax.swing.JOptionPane;


public class ThreadNotify implements Runnable
{
	private BlockingQueue<String> queue;
	private String notify;
	public ThreadNotify(BlockingQueue<String> queue)
	{
		this.queue=queue;
	}
	@Override
	public void run() 
	{
		// TODO Auto-generated method stub
		while(true)
		{
			try 
			{
				notify=queue.take();
				JOptionPane.showMessageDialog(null, notify);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
			
		}
	}

}
