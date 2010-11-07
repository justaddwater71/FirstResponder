package edu.nps.FirstResponder;

import android.app.Activity;
import android.content.Intent;
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
    	}
    }
}
