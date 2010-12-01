package edu.nps.FirstResponder;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VideoView;
import android.widget.ViewFlipper;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TabHost.TabSpec;

public class FirstResponder extends TabActivity 
{
	//Data Members
	//Tab Tag Strings
	public static final String		LOGIN_TAB_TAG					= "loginTabSpec";
	public static final String		VIDEO_TAB_TAG					= "videoTabSpec";
	public static final String		CHAT_TAB_TAG						= "chatTabSpec";

	//Intent Request Codes
	public static final int			REQ_CODE_STREAM 			= 0;
	public static final int			REQ_CODE_OPTIONS			= 1;

	//Intent Reply Codes
	public static final int			REPLY_CODE_CANCEL 			= 0;
	public static final int 			REPLY_CODE_SET_STREAM = 1;

	//Stream Intent Extras Keys
	public static final String		 INTENT_KEY_STREAM 		= "stream";

	//Full Screen Intent Extra Keys
	public static final String		INTENT_KEY_FULLSCREEN	= "fullscreen";

	//Notification Intent keys
	public static final int			INTENT_KEY_ENTERING_NOTIFICATION 	= 2;
	public static final int			INTENT_KEY_LEAVING_NOTIFICATION 		= 3;

	//Stream filename
	public static final String		STREAM_FILE							= "stream_file.txt";

	private NotificationManager notifier;

	TabHost						myTabHost;
	boolean					tabIcons;

//Login/Option Tab Data Members

	//Login Layout
	TabSpec						loginOptionTabSpec;
	ViewFlipper				loginOptionViewFlipper;
	RelativeLayout			loginLayout;
	RelativeLayout			optionLayout;
	EditText 						username;
	EditText 						password;
	Button 						loginButton;
	Button 						optionsButton;

	//Option Layout Data Members
	ToggleButton 			showOptionChatToggleButton;
	ToggleButton 			streamSelectionTypeToggleButton;
	ToggleButton 			showChatIconsToggleButton;
	Button						optionsDoneButton;

	//Video Tab Data Members
	TabSpec						videoTabSpec;
	MediaController		mediaController;
	VideoView 				videoViewer;
	Uri 								video;
	Button						chooseAreaOfInterest;
	Button 						chooseStreamButton;
	Button						fullScreenButton;
	ToggleButton			seeChatToggleButton;
	boolean					showChatPopUps 					= false;

	//Chat Tab Data Members
	TabSpec									chatTabSpec;
	ListAdapter							chatListAdapter;
	EditText 									enterChat;
	Button 									sendChatButton;
	LinearLayout 						tabRow;
	ArrayList<Button> 				buttonArrayList 	= new ArrayList<Button>();
	ListView 								chatPostsView;
	ArrayList<ArrayAdapter<String>> chatPostsAdapterList = new ArrayList<ArrayAdapter<String>>();
	ArrayAdapter<String>		chatPostsAdapter;
	ArrayList<String>				chatGroupIDList			= new ArrayList<String>();

