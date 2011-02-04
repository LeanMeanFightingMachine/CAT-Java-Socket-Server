package uk.co.lmfm.cat.server.net;

import java.util.TimerTask;

import javax.swing.JLabel;

public class UpdateClientCountTask extends TimerTask
{
	protected JLabel clientsLabel;
	protected CommunicationServer chatServer;
	
	public UpdateClientCountTask(CommunicationServer chatServer,JLabel clientsLabel)
	{
		this.chatServer = chatServer;
		this.clientsLabel = clientsLabel;
	}
	
	public void run()
	{
		int count = this.chatServer.getClientCount();
		String msg = count + " client" + ((count != 1) ? "s" : "");
		this.clientsLabel.setText(msg);
	}
}