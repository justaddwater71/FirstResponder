package edu.nps.FirstResponder;

/* 
 * This code is heavily modeled on Alex Young's javascript
 * JSChat client found at gisthub.
 * https://gist.github.com/112520
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JSChatJavaClient
{
	public Socket 					socket;
	public BufferedReader 	inConnection;
	public PrintWriter 			outConnection;
	
	public boolean 				connected =true;
	
	public void connect(String urlString, int port)
	{
		try
		{
			Log.d(FirstResponderParameters.DEB_TAG, "Connecting to " + urlString + ":" + port);
			socket = new Socket(urlString,port);
			inConnection = new BufferedReader( new InputStreamReader( socket.getInputStream()));
			outConnection = new PrintWriter( socket.getOutputStream());
			
		} catch (UnknownHostException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void write(JSONObject json) throws IOException
	{
		Log.d(FirstResponderParameters.DEB_TAG, "Writing : " + json.toString());

		outConnection.write("\r\n");
		outConnection.flush();
		try
		{
			Thread.sleep(50);
		} catch (InterruptedException e)
		{
			//Do nothing, we're delayed long enough to continue xmit anyway
		}
	}
	
	public void identify(String user) throws JSONException, IOException
	{
		JSONObject json = new JSONObject();
		json.put("identify", new String("grady"));
		write(json);
	}
	
	public void join(String room) throws JSONException, IOException
	{
		JSONObject json = new JSONObject();
		json.put("join", room);
		write(json);
	}
	
	public void post(String message, String room) throws JSONException, IOException
	{
		JSONObject json = new JSONObject();
		json.put("to", room);
		json.put("send", message);
		write(json);
	}
	
	public void quit()
	{
		try
		{
		outConnection.close();
		inConnection.close();
		//socket.close();
		}
		catch(IOException i)
		{
			//Do nothing for now
		}
	}
	

}