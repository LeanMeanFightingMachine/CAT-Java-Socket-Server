package uk.co.lmfm.cat.server.net;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Vector;

import uk.co.lmfm.cat.server.SocketApplication;

public class CommunicationServer extends Thread
{
	protected ServerSocket socketServer;
	protected int port;
	protected boolean listening;
	public Vector<CommunicationServerConnection> clientConnections;

	public CommunicationServer(int serverPort)
	{
		this.port = serverPort;
		this.clientConnections = new Vector<CommunicationServerConnection>();
		this.listening = false;
	}

	public int getPort()
	{
		return this.port;
	}

	public int getClientCount()
	{
		return this.clientConnections.size();
	}

	public void recieved()
	{

	}

	protected void debug(String msg)
	{
		SocketApplication.debug("ChatServer (" + this.port + ")", msg);
	}

	public void run()
	{
		try
		{
			this.socketServer = new ServerSocket(this.port);
			this.listening = true;
			debug("listening");

			while (listening)
			{
				Socket socket = this.socketServer.accept();
				debug("client connection from " + socket.getRemoteSocketAddress());
				CommunicationServerConnection socketConnection = new CommunicationServerConnection(socket, this);
				socketConnection.start();
				this.clientConnections.add(socketConnection);
				
				SocketApplication.connected(socketConnection);
			}
			;
		}
		catch (Exception e)
		{
			debug(e.getMessage());
		}
	}

	public void writeToAll(String msg)
	{
		try
		{
			for (int i = 0; i < this.clientConnections.size(); i++)
			{
				CommunicationServerConnection client = this.clientConnections.get(i);
				client.write(msg);
			}
			debug("broadcast message '" + msg + "' was sent");
		}
		catch (Exception e)
		{
			debug("Exception (writeToAll): " + e.getMessage());
		}
	}

	public boolean remove(SocketAddress remoteAddress)
	{
		try
		{
			for (int i = 0; i < this.clientConnections.size(); i++)
			{
				CommunicationServerConnection client = this.clientConnections.get(i);

				if (client.getRemoteAddress().equals(remoteAddress))
				{
					this.clientConnections.remove(i);
					debug("client " + remoteAddress + " was removed");
					writeToAll(remoteAddress + " has disconnected.");

					return true;
				}
			}
		}
		catch (Exception e)
		{
			debug("Exception (remove): " + e.getMessage());
		}

		return false;
	}

	protected void finalize()
	{
		try
		{
			this.socketServer.close();
			this.listening = false;
			debug("stopped");
		}
		catch (Exception e)
		{
			debug("Exception (finalize): " + e.getMessage());
		}
	}
}
