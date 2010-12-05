package edu.nps.FirstResponder;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

public class FullScreenVideo extends Activity 
{
	String	videoURIString;
	Uri 		video;
	ChatReceiver chatReceiver;
	IntentFilter			chatIntentFilter;
	
	 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent callingIntent = this.getIntent();
        
        try
        {
        	Bundle callingBundle = callingIntent.getExtras();
        
        	videoURIString = callingBundle.getString(FirstResponder.INTENT_KEY_FULLSCREEN);
        	video = Uri.parse(videoURIString);
        }
        catch (NullPointerException n)
        {
        	video = null;
        }
        
        chatReceiver = new ChatReceiver();
		chatIntentFilter = new IntentFilter(FirstResponder.INTENT_ACTION_CHAT_MESSAGE);
		registerReceiver(chatReceiver, chatIntentFilter);
        
        mainView();
    }
    
    private void mainView()
    {
    	setContentView(R.layout.full_screen_video);
    	
    	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    	VideoView fullScreenVideo = (VideoView)findViewById(R.id.full_screen_video_view);
    	
    	MediaController fullScreenMedia = new MediaController(this);
    	fullScreenMedia.setAnchorView(fullScreenVideo);
    	
    	fullScreenVideo.setMediaController(fullScreenMedia);
    	
    	if ( video == null )
    	{
    		Toast toast = Toast.makeText(fullScreenVideo.getContext(), "No video stream selected.", Toast.LENGTH_LONG);
    		toast.show();
    	}
    	else
    	{
	    	fullScreenVideo.setVideoURI(video);
	    	
	    	fullScreenVideo.start();
	    	
	    	fullScreenVideo.setBackgroundColor(R.color.edit_text_background);
    	}
    }
    
	public class ChatReceiver extends BroadcastReceiver
	{

		public void onReceive(Context context, Intent intent)
		{
			Bundle bundle = intent.getExtras();

			// String room = bundle.getString(INTENT_KEY_CHAT_ROOM);
			String message = bundle.getString(FirstResponder.INTENT_KEY_CHAT_MESSAGE);

			processChatMessage(message);

		}

	}
	
	public void processChatMessage(String chatLine)
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

			Toast.makeText(this.getBaseContext(), "Room: " + room + "\n" + user + " says: " + message, Toast.LENGTH_SHORT);
		}
		catch (JSONException e)
		{
			//Do nothing, it's just not that critical.....
		}
	}
}
