package uk.co.lmfm.cat.server;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.Gson;

public class Main
{
	public static final boolean DEBUG = true;
	public static final int UPDATE = 100;
	public static CommunicationServer communicationServer;
	public static PolicyServer policyServer;

	private static ArrayList<StateItem> _state = new ArrayList<StateItem>();
	private static Boolean _invalidate = false;

	private Timer _timer = new Timer();

	public Main()
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
			Main.communicationServer.writeToAll(jsonState());
			_invalidate = false;
		}
	}

	public static void recieved(String data)
	{
		Gson gson = new Gson();
		StateItem changed = gson.fromJson(data, StateItem.class);

		if (changed != null)
		{
			ListIterator<StateItem> itr = _state.listIterator();
			StateItem item;

			while (itr.hasNext())
			{
				item = itr.next();
				if(item.id == changed.id)
				{
					itr.set(changed);
					_invalidate = true;
					break;
				}
			}
		}
	}

	public static void connected(CommunicationServerConnection connection)
	{
		connection.write(jsonState());
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

			Main.communicationServer = communicationServer;
			Main.policyServer = policyServer;

			new Main();
		}
		catch (Exception e)
		{
			debug("Main", "Exception (main)" + e.getMessage());
		}
	}
	
	private static String jsonState()
	{
		Gson gson = new Gson();
		String response = gson.toJson(_state);
		return response;
	}

	private void setState()
	{
		for (int i = 0; i < 10; i++)
		{
			_state.add(new StateItem(i, (int) (Math.random() * 200), (int) (Math.random() * 200)));
		}
	}

	class BroadcastTask extends TimerTask
	{
		public void run()
		{
			Main.broadcast();
		}
	}

	static class StateItem
	{
		public int id;
		public int x;
		public int y;
		public Boolean locked = false;

		public StateItem()
		{

		}

		public StateItem(int id, int x, int y, Boolean locked)
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