	//Constructors
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        mainView();
    }
    
    //Methods
    private void mainView()
    {
    	switch (this.getResources().getConfiguration().orientation)
    	{
    	case Configuration.ORIENTATION_PORTRAIT:
    		setContentView(R.layout.main);
    		tabIcons = true;
    	  break;
    	case Configuration.ORIENTATION_LANDSCAPE:
    		setContentView(R.layout.horizontal_main);
    		tabIcons = false;
    	  break;
    	case Configuration.ORIENTATION_SQUARE:
    		setContentView(R.layout.main);
    		tabIcons = true;
    	  break;
    	default:
    		setContentView(R.layout.main);
    		tabIcons = true;
    	  break;
    	}
		//setContentView(R.layout.main);

		//Create the tabs at the top of our "home view"
    	
    	notifier = (NotificationManager) getSystemService ( Context.NOTIFICATION_SERVICE );
    	
    	
    	
		myTabHost = getTabHost();

		createLoginOptionTab();

		createVideoTab();

		createChatTab();

		//Set Initial Active Tab
		myTabHost.setCurrentTabByTag(LOGIN_TAB_TAG);
    }
  
    //******************** Tab View Setup Methods **************************************
    
	//**************************** LOGIN/OPTION TAB ************************************
    public void createLoginOptionTab()
    {


		//Set up login tab (multiple TabSpec objects not strictly necessary, but makes code clearer...
		loginOptionTabSpec = myTabHost.newTabSpec(LOGIN_TAB_TAG);
		if (tabIcons)
		{
			loginOptionTabSpec.setIndicator("Login", getResources().getDrawable(R.drawable.login));
		}
		else
		{
			loginOptionTabSpec.setIndicator("Login");
		}
		loginOptionTabSpec.setContent(R.id.login_option_flipper);
		myTabHost.addTab(loginOptionTabSpec);

		loginOptionViewFlipper	= (ViewFlipper)findViewById(R.id.login_option_flipper);

		//*************************** LOGIN VIEW ***************************************
		//Create items to display in option/login tab
		loginLayout 						= (RelativeLayout)findViewById(R.id.login_table);

		//Make username and password text entry fields
		username 	= (EditText)findViewById(R.id.username_edit_text);
		password	= (EditText)findViewById(R.id.password_edit_text);

		//Create login button
		loginButton	= (Button)findViewById(R.id.login_button);
		loginButton.setOnClickListener(onLoginButtonClicked);

		//Create options button
		optionsButton = (Button)findViewById(R.id.options_button);
		optionsButton.setOnClickListener(onOptionsButtonClicked);

		//******************** OPTION LAYOUT **************************
		optionLayout					= (RelativeLayout)findViewById(R.id.option_table);
		showOptionChatToggleButton 			= (ToggleButton)findViewById(R.id.show_chat_option_toggle);
		showOptionChatToggleButton.setOnClickListener(onShowOptionChatToggleButtonClicked);
		streamSelectionTypeToggleButton 	= (ToggleButton)findViewById(R.id.show_stream_option_toggle);
		streamSelectionTypeToggleButton.setOnClickListener(onStreamSelectionTypeToggleButtonClicked);
		showChatIconsToggleButton				= (ToggleButton)findViewById(R.id.show_icon_option_toggle);
		showChatIconsToggleButton.setOnClickListener(onShowChatIconsToggleButtonClicked);
		optionsDoneButton								= (Button)findViewById(R.id.options_done_button);
		optionsDoneButton.setOnClickListener(onOptionsDoneButtonClicked);
    }
    
    private void createVideoTab()
    {
    	//********************* VIDEO TAB *******************************
		//Set up video viewer tab
		videoTabSpec = myTabHost.newTabSpec(VIDEO_TAB_TAG);
		if (tabIcons)
		{
			videoTabSpec.setIndicator("Video", getResources().getDrawable(R.drawable.video_camera));
		}
		else
		{
			videoTabSpec.setIndicator("Video");
		}
		videoTabSpec.setContent(R.id.video_viewer);
		myTabHost.addTab(videoTabSpec);

		//Create items to display in video viewer tab

		//Create view viewer and accompanying media controller
		videoViewer	= (VideoView)findViewById(R.id.video_app);
        mediaController = new MediaController(this);
        mediaController.setAnchorView(videoViewer);
        
        //Create area of interest button
        chooseAreaOfInterest = (Button)findViewById(R.id.aoi_button);
        chooseAreaOfInterest.setOnClickListener(onChooseAOIButtonClicked);
        
        //Create choose stream button
		chooseStreamButton = (Button)findViewById(R.id.choose_stream);
		chooseStreamButton.setOnClickListener(onChooseStreamButtonClicked);

		fullScreenButton = (Button)findViewById(R.id.full_screen);
		fullScreenButton.setOnClickListener(onFullScreenButtonClicked);

		//Create see chat pop ups toggle button with cool green light
		seeChatToggleButton = (ToggleButton)findViewById(R.id.see_chat);
		seeChatToggleButton.setChecked(true);
		seeChatToggleButton.setOnClickListener(onSeeChatToggleButtonClicked);
		seeChatToggleButton.setOnClickListener(onSeeChatToggleButtonClicked);
    }
    
    private void createChatTab()
    {
    	 //******************** CHAT TAB **********************************
		//Set up chat tab
		chatTabSpec = myTabHost.newTabSpec(CHAT_TAB_TAG);
		if (tabIcons)
		{
			chatTabSpec.setIndicator("Chat", getResources().getDrawable(R.drawable.speechbubble));
		}
		else
		{
			chatTabSpec.setIndicator("Chat");
		}
		chatTabSpec.setContent(R.id.chat_viewer);
		myTabHost.addTab(chatTabSpec);

		//Create the addChatGroupButton that is ALWAYS display under chat tab and
		//launches add/remove chat group method
		Button addChatGroupButton = (Button)findViewById(R.id.add_chat_group);

		addChatGroupButton.setBackgroundResource(R.drawable.timepicker_up_normal);

		//Test setup of "
		Button 								localButton;

		tabRow = (LinearLayout)findViewById(R.id.tab_row);

		chatPostsView = (ListView)findViewById(R.id.show_chat);

		for (int i = 0; i < 3; i++)
		{
			localButton = makeGenericButton(i, Integer.toString(i), tabRow.getContext());
			buttonArrayList.add(localButton);

			//can see all the buttons all the time, but you don't add below b/c you see ONE listview at a time!!!
			tabRow.addView(localButton);

			chatPostsAdapterList.add( new ArrayAdapter<String>(chatPostsView.getContext(), R.layout.chat_row, new ArrayList<String>()));

			for (int j = 0; j < 40; j++)
			{
				chatPostsAdapterList.get(chatPostsAdapterList.size() - 1).add(Integer.toString(i + j));
			}
		}


		chatPostsView.setAdapter(chatPostsAdapterList.get(0));
		buttonArrayList.get(0).setBackgroundResource(android.R.color.background_light);

		//Create field to enter chats to send
		enterChat = (EditText)findViewById(R.id.enter_chat);

		//Create send caht button
		sendChatButton = (Button)findViewById(R.id.send_chat);
		sendChatButton.setOnClickListener(onSendChatButtonClicked);
    }
    
    //******************** FUNCTIONAL METHODS *****************************************  
    //Hook for actual JSON getStreams() method.  Currently hard coded for testing purposes
