package edu.nps.FirstResponder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class JSChatClientService extends Service
{
	public Socket socket;
	public BufferedReader inConnection;
	public PrintWriter outConnection;
	public LocalBinder binder;

	public boolean connected = false;

	@Override
	public void onCreate()
	{
		super.onCreate();
		binder = new LocalBinder();
	}

	@Override
	public IBinder onBind(Intent arg0)
	{
		// Close to Mark Murphy's
		// "The Busy Coder's Guide to Android Programming"
		return binder;
	}

	// From Mark Murphy's "The Busy Coder's Guide to Android Programming
	public class LocalBinder extends Binder
	{
		public JSChatClientService getService()
		{
			return JSChatClientService.this;
		}
	}

	public void connect(String urlString, int port, String username)
	{
		try
		{
			Log.d(FirstResponderParameters.DEB_TAG, "Connecting to "
					+ urlString + ":" + port);
			socket = new Socket(urlString, port);
			inConnection = new BufferedReader(new InputStreamReader(socket
					.getInputStream()));
			outConnection = new PrintWriter(socket.getOutputStream());

			connected = true;

			Runnable thread = new Runnable()
			{
				public void run()
				{
					while (connected)
					{
						listen();
					}
				}
			};

			new Thread(thread).start();

			try
			{
				identify(username);
			} catch (JSONException e)
			{
				Log.e("JSON", "Unable to identify to chat server", e);
				// FIXME Put error INTENT here to let user know the server
				// address is bad.....
			}

		} catch (UnknownHostException e)
		{
			Log.e("HOST", "Unable to connect to chat server", e);
		} catch (IOException e)
		{
			connected = false;
			Log
					.e(
							"IO",
							"Got IO Exception from Socket need to try to reconnect.",
							e);
			// FIXME Put intent here that let's user know we're broken for the
			// moment.....
		}

	}

	public void write(JSONObject json) throws IOException
	{
		Log.d(FirstResponderParameters.DEB_TAG, "Writing : " + json.toString());
		outConnection.write(json.toString());
		outConnection.write("\r\n");
		outConnection.flush();
		try
		{
			Thread.sleep(50);
		} catch (InterruptedException e)
		{
			// Do nothing, we're delayed long enough to continue xmit anyway
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

	public void post(String message, String room) throws JSONException,
			IOException
	{
		JSONObject json = new JSONObject();
		json.put("to", room);
		json.put("send", message);
		write(json);
	}

	public void part(String room) throws JSONException, IOException
	{
		JSONObject json = new JSONObject();
		json.put("part", room);
		write(json);
	}

	public void quit()
	{
		try
		{
			outConnection.close();
			inConnection.close();
			// socket.close();
		} catch (IOException i)
		{
			// Do nothing for now
		}
	}

	public void listen()
	{
		Intent intent;
		String line;

		while (true)
		{
			Log.d(FirstResponderParameters.DEB_TAG, "JSChat Thread Start");
			try
			{
				if ((line = inConnection.readLine()) != null)
				{
					Log.d(FirstResponderParameters.DEB_TAG, line);
					intent = new Intent(
							FirstResponder.INTENT_ACTION_CHAT_MESSAGE);
					intent.putExtra(FirstResponder.INTENT_KEY_CHAT_MESSAGE,
							line);
					sendBroadcast(intent);

				}
			} catch (IOException e)
			{
				System.out.println("IO ERROR!!");
			}
			try
			{
				Thread.sleep(10);
			} catch (InterruptedException e)
			{
				Log.e("CHAT_LISTENER", "Couldn't sleep", e);
			}
		}
	}
}
