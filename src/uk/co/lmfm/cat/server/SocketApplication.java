package uk.co.lmfm.cat.server;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;

import uk.co.lmfm.cat.server.net.CommunicationServer;
import uk.co.lmfm.cat.server.net.CommunicationServerConnection;
import uk.co.lmfm.cat.server.net.PolicyServer;

import com.google.gson.Gson;

public class SocketApplication
{
	public static final boolean DEBUG = true;
	public static final int UPDATE = 100;
	public static final int NUMBER_MAGNETS = 10;
	public static final int WIDTH = 200;
	public static final int HEIGHT = 200;
	
	public static CommunicationServer communicationServer;
	public static PolicyServer policyServer;

	private static ArrayList<StateItem> _state = new ArrayList<StateItem>();
	private static Boolean _invalidate = false;

	private Timer _timer = new Timer();

	public SocketApplication()
	{
		_timer = new Timer();
		_timer.schedule(new BroadcastTask(), 0, UPDATE);

		setState();
	}

	public static void debug(String label, String msg)
	{
		System.out.println(label + " : " + msg);
	}

	public static void broadcast()
	{
		if(_invalidate)
		{
			for (int i = 0; i < SocketApplication.communicationServer.getClientCount(); i++)
			{
				CommunicationServerConnection client = SocketApplication.communicationServer.clientConnections.get(i);
				send(client);
			}
			
			_invalidate = false;
		}
	}
	
	public static void send(CommunicationServerConnection client)
	{
		Gson gson = new Gson();
		String response = gson.toJson(new DataItem(new HeaderItem(client.socket.getPort()),_state));
		
		client.write(response);
	}

	public static void recieved(String data)
	{
		Gson gson = new Gson();
		Message message = gson.fromJson(data, Message.class);
		
		int port = message.port;
		StateItem changed = message.data;

		if (changed != null)
		{
			ListIterator<StateItem> itr = _state.listIterator();
			StateItem item;

			while (itr.hasNext())
			{
				item = itr.next();
				if(item.id == changed.id)
				{
					if(item.locked < 0 || port == item.locked)
					{
						itr.set(changed);
						_invalidate = true;
					}
					break;
				}
			}
		}
	}

	public static void connected(CommunicationServerConnection client)
	{
		send(client);
	}

	public static void main(String[] args)
	{
		try
		{
			int communicationPort = 5555;
			int policyPort = communicationPort + 1;

			for (int i = 0; i < args.length; i++)
			{
				if (i == 0)
				{
					communicationPort = Integer.parseInt(args[i]);
				}

				if (i == 1)
				{
					policyPort = Integer.parseInt(args[i]);
				}
			}

			PolicyServer policyServer = new PolicyServer(policyPort);
			policyServer.start();

			CommunicationServer communicationServer = new CommunicationServer(communicationPort);
			communicationServer.start();

			SocketApplication.communicationServer = communicationServer;
			SocketApplication.policyServer = policyServer;

			new SocketApplication();
		}
		catch (Exception e)
		{
			debug("Main", "Exception (main)" + e.getMessage());
		}
	}

	private void setState()
	{
		for (int i = 0; i < NUMBER_MAGNETS; i++)
		{
			_state.add(new StateItem(i, (int) (Math.random() * WIDTH), (int) (Math.random() * HEIGHT)));
		}
	}

	class BroadcastTask extends TimerTask
	{
		public void run()
		{
			SocketApplication.broadcast();
		}
	}
	
	static class Message
	{
		public int port;
		public StateItem data;
		
		public Message()
		{
			
		}
	}
	
	static class DataItem
	{
		public ArrayList<StateItem> state;
		public HeaderItem header;
		
		public DataItem()
		{
			
		}
		
		public DataItem(HeaderItem header, ArrayList<StateItem> state)
		{
			this.header = header;
			this.state = state;
		}
	}
	
	static class HeaderItem
	{
		public int port;
		public int w = WIDTH;
		public int h = HEIGHT;
		
		public HeaderItem()
		{
			
		}
		
		public HeaderItem(int port)
		{
			this.port = port;
		}
	}

	static class StateItem
	{
		public int id;
		public int x;
		public int y;
		public int locked = -1;

		public StateItem()
		{
			
		}

		public StateItem(int id, int x, int y, int locked)
		{
			this.id = id;
			this.x = x;
			this.y = y;
			this.locked = locked;
		}

		public StateItem(int id, int x, int y)
		{
			this.id = id;
			this.x = x;
			this.y = y;
		}
	}
}