/*	public static ArrayList<String> getStreams()
	{
		ArrayList<String> streams = null;
		
		//For testing purposes only.......................................
		//Set up a few dummy strings....
		streams = new ArrayList<String>();
		
    	streams.add( "/sdcard/test2.3gp");
    	streams.add("rtsp://v3.cache6.c.youtube.com/CjgLENy73wIaLwkc2NKc3hvZQBMYESARFEIJbXYtZ29vZ2xlSARSB3JlbGF0ZWRgrOuMlcOelOVMDA==/0/0/0/video.3gp");
    	streams.add( "rtsp://v3.cache4.c.youtube.com/CjYLENy73wIaLQn-5SAo5qVUFhMYESARFEIJbXYtZ29vZ2xlSARSBXdhdGNoYKzrjJXDnpTlTAw=/0/0/0/video.3gp");
    	streams.add("/sdcard/CDR-Dinner_LAN_800k.mp4");
    	streams.add("rtsp://192.168.13.133:8000/stream.sdp");
		//End for testing purposes only......................................
		return streams;
	}*/

	/* 
	 * Method to launch Stream Chooser view that allows stream to be chosen and stream
	 * value returned to FirstResponder
	*/
    
    private void createNotification(String ticker, String title, String text, int intentID)
    {
    	Notification notification = new Notification(R.drawable.ic_notification_overlay, ticker, System.currentTimeMillis());
    	Intent notificationIntent = new Intent(this, this.getClass());
    	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
    	
    	/*long[] vibTimes = {0, 100, 200, 300};
    	enteringNotification.vibrate = vibTimes;*/
    	
    	notification.defaults 	= Notification.DEFAULT_ALL;
    	notification.flags  		= Notification.FLAG_AUTO_CANCEL;
    	notification.setLatestEventInfo(getApplicationContext(), title, text, contentIntent);
    	
    	notifier.notify(intentID, notification);
    }
    
	private void sendToStreamChooser()
	{
		//Build Intent, Bundle for the Intent, and Extras to go inside Bundle
		Intent		startStreamChooserIntent		= new Intent(FirstResponder.this, StreamChooser.class);

		Bundle	startStreamChooserBundle	= new Bundle();

		startStreamChooserIntent.putExtras(startStreamChooserBundle);

		//Tell stream chooser to use Request Code Stream to identify type of return intent
		startActivityForResult(startStreamChooserIntent, REQ_CODE_STREAM);
	}
	
	private void sendToMapActivity()
	{
		Intent	startMapActivityIntent		= new Intent(this, MapsActivity.class);
		
		Bundle	startMapActivityBundle = new Bundle();
		
		startMapActivityIntent.putExtras(startMapActivityBundle);
		
		startActivity(startMapActivityIntent);
		
		//put bundle stuff here........
	}

	private void sendToFullScreen()
	{
		//Build Intent, Bundle for the Intent, and Extras to go inside Bundle
		Intent		startFullScreenIntent		= new Intent(FirstResponder.this, FullScreenVideo.class);

		Bundle	startFullScreenBundle	= new Bundle();

		startFullScreenBundle.putString(INTENT_KEY_FULLSCREEN, video.toString());
		//startFullScreenBundle.putString( INTENT_KEY_FULLSCREEN, "/sdcard/test2.3gp" );

		startFullScreenIntent.putExtras(startFullScreenBundle);

		//Tell stream chooser to use Request Code Stream to identify type of return intent
		startActivity(startFullScreenIntent);
	}

	public void receiveChat(String chatGroup, String chatString)
	{
		for (int i=0; i < chatGroupIDList.size(); i++)
		{
			if (chatGroup.equalsIgnoreCase(chatGroupIDList.get(i)))
			{
				chatPostsAdapterList.get(i).add(chatString);
				//Highlight button-tab of chat window just updated so the user knows a new chat is there
				//Button button = buttonArrayList.get(i);
				//button.setBackgroundDrawable(android.R.drawable.btn_star);

				//IF the user wants chat popups and is NOT already looking at the chat tab.....
				if (showChatPopUps && myTabHost.getCurrentTabTag() != CHAT_TAB_TAG)
				{
					sendChatToast(chatGroup, chatString);
				}
			}
		}
	}

	public void sendChatToast(String chatGroup, String chatString)
	{
		Toast toast = Toast.makeText(getApplicationContext(), chatGroup + ": " + chatString, Toast.LENGTH_LONG);
		toast.show();
	}

	public void sendChat(String chatString)
	{

		//FOR TESTING PURPOSES ONLY...............................
		/*chatArrayList.add(chatString);
		enterChat.setText("");
		showChatListView.setAdapter(chatListAdapter);*/
	}

	//This method gets invoked by the actual chat code to update the chat ListView and possibly video pop-ups
