package uk.co.lmfm.cat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server
{
	private static final int PORT = 8888;
	
	public static void main(String[] args)
	{
		ServerSocket serverSocket = null;
		Socket socket = null;
		DataInputStream dataInputStream = null;
		DataOutputStream dataOutputStream = null;

		System.out.println("SERVER : init");

		try
		{
			serverSocket = new ServerSocket(PORT);
			System.out.println("SERVER : socket created");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		while (true)
		{
			try
			{
				if(socket == null)
				{
					System.out.println("SERVER : connect");
				}
				
				socket = serverSocket.accept();
				dataInputStream = new DataInputStream(socket.getInputStream());
				dataOutputStream = new DataOutputStream(socket.getOutputStream());
				dataOutputStream.writeUTF("Server says 'Hello'");
				
				System.out.println("SERVER : client ip address " + socket.getInetAddress());
				System.out.println("SERVER : client message " + dataInputStream.readUTF());
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally
			{
				if (socket != null)
				{
					try
					{
						socket.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}

				if (dataInputStream != null)
				{
					try
					{
						dataInputStream.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}

				if (dataOutputStream != null)
				{
					try
					{
						dataOutputStream.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}
}