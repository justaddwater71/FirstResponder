package edu.nps.FirstResponder;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;

public class StreamNotification extends Activity 
{
	//Data Members
	public static final int SOURCE_ENTERING		= 0;
	public static final int SOURCE_LEAVING			= 1;
	
	public static final String SOURCE_EVENT			= "source_event";
	
	 private NotificationManager noticeManager;
	//Constructors
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        noticeManager =(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        
        Intent callingIntent = this.getIntent();
        
        Bundle callingBundle = callingIntent.getExtras();
        
        switch(callingBundle.getInt(SOURCE_EVENT))
        {
        case SOURCE_ENTERING:
        	notifyEntering();
        	break;
        	
        case SOURCE_LEAVING:
        	notifyLeaving();
        	break;
        }
    }

    private void notifyEntering()
    {
    	
    }
    
    private void notifyLeaving()
    {
    	
    }
    
}