/*	public void receiveChat(String chatString)
	{
		//IF seeChatPopUp is selected AND we're not in the chat tab, show chat posts as Toast
		if (showChatPopUps && ! myTabHost.getCurrentTabTag().equalsIgnoreCase(CHAT_TAB_TAG))
		{
			Toast.makeText(this.getBaseContext(), chatString, Toast.LENGTH_LONG);
		}
	}*/

	private Button makeGenericButton(int index, String title,  Context context )
	{
		Button button = new Button(context);

		LayoutParams buttonLayout =  new LayoutParams( android.view.ViewGroup.LayoutParams.FILL_PARENT, 
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				1);
		buttonLayout.setMargins(0, 1, 3, 0);

		button.setLayoutParams( buttonLayout );
		button.setBackgroundResource(android.R.color.darker_gray);

		button.setId(index);
		button.setText(title);

		button.setOnClickListener(buttonOnClickListener);

		return button;

	}

	public void login(String username, String password)
	{
		//FIXME put actual guts of login here or call to real login class/method
	}

	//Get a new stream name and restart the video viewer
	public void setStream(String streamName)
	{
        // Set video link (mp4 format )
		video = Uri.parse(streamName);
        videoViewer.setMediaController(mediaController);
        videoViewer.setVideoURI(video);
        videoViewer.start();
	}

	public void testLoadChatListView()
	{
		/*chatArrayList.add("one");
		chatArrayList.add("two");
		chatArrayList.add("three");*/
	}

	// *********************************** VIDEO TAB LISTENERS *******************************************
    private Button.OnClickListener onChooseStreamButtonClicked = new Button.OnClickListener()
    {

		public void onClick(View v) 
		{
			sendToStreamChooser();
		}
    	
    };
    
    private Button.OnClickListener onChooseAOIButtonClicked = new Button.OnClickListener()
    {

		@Override
		public void onClick(View v)
		{
			//start Map Activity
			sendToMapActivity();
			
			
		}
    	
    };
    
    private Button.OnClickListener onSeeChatToggleButtonClicked = new Button.OnClickListener()
    {

		public void onClick(View v) 
		{
			if (seeChatToggleButton.isChecked())
			{
				showChatPopUps = true;
				showOptionChatToggleButton.setChecked(true);
			}
			else
			{
				showChatPopUps = false;
				showOptionChatToggleButton.setChecked(false);
			}
		}
    	
    };
    
    private Button.OnClickListener onFullScreenButtonClicked = new Button.OnClickListener()
    {

		public void onClick(View v) 
		{
			if (video == null)
			{
				Toast toast = Toast.makeText(getApplicationContext(), "No video stream selected", Toast.LENGTH_LONG);
				toast.show();
			}
			else
			{
				sendToFullScreen();
			}
		}
    	
    };
    
 // *********************************** CHAT TAB LISTENERS *******************************************
    private Button.OnClickListener onSendChatButtonClicked = new Button.OnClickListener()
    {

		public void onClick(View v) 
		{
			sendChat(enterChat.getText().toString());
			hideKeyboard(v);
			enterChat.setText("");
		}
    	
    };
    
    private Button.OnClickListener buttonOnClickListener = new Button.OnClickListener()
	{

		@Override
		public void onClick(View v)
		{
			int index = v.getId();

			chatPostsView.setAdapter(chatPostsAdapterList.get(index));

			Iterator<Button> iterator = buttonArrayList.iterator();

			while (iterator.hasNext())
			{
				iterator.next().setBackgroundResource(android.R.color.darker_gray);
			}

			v.setBackgroundResource(android.R.color.background_light);

		}

	};

