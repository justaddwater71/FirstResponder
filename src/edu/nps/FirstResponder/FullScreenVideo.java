package edu.nps.FirstResponder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

public class FullScreenVideo extends Activity
{
	//Menu Constants
	private final static int CHAT_POP_UPS	= 0;
	private static final int SEND_CHAT		= 1;
	private static final int EXIT					= 2;
	
	String videoURIString;
	Uri video;
	ChatReceiver chatReceiver;
	IntentFilter chatIntentFilter;
	VideoView fullScreenVideo;
	RelativeLayout videoWrapper;
	boolean chatPopUps = true;
	JSChatClientService jsChatClientService;
	ArrayList<String> chatGroups;
	String destinationChatGroup = "";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		//get chat connection up so the user can chat while waiting for the video to spool
		bindService(new Intent(this, JSChatClientService.class), onService,
				BIND_AUTO_CREATE);
		
		Intent callingIntent = this.getIntent();

		//Putting this here soley to get a Toast to show up in front of the MediaPlayer
		videoWrapper = (RelativeLayout)findViewById(R.id.video_wrapper);
		
		Bundle callingBundle = callingIntent.getExtras();
		
		try
		{
			videoURIString = callingBundle
					.getString(FirstResponder.INTENT_KEY_FULLSCREEN);
			video = Uri.parse(videoURIString);
		} catch (NullPointerException n)
		{
			video = null;
		}

		chatReceiver = new ChatReceiver();
		chatIntentFilter = new IntentFilter(
				FirstResponder.INTENT_ACTION_CHAT_MESSAGE);
		registerReceiver(chatReceiver, chatIntentFilter);
		
		//Get list of chatgroups/rooms that user is currently using in chat
		//This will let user select list of rooms to chat to from fullscreenvideo
		chatGroups = callingBundle.getStringArrayList(FirstResponder.INTENT_KEY_CHAT_ARRAY);

		mainView();
	}

	private void mainView()
	{
		setContentView(R.layout.full_screen_video);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		fullScreenVideo = (VideoView) findViewById(R.id.full_screen_video_view);

		MediaController fullScreenMedia = new MediaController(this);
		fullScreenMedia.setAnchorView(fullScreenVideo);

		fullScreenVideo.setMediaController(fullScreenMedia);

		if (video == null)
		{
			Toast toast = Toast.makeText(getBaseContext(),
					"No video stream selected.", Toast.LENGTH_LONG);
			toast.show();
		} else
		{
			fullScreenVideo.setVideoURI(video);

			fullScreenVideo.start();

			fullScreenVideo.setBackgroundColor(R.color.edit_text_background);
		}
	}
	
	//create connection to chat server
	private ServiceConnection onService = new ServiceConnection()
	{
		@Override
		public void onServiceConnected(ComponentName className,
				IBinder rawBinder)
		{
			jsChatClientService = ((JSChatClientService.LocalBinder) rawBinder)
					.getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName className)
		{
			jsChatClientService = null;
		}
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, CHAT_POP_UPS, 0, R.string.menu_chat_pop_ups);
		menu.add(0, SEND_CHAT, 0, R.string.menu_send_chat);
		menu.add(0, EXIT, 0, R.string.menu_exit);
		return result;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case CHAT_POP_UPS:
				toggleChatPopUps();
				return true;
			case SEND_CHAT:
				grabAndSendChat();
				return true;
			case EXIT:
				finish();
				return true;
		}
	
		return super.onOptionsItemSelected(item);
	}

	public void toggleChatPopUps()
	{
		if (chatPopUps)
		{
			chatPopUps	=	false;
			broadcastChatPopUpStatus();
			Toast.makeText(this, "Chat Pop-Ups are now off.", Toast.LENGTH_LONG);
		}
		else
		{
			chatPopUps	=	true;
			broadcastChatPopUpStatus();
			Toast.makeText(this, "Chat Pop-Ups are now on.", Toast.LENGTH_LONG);
		}
	}
	
	public void broadcastChatPopUpStatus()
	{
		//If chat pop-ups get implemented for video view in FirstResponder activity, then build intent here
		//and listener there.
	}

	public void getChatGroup()
	{
		AlertDialog.Builder getGroup = new AlertDialog.Builder(this);

		// final Context alertContext = v.getContext();

		getGroup.setTitle("Select Room");
		//getGroup.setMessage("Select Room");

		//It really pains me to load a dummy arraylist for this, but putting in the arraylist at
		//initialization isn't working -- the ArrayAdapter thinks it is empty.... argh!
		//ArrayList<String> tempArrayList = new ArrayList<String>();
		
		final ArrayAdapter<String> chatGroupArrayAdapter = new ArrayAdapter<String>(
				this, R.layout.chat_group_display_row, chatGroups);
		
		/*Iterator<String> iterator = chatGroups.iterator();
		while (iterator.hasNext())
		{
			chatGroupArrayAdapter.add(iterator.next());
		}*/
		
		getGroup.setAdapter(chatGroupArrayAdapter, new OnClickListener()
		{
			//User clicks a row
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				destinationChatGroup =  chatGroupArrayAdapter.getItem(which);
				getMessage();
			}
		});
		
		getGroup.setPositiveButton("Cancel", new OnClickListener()
		{

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				//Do nothing....
				
			}
			
		});
		
		getGroup.show();
	}
	
	public void getMessage()
	{
		AlertDialog.Builder getMessage = new AlertDialog.Builder(this);
		
		final EditText chatMessage = new EditText(this);

		chatMessage.setInputType(InputType.TYPE_CLASS_TEXT);

		getMessage.setView(chatMessage);
		
		getMessage.setTitle("Enter Chat Message");
		
		getMessage.setPositiveButton("Send", new OnClickListener()
		{

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				sendChat(chatMessage.getText().toString(), 
						destinationChatGroup);
			}
		});

		getMessage.setNegativeButton("Cancel", new OnClickListener()
		{

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				// Do nothing.....

			}

		});

		// alert.setCancelable(true);

		getMessage.show();
	}
	
	public void grabAndSendChat()
	{
		//Get the chat group (use a top level variable, I know bad, but we're moving quick....)
		getChatGroup();
		
		//We have the chatgroup, now lets get the message(use a top level variable, I know bad, but we're moving quick....)
		//getMessage();
	}
		
	
	public void sendChat(String chatString, String room)
	{
		try
		{
			jsChatClientService.post(chatString, room);
		} 
		catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			Toast.makeText(this, "Connection to chat server appears down",
					Toast.LENGTH_LONG).show();
		}
	}
	
	public class ChatReceiver extends BroadcastReceiver
	{

		public void onReceive(Context context, Intent intent)
		{
			Bundle bundle = intent.getExtras();

			// String room = bundle.getString(INTENT_KEY_CHAT_ROOM);
			String message = bundle
					.getString(FirstResponder.INTENT_KEY_CHAT_MESSAGE);

			processChatMessage(message);

		}

	}

	public void processChatMessage(String chatLine)
	{
		if (chatPopUps)
		{
			JSONObject json;
			try
			{
				json = new JSONObject(chatLine);
	
				String time = json.getString("time");
	
				JSONObject jsonInner = new JSONObject(json.getString("message"));
	
				String user = jsonInner.getString("user");
				String room = jsonInner.getString("room");
				String message = jsonInner.getString("message");
	
				Toast.makeText(this, "Room: " + room + "\n " + user
						+ ": " + message, Toast.LENGTH_SHORT).show();
				
			} catch (JSONException e)
			{
				// Do nothing, it's just not that critical.....
			}
		}
		else
		{
			//Do nothing
		}
	}
}
