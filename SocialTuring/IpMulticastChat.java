package SocialTuring;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class IpMulticastChat 
{
	private int ip4=1;
	private int ip3=0;
	private List<byte[]> IP= new ArrayList<byte[]>();
	private byte[] bytes;
	private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	
	//224.0.0.0.0
	//239.255.255.255
	public byte [] createNewIp() throws UnknownHostException
	{
		try
		{
			readWriteLock.readLock().lock();
			if(IP.isEmpty())
			{
				if(ip4==255)
				{
					if(ip3<=255)
					{
						ip4=0;
						ip3++;
					}
					else
					{
						return null;
					}
				}
				else
				{
					ip4++;
				}
				bytes = InetAddress.getByName("224.0."+ip3+"."+ip4).getAddress();
			}
			else
			{
				bytes=IP.remove(0);
				
			}
				
				return 	bytes;
		}
		finally 
		{
			readWriteLock.readLock().unlock();
		}
	}
	
	public void addIp(byte[] ip)
	{
		readWriteLock.writeLock().lock();
		IP.add(ip);
		readWriteLock.writeLock().unlock();
	}
}