// *********************************LOGIN/OPTION TAB LISTENERS**************************************
    
 // *********************************** LOGIN LAYOUT LISTENERS **************************************
    private Button.OnClickListener onLoginButtonClicked = new Button.OnClickListener()
    {

		public void onClick(View view) 
		{
			String usernameString = username.getText().toString();
			String passwordString = password.getText().toString();
			hideKeyboard(view);

			login(usernameString, passwordString);

			username.setText("");
			password.setText("");
		}
    	
    };
    
    private Button.OnClickListener onOptionsButtonClicked = new Button.OnClickListener()
    {

		public void onClick(View v) 
		{
			loginOptionViewFlipper.showNext();
		}
    	
    };
    
    // ****************** OPTION LAYOUT LISTENERS ********************************
    private Button.OnClickListener onShowOptionChatToggleButtonClicked = new Button.OnClickListener()
    {

		public void onClick(View v) 
		{
			if (showOptionChatToggleButton.isChecked())
			{
				showChatPopUps = true;
				seeChatToggleButton.setChecked(true);
			}
			else
			{
				showChatPopUps = false;
				seeChatToggleButton.setChecked(false);
			}
		}
    	
    };
    
    private Button.OnClickListener onStreamSelectionTypeToggleButtonClicked = new Button.OnClickListener()
    {

		public void onClick(View v) 
		{
			//Do grid vs list thing here.
		}
    	
    };
    
    private Button.OnClickListener onShowChatIconsToggleButtonClicked = new Button.OnClickListener()
    {

		@Override
		public void onClick(View v) 
		{
			createNotification("Test ticker", "Test Title", "Test text", INTENT_KEY_ENTERING_NOTIFICATION);	
		}
    	
    };
    
    private Button.OnClickListener onOptionsDoneButtonClicked = new Button.OnClickListener()
    {

		@Override
		public void onClick(View v) 
		{
			loginOptionViewFlipper.showPrevious();

		}
    	
    };
    
    
 // ******************* ACTIVITY MANAGEMENT METHODS ***********************
	private void hideKeyboard(View view)
	{

		 //Below hide-keyboard code copied from
		 //http://stackoverflow.com/questions/1109022/how-to-close-hide-the-android-soft-keyboard

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
        //End copy from stackoverflow
	}

/*    private ListAdapter makeListAdapter(View v, int rowContext, ArrayList arrayList)
    {
    	ListAdapter listAdapter = new ArrayAdapter<Adapter>(v.getContext(), rowContext, arrayList);
    	
    	return listAdapter;
    }*/

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode == REPLY_CODE_CANCEL)
		{
			//Do nothing, use for all cancel replies from any finishing View/Activity started from an Inent sent by FirstResponder
		}
		else
		{
			//Get the string values replied from the spawned View/Activity
			Bundle result = data.getExtras();

			switch (requestCode)
			{
			//Stream chooser is the View returning a value
			case REQ_CODE_STREAM:

					setStream(result.getString(INTENT_KEY_STREAM));

					break;

			//Options is the View returning a value
			case REQ_CODE_OPTIONS:

				switch (resultCode)
				{
					//Put actions from options reply (if any) here
				}
				break;
			}
		}
	}
}