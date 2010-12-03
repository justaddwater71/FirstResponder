package edu.nps.FirstResponder;

import java.io.BufferedReader;
import java.io.IOException;

import android.util.Log;

public class JSChatJavaListener implements Runnable// extends Thread
{
	JSChatJavaClient client;
	BufferedReader bufferedReader;
	String line = "";
	
	public JSChatJavaListener(BufferedReader bufferedReader)
	{
		//this.client = client;
		this.bufferedReader = bufferedReader;
		//listen();
	}
	
	
	
	public void run() {
		// TODO Auto-generated method stub
		listen();
	}



	public void listen()
	{
		/*while(client.connected)
		{
			System.out.println(client.read());
		}*/
		
		while (true)
		{
			Log.d(FirstResponderParameters.DEB_TAG, "JSChat Thread Start");
			try
			{
				if ((line = bufferedReader.readLine()) != null)
				{
					System.out.println(line);
					Log.d(FirstResponderParameters.DEB_TAG, line);
				}
			} catch (IOException e)
			{
				System.out.println("IO ERROR!!");
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
