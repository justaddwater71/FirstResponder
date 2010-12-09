package edu.nps.FirstResponder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VideoView;
import android.widget.ViewFlipper;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TabHost.TabSpec;

public class FirstResponder extends TabActivity
{
	// Data Members
	// Tab Tag Strings
	public static final String LOGIN_TAB_TAG = "loginTabSpec";
	public static final String VIDEO_TAB_TAG = "videoTabSpec";
	public static final String CHAT_TAB_TAG = "chatTabSpec";

	// Intent Request Codes
	public static final int REQ_CODE_STREAM = 0;
	public static final int REQ_CODE_OPTIONS = 1;

	// Intent Reply Codes
	public static final int REPLY_CODE_CANCEL = 0;
	public static final int REPLY_CODE_SET_STREAM = 1;

	// Stream Intent Extras Keys
	public static final String INTENT_KEY_STREAM = "stream";

	// Full Screen Intent Extra Keys
	public static final String INTENT_KEY_FULLSCREEN = "fullscreen";
	public static final String INTENT_KEY_CHAT_ARRAY = "chat_array";

	// Notification Intent keys
	public static final int INTENT_KEY_ENTERING_NOTIFICATION = 2;
	public static final int INTENT_KEY_LEAVING_NOTIFICATION = 3;

	// StreamChooser Intent ACTION
	public static final String INTENT_ACTION_GEOUPDATE = "geo_update_action";
	public static final String INTENT_KEY_GEOUPDATE = "geo_update_action";

	// MapView Intent ACTION
	public static final String INTENT_ACTION_AOI = "aoi_update";

	// MapVIew Intent keys
	public static final String INTENT_KEY_AOILIST = "aoi_list";

	// Chat Intent ACTION
	public static final String INTENT_ACTION_CHAT_MESSAGE = "message_action";

	// Chat Intent keys
	public static final String INTENT_KEY_CHAT_MESSAGE = "message_key";
	public static final String INTENT_KEY_CHAT_ROOM = "room_key";
	public static final String INTENT_ACTION_LOGIN_SUCCESSFUL = "login_sucessful";

	// Chat constants
	public static final String CHAT_SERVER_URL = "192.168.1.104";

	// Menu Constants
	public static final int MENU_SETTINGS = 0;
	public static final int MENU_EXIT = 1;
	public static final int MENU_TEST_NOTIFIER = 2;

	// Data Members
	private NotificationManager notifier;

	TabHost myTabHost;
	boolean tabIcons;
	Button currentChatGroup;

	HashMap<String, Boolean> lastStreamAlert = new HashMap<String, Boolean>();
	int enterNotificationID = 0;
	int leaveNotificationID = 1;

	// Login/Option Tab Data Members

	// Login Layout
	TabSpec loginOptionTabSpec;
	/*
	 * ViewFlipper loginOptionViewFlipper; RelativeLayout loginLayout;
	 * RelativeLayout optionLayout; EditText username; EditText password; Button
	 * loginButton; Button optionsButton;
	 */

	// Option Layout Data Members
	/*
	 * ToggleButton showOptionChatToggleButton; ToggleButton
	 * streamSelectionTypeToggleButton; ToggleButton showChatIconsToggleButton;
	 * Button optionsDoneButton;
	 */

	// Video Tab Data Members
	TabSpec videoTabSpec;
	ViewFlipper videoChooserViewFlipper;
	LinearLayout streamChooserLayout;
	RelativeLayout videoLayout;
	MediaController mediaController;
	VideoView videoViewer;
	Uri video;
	Button chooseAreaOfInterest;
	Button chooseStreamButton;
	Button fullScreenButton;
	// ToggleButton seeChatToggleButton;
	boolean showChatPopUps = false;
	ArrayList<Double> aoiDoubles = new ArrayList<Double>();

	// Stream Choose (under Video Tab) Members
	ListView streamListView;
	EditText enterStream;

	/*
	 * DialogInterface.OnClickListener atRootButtonListener = new
	 * DialogInterface.OnClickListener() { //@Override public void
	 * onClick(DialogInterface arg0, int arg1) { //Do nothing } };
	 */

	Button setStream;
	Button cancelSetStream;
	ArrayList<String> streamLabels = new ArrayList<String>();
	ArrayList<String> streamFQDN = new ArrayList<String>();
	ArrayAdapter<String> streamListAdapter;

	// Chat Tab Data Members
	TabSpec chatTabSpec;
	// ListAdapter chatListAdapter;
	EditText enterChat;
	Button sendChatButton;
	LinearLayout tabRow;
	ArrayList<Button> buttonArrayList = new ArrayList<Button>();
	ListView chatPostsView;
	ArrayList<ArrayAdapter<String>> chatPostsAdapterList = new ArrayList<ArrayAdapter<String>>();
	ArrayAdapter<String> chatPostsAdapter;
	ArrayList<String> chatGroupIDList = new ArrayList<String>();
	JSChatClientService jsChatClientService;

	// BroadcastReceiver for streams variables
	StreamReceiver streamReceiver;
	IntentFilter streamIntentFilter;

	// BroadcastReceiver for AOI variables
	AOIReceiver aoiReceiver;
	IntentFilter aoiIntentFilter;

	// ChatReceiver to get chats into chat application or as pop-ups elsewhere
	ChatReceiver chatReceiver;
	IntentFilter chatIntentFilter;

	// LoginReceiver (sucessful anyway) to get JSChatJavaClient going upon
	// sucessful login
	LoginReceiver loginReceiver;
	IntentFilter loginIntentFilter;

	// Preferences
	SharedPreferences prefs;

	// Constructors
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		bindService(new Intent(this, JSChatClientService.class), onService,
				BIND_AUTO_CREATE);
		
		mainView();

		notifier = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		// You're looking for a JSChatJavaClient here, but it is in the
		// successful logon realm.....

		streamReceiver = new StreamReceiver();
		streamIntentFilter = new IntentFilter(INTENT_ACTION_GEOUPDATE);
		registerReceiver(streamReceiver, streamIntentFilter);

		aoiReceiver = new AOIReceiver();
		aoiIntentFilter = new IntentFilter(INTENT_ACTION_AOI);
		registerReceiver(aoiReceiver, aoiIntentFilter);

		chatReceiver = new ChatReceiver();
		chatIntentFilter = new IntentFilter(INTENT_ACTION_CHAT_MESSAGE);
		registerReceiver(chatReceiver, chatIntentFilter);

		loginReceiver = new LoginReceiver();
		loginIntentFilter = new IntentFilter(INTENT_ACTION_LOGIN_SUCCESSFUL);
		registerReceiver(loginReceiver, loginIntentFilter);

		

		prefs.registerOnSharedPreferenceChangeListener(prefListener);
	}

	private SharedPreferences.OnSharedPreferenceChangeListener prefListener = new SharedPreferences.OnSharedPreferenceChangeListener()
	{
		public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key)
		{
			Resources res = getResources();
			if (key.equals(res.getString(R.string.shaft_server_key)))
			{
				//FIXME change where we get the feeds and update locations
			}
			else if (key.equals(res.getString(R.string.chat_server_key)))
			{
				loginToChatServer();
			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_SETTINGS, 0, R.string.menu_settings);
		menu.add(0, MENU_TEST_NOTIFIER, 0, R.string.menu_test_notifier);
		menu.add(0, MENU_EXIT, 0, R.string.menu_exit);
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case MENU_SETTINGS:
				startActivity(new Intent(this, ShaftPreferenceActivity.class));
				return true;
			case MENU_TEST_NOTIFIER:
				toggleTestNotifier();
				return true;
			case MENU_EXIT:
				finish();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

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

	// Methods
	private void mainView()
	{
		switch (this.getResources().getConfiguration().orientation)
		{
			/*
			 * case Configuration.ORIENTATION_PORTRAIT:
			 * setContentView(R.layout.main); tabIcons = true; break; case
			 * Configuration.ORIENTATION_LANDSCAPE:
			 * setContentView(R.layout.horizontal_main); tabIcons = false;
			 * break; case Configuration.ORIENTATION_SQUARE:
			 * setContentView(R.layout.main); tabIcons = true; break;
			 */
			default:
				setContentView(R.layout.main);
				tabIcons = true;
				break;
		}
		// setContentView(R.layout.main);

		// Create the tabs at the top of our "home view"

		myTabHost = getTabHost();

		createLoginOptionTab();

		createVideoTab();

		createChatTab();

		// Set Initial Active Tab
		myTabHost.setCurrentTabByTag(LOGIN_TAB_TAG);
	}

	// ******************** Tab View Setup Methods
	// **************************************

	// **************************** LOGIN/OPTION TAB
	// ************************************
	public void createLoginOptionTab()
	{

		// Set up login tab (multiple TabSpec objects not strictly necessary,
		// but makes code clearer...
		loginOptionTabSpec = myTabHost.newTabSpec(LOGIN_TAB_TAG);
		if (tabIcons)
		{
			loginOptionTabSpec.setIndicator("Login", getResources()
					.getDrawable(R.drawable.login));
		} else
		{
			loginOptionTabSpec.setIndicator("Login");
		}
		loginOptionTabSpec.setContent(new Intent(this, LoginActivity.class));
		myTabHost.addTab(loginOptionTabSpec);

		/*
		 * loginOptionViewFlipper =
		 * (ViewFlipper)findViewById(R.id.login_option_flipper);
		 * 
		 * //*************************** LOGIN VIEW
		 * *************************************** //Create items to display in
		 * option/login tab loginLayout =
		 * (RelativeLayout)findViewById(R.id.login_table);
		 * 
		 * //Make username and password text entry fields username =
		 * (EditText)findViewById(R.id.username_edit_text); password =
		 * (EditText)findViewById(R.id.password_edit_text);
		 * 
		 * //Create login button loginButton =
		 * (Button)findViewById(R.id.login_button);
		 * loginButton.setOnClickListener(onLoginButtonClicked);
		 * 
		 * //Create options button optionsButton =
		 * (Button)findViewById(R.id.options_button);
		 * optionsButton.setOnClickListener(onOptionsButtonClicked);
		 * 
		 * //******************** OPTION LAYOUT **************************
		 * optionLayout = (RelativeLayout)findViewById(R.id.option_table);
		 * showOptionChatToggleButton =
		 * (ToggleButton)findViewById(R.id.show_chat_option_toggle);
		 * showOptionChatToggleButton
		 * .setOnClickListener(onShowOptionChatToggleButtonClicked);
		 * streamSelectionTypeToggleButton =
		 * (ToggleButton)findViewById(R.id.show_stream_option_toggle);
		 * streamSelectionTypeToggleButton
		 * .setOnClickListener(onStreamSelectionTypeToggleButtonClicked);
		 * showChatIconsToggleButton =
		 * (ToggleButton)findViewById(R.id.show_icon_option_toggle);
		 * showChatIconsToggleButton
		 * .setOnClickListener(onShowChatIconsToggleButtonClicked);
		 * optionsDoneButton = (Button)findViewById(R.id.options_done_button);
		 * optionsDoneButton.setOnClickListener(onOptionsDoneButtonClicked);
		 */
	}

	private void createVideoTab()
	{
		// ********************* VIDEO TAB *******************************
		videoChooserViewFlipper = (ViewFlipper) findViewById(R.id.video_chooser_flipper);

		videoTabSpec = myTabHost.newTabSpec(VIDEO_TAB_TAG);
		if (tabIcons)
		{
			videoTabSpec.setIndicator("Video", getResources().getDrawable(
					R.drawable.video_camera));
		} else
		{
			videoTabSpec.setIndicator("Video");
		}
		videoTabSpec.setContent(R.id.video_chooser_flipper);
		myTabHost.addTab(videoTabSpec);

		createVideoView();
		createStreamChooserView();
	}

	private void createVideoView()
	{
		// Create items to display in video viewer tab

		// Create view viewer and accompanying media controller
		videoViewer = (VideoView) findViewById(R.id.video_app);
		mediaController = new MediaController(this);
		mediaController.setAnchorView(videoViewer);

		// Create area of interest button
		chooseAreaOfInterest = (Button) findViewById(R.id.aoi_button);
		chooseAreaOfInterest.setOnClickListener(onChooseAOIButtonClicked);

		// Create choose stream button
		chooseStreamButton = (Button) findViewById(R.id.choose_stream);
		chooseStreamButton.setOnClickListener(onChooseStreamButtonClicked);

		fullScreenButton = (Button) findViewById(R.id.full_screen);
		fullScreenButton.setOnClickListener(onFullScreenButtonClicked);

		// Create see chat pop ups toggle button with cool green light

		/*
		 * seeChatToggleButton = (ToggleButton) findViewById(R.id.see_chat);
		 * seeChatToggleButton.setChecked(true);
		 * seeChatToggleButton.setOnClickListener(onSeeChatToggleButtonClicked);
		 */

	}

	private void createStreamChooserView()
	{
		// setContentView(R.layout.stream_chooser);
		streamChooserLayout = (LinearLayout) findViewById(R.id.stream_chooser_view);

		// Set up a ListView with clickable rows. Each row shows a label for a
		// data stream
		streamListView = (ListView) findViewById(R.id.stream_list);

		/* streamFQDN = FirstResponder.getStreams(); */

		/* streamLabels = getStreamLabels(streamFQDN) */;

		// getStreams();

		streamListAdapter = new ArrayAdapter<String>(this, R.layout.stream_row,
				streamLabels);

		streamListView.setAdapter(streamListAdapter);
		streamListView.setOnItemClickListener(streamListViewRowListener);

		enterStream = (EditText) findViewById(R.id.enter_stream);

		setStream = (Button) findViewById(R.id.set_stream);
		cancelSetStream = (Button) findViewById(R.id.cancel_set_stream);

		setStream.setOnClickListener(onSetStreamClick);

		cancelSetStream.setOnClickListener(onCancelSetStreamClick);
	}

	private OnItemClickListener streamListViewRowListener = new OnItemClickListener()
	{

		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position,
				long id)
		{
			enterStream.setText(streamFQDN.get(position));
		}

	};

	public double min(double d1, double d2)
	{
		if (d1 <= d2)
		{
			return d1;
		} else
		{
			return d2;
		}
	}

	public double max(double d1, double d2)
	{
		if (d1 >= d2)
		{
			return d1;
		} else
		{
			return d2;
		}
	}

	public void checkStreamAlert(double latitude, double longitude, String url,
			String description)
	{
		double pt1Lat;
		double pt1Long;
		double pt2Lat;
		double pt2Long;
		Boolean streamBool;

		if (lastStreamAlert.containsKey(url))
		{
			// Do nothing...
		} 
		else
		{
			lastStreamAlert.put(url, false);
		}

		streamBool = lastStreamAlert.get(url);

		for (int i = 0; i < aoiDoubles.size(); i++)
		{
			pt1Lat = aoiDoubles.get(i);
			i++;
			pt1Long = aoiDoubles.get(i);
			i++;
			pt2Lat = aoiDoubles.get(i);
			i++;
			pt2Long = aoiDoubles.get(i);
			// Don't increment i here, the for loop will do it for you.

			// IF feed location is between point1 and point2 (and pt1/pt2 aren't
			// straddling the International dateline or the poles...)
			if (min(pt1Lat, pt2Lat) <= latitude
					&& latitude <= max(pt1Lat, pt2Lat)
					&& min(pt1Long, pt2Long) <= longitude
					&& longitude <= max(pt1Long, pt2Long))
			{
				if (streamBool)
				{
					// Do nothing.....
				} 
				else
				{
					streamBool = true;
					// setStream(stream); Let notifier handle this......
					createEnterNotification("Stream in AOI", "Stream in AOI",
							description + " entering AOI", enterNotificationID,
							url);
					enterNotificationID += 2;
				}
			}
			else if (streamBool)
			{
				streamBool = false;
				createLeaveNotification("Stream Leaving AOI",
						"Stream Leaving AOI", description + "leavingAOI",
						leaveNotificationID);
				leaveNotificationID += 2;
			}
		}

	}

	private Button.OnClickListener onSetStreamClick = new Button.OnClickListener()
	{

		public void onClick(View v)
		{
			// Get the text from the edit text box
			final String enterStreamText = enterStream.getText().toString();

			// If the enter text is left blank, don't update the stream
			if (enterStreamText != null
					&& !enterStreamText.equalsIgnoreCase(""))
			{

				hideKeyboard(v);

				setStream(enterStreamText);

				videoChooserViewFlipper.showPrevious();
			} else
			{
				// Create an alert dialog popup telling user they left the edit
				// text box blank
				DialogInterface.OnClickListener atRootButtonListener = new DialogInterface.OnClickListener()
				{
					// @Override
					public void onClick(DialogInterface arg0, int arg1)
					{
						// Do nothing
					}
				};

				new AlertDialog.Builder(v.getContext())
						.setTitle("At the top")
						.setMessage(
								"No stream selected or manually entered. Try again.")
						.setPositiveButton("OK", atRootButtonListener).show();
			}
		}

	};

	private Button.OnClickListener onCancelSetStreamClick = new Button.OnClickListener()
	{

		public void onClick(View v)
		{
			videoChooserViewFlipper.showPrevious();
		}

	};

	private void createChatTab()
	{
		// ******************** CHAT TAB **********************************
		// Set up chat tab
		chatTabSpec = myTabHost.newTabSpec(CHAT_TAB_TAG);
		if (tabIcons)
		{
			chatTabSpec.setIndicator("Chat", getResources().getDrawable(
					R.drawable.speechbubble));
		} else
		{
			chatTabSpec.setIndicator("Chat");
		}
		chatTabSpec.setContent(R.id.chat_viewer);
		myTabHost.addTab(chatTabSpec);

		// Create the addChatGroupButton that is ALWAYS display under chat tab
		// and
		// launches add/remove chat group method
		Button addChatGroupButton = (Button) findViewById(R.id.add_chat_group);

		addChatGroupButton
				.setBackgroundResource(R.drawable.timepicker_up_normal);

		addChatGroupButton.setOnClickListener(onAddChatGroupButtonClicket);

		tabRow = (LinearLayout) findViewById(R.id.tab_row);

		chatPostsView = (ListView) findViewById(R.id.show_chat);

		// Create field to enter chats to send
		enterChat = (EditText) findViewById(R.id.enter_chat);

		// Create send caht button
		sendChatButton = (Button) findViewById(R.id.send_chat);
		sendChatButton.setOnClickListener(onSendChatButtonClicked);

	}

	// ******************** FUNCTIONAL METHODS
	// *****************************************
	// Hook for actual JSON getStreams() method. Currently hard coded for
	// testing purposes
	/*
	 * public static ArrayList<String> getStreams() { ArrayList<String> streams
	 * = null;
	 * 
	 * //For testing purposes only....................................... //Set
	 * up a few dummy strings.... streams = new ArrayList<String>();
	 * 
	 * streams.add( "/sdcard/test2.3gp");streams.add(
	 * "rtsp://v3.cache6.c.youtube.com/CjgLENy73wIaLwkc2NKc3hvZQBMYESARFEIJbXYtZ29vZ2xlSARSB3JlbGF0ZWRgrOuMlcOelOVMDA==/0/0/0/video.3gp"
	 * ); streams.add(
	 * "rtsp://v3.cache4.c.youtube.com/CjYLENy73wIaLQn-5SAo5qVUFhMYESARFEIJbXYtZ29vZ2xlSARSBXdhdGNoYKzrjJXDnpTlTAw=/0/0/0/video.3gp"
	 * ); streams.add("/sdcard/CDR-Dinner_LAN_800k.mp4");
	 * streams.add("rtsp://192.168.13.133:8000/stream.sdp"); //End for testing
	 * purposes only...................................... return streams; }
	 */

	/*
	 * Method to launch Stream Chooser view that allows stream to be chosen and
	 * stream value returned to FirstResponder
	 */

	private void createEnterNotification(String ticker, String title,
			String text, int intentID, String stream)
	{
		Notification notification = new Notification(
				R.drawable.ic_notification_overlay, ticker, System
						.currentTimeMillis());
		// Intent notificationIntent = new Intent(this, this.getClass());
		Intent startFullScreenIntent = new Intent(FirstResponder.this,
				FullScreenVideo.class);

		Bundle startFullScreenBundle = new Bundle();

		startFullScreenBundle.putString(INTENT_KEY_FULLSCREEN, stream);
		// startFullScreenBundle.putString( INTENT_KEY_FULLSCREEN,
		// "/sdcard/test2.3gp" );

		startFullScreenIntent.putExtras(startFullScreenBundle);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				startFullScreenIntent, 0);

		/*
		 * long[] vibTimes = {0, 100, 200, 300}; enteringNotification.vibrate =
		 * vibTimes;
		 */

		notification.defaults = Notification.DEFAULT_ALL;
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notification.setLatestEventInfo(getApplicationContext(), title, text,
				contentIntent);

		notifier.notify(intentID, notification);
	}

	private void createLeaveNotification(String ticker, String title,
			String text, int intentID)
	{
		Notification notification = new Notification(
				R.drawable.ic_notification_overlay, ticker, System
						.currentTimeMillis());

		Intent startSeinfeldIntent = new Intent(FirstResponder.this,
				Seinfeld.class);

		/*
		 * Bundle startFullScreenBundle = new Bundle();
		 * 
		 * startFullScreenBundle.putString(INTENT_KEY_FULLSCREEN, stream);
		 */
		// startFullScreenBundle.putString( INTENT_KEY_FULLSCREEN,
		// "/sdcard/test2.3gp" );

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				startSeinfeldIntent, 0);

		notification.defaults = Notification.DEFAULT_ALL;
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notification.setLatestEventInfo(getApplicationContext(), title, text,
				contentIntent);

		notifier.notify(intentID, notification);
	}

	/*
	 * private void sendToStreamChooser() { //Build Intent, Bundle for the
	 * Intent, and Extras to go inside Bundle Intent startStreamChooserIntent =
	 * new Intent(FirstResponder.this, StreamChooser.class);
	 * 
	 * Bundle startStreamChooserBundle = new Bundle();
	 * 
	 * startStreamChooserIntent.putExtras(startStreamChooserBundle);
	 * 
	 * //Tell stream chooser to use Request Code Stream to identify type of
	 * return intent startActivityForResult(startStreamChooserIntent,
	 * REQ_CODE_STREAM); }
	 */

	private void processJSONFeedArray(String feedArrayString)
	{
		JSONArray jsonArray;
		try
		{
			jsonArray = new JSONArray(feedArrayString);

			if (jsonArray.length() > 0)
			{
				streamListAdapter.clear();
			} else
			{
				Log.i("MAYBE EMPTY MAYBE BAD",
						"This may be bad or may be empty");
				return; // TODO It's empty, we'll leave the feeds we have in
				// place. Real world, we need a CLEAR signal......
			}
			JSONObject jsonFeed = null;
			JSONObject json = null;
			String url = "";
			String description = "";
			double streamLat = 0.0;
			double streamLong = 0.0;

			for (int i = 0; i < jsonArray.length(); i++)
			{
				try
				{
					jsonFeed = jsonArray.getJSONObject(i);
					json = new JSONObject(jsonFeed.getString("feed"));
					url = json.getString("url");
					description = json.getString("description");
					streamLat = json.getDouble("latitude");
					streamLong = json.getDouble("longitude");
					streamListAdapter.add(description);
					streamFQDN.add(url);

					checkStreamAlert(streamLat, streamLong, url, description);
				} catch (JSONException e)
				{
					Log.e("JSON FEED ERROR",
							"Did not get a JSONObjecty out of feedString", e);
				}

			}
		} catch (JSONException e1)
		{
			// TODO Auto-generated catch block
			Log.e("JSON ARRAY ERROR",
					"Did not get a JSONArray out of feedArrayString", e1);
		}
	}

	private void sendToMapActivity()
	{
		Intent startMapActivityIntent = new Intent(this, MapsActivity.class);

		Bundle startMapActivityBundle = new Bundle();

		startMapActivityIntent.putExtras(startMapActivityBundle);

		startActivity(startMapActivityIntent);

		// put bundle stuff here........
	}

	private void sendToFullScreen()
	{
		// Build Intent, Bundle for the Intent, and Extras to go inside Bundle
		Intent startFullScreenIntent = new Intent(FirstResponder.this,
				FullScreenVideo.class);

		Bundle startFullScreenBundle = new Bundle();

		startFullScreenBundle
				.putString(INTENT_KEY_FULLSCREEN, video.toString());
		startFullScreenBundle.putStringArrayList(INTENT_KEY_CHAT_ARRAY,
				getButtonLabelList());
		// startFullScreenBundle.putString( INTENT_KEY_FULLSCREEN,
		// "/sdcard/test2.3gp" );

		startFullScreenIntent.putExtras(startFullScreenBundle);

		// Tell stream chooser to use Request Code Stream to identify type of
		// return intent
		startActivity(startFullScreenIntent);
	}

	public void receiveChat(String chatGroup, String chatString)
	{
		for (int i = 0; i < chatGroupIDList.size(); i++)
		{
			if (chatGroup.equalsIgnoreCase(chatGroupIDList.get(i)))
			{
				chatPostsAdapterList.get(i).add(chatString);
				// Highlight button-tab of chat window just updated so the user
				// knows a new chat is there
				// Button button = buttonArrayList.get(i);
				// button.setBackgroundDrawable(android.R.drawable.btn_star);

				// IF the user wants chat popups and is NOT already looking at
				// the chat tab.....
				if (showChatPopUps
						&& myTabHost.getCurrentTabTag() != CHAT_TAB_TAG)
				{
					sendChatToast(chatGroup, chatString);
				}
			}
		}
	}

	public void sendChatToast(String chatGroup, String chatString)
	{
		Toast toast = Toast.makeText(getApplicationContext(), chatGroup + ": "
				+ chatString, Toast.LENGTH_LONG);
		toast.show();
	}

	public void sendChat(String chatString, String room)
	{
		try
		{
			jsChatClientService.post(chatString, room);
		} catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e)
		{
			Toast.makeText(this, "Connection to chat server appears down",
					Toast.LENGTH_LONG).show();
		}

		// FOR TESTING PURPOSES ONLY...............................
		/*
		 * chatArrayList.add(chatString); enterChat.setText("");
		 * showChatListView.setAdapter(chatListAdapter);
		 */
	}

	// This method gets invoked by the actual chat code to update the chat
	// ListView and possibly video pop-ups
	/*
	 * public void receiveChat(String chatString) { //IF seeChatPopUp is
	 * selected AND we're not in the chat tab, show chat posts as Toast if
	 * (showChatPopUps && !
	 * myTabHost.getCurrentTabTag().equalsIgnoreCase(CHAT_TAB_TAG)) {
	 * Toast.makeText(this.getBaseContext(), chatString, Toast.LENGTH_LONG); } }
	 */

	/*
	 * private Button makeGenericButton(int index, String title, Context context
	 * ) { Button button = new Button(context);
	 * 
	 * LayoutParams buttonLayout = new LayoutParams(
	 * android.view.ViewGroup.LayoutParams.FILL_PARENT,
	 * android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1);
	 * buttonLayout.setMargins(0, 1, 3, 0);
	 * 
	 * button.setLayoutParams( buttonLayout );
	 * button.setBackgroundResource(android.R.color.darker_gray);
	 * 
	 * button.setId(index); button.setText(title);
	 * 
	 * button.setOnClickListener(buttonOnClickListener);
	 * 
	 * return button;
	 * 
	 * }
	 */

	private Button makeGenericButton(String title, Context context)
	{
		Button button = new Button(context);

		LayoutParams buttonLayout = new LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1);
		buttonLayout.setMargins(0, 1, 3, 0);

		button.setLayoutParams(buttonLayout);
		button.setBackgroundResource(android.R.color.darker_gray);

		button.setText(title);

		button.setOnClickListener(buttonOnClickListener);

		return button;

	}

	// Get a new stream name and restart the video viewer
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
		/*
		 * chatArrayList.add("one"); chatArrayList.add("two");
		 * chatArrayList.add("three");
		 */
	}

	// *********************************** VIDEO TAB LISTENERS
	// *******************************************
	private Button.OnClickListener onChooseStreamButtonClicked = new Button.OnClickListener()
	{

		public void onClick(View v)
		{
			// sendToStreamChooser();
			videoChooserViewFlipper.showNext();
		}

	};

	private Button.OnClickListener onChooseAOIButtonClicked = new Button.OnClickListener()
	{

		@Override
		public void onClick(View v)
		{
			// start Map Activity
			sendToMapActivity();

		}

	};

	private void toggleTestNotifier()
	{
		if (showChatPopUps)
		{
			showChatPopUps = false;
			createEnterNotification("Entering AOI...", "AOI Alert",
					"Stream entering AOI", enterNotificationID, "Stream");
			// showOptionChatToggleButton.setChecked(true);
		} else
		{
			showChatPopUps = true;
			createLeaveNotification("Leaving AOI", "AOI Alert",
					"Stream leaving AOI", leaveNotificationID);
			// showOptionChatToggleButton.setChecked(false);
		}
	}

	/*
	 * private Button.OnClickListener onSeeChatToggleButtonClicked = new
	 * Button.OnClickListener() {
	 * 
	 * public void onClick(View v) { if (seeChatToggleButton.isChecked()) {
	 * showChatPopUps = true; createEnterNotification("Entering AOI...",
	 * "AOI Alert", "Stream entering AOI", enterNotificationID, "Stream");
	 * //showOptionChatToggleButton.setChecked(true); } else { showChatPopUps =
	 * false; createLeaveNotification("Leaving AOI", "AOI Alert",
	 * "Stream leaving AOI", leaveNotificationID);
	 * //showOptionChatToggleButton.setChecked(false); } } };
	 */

	private Button.OnClickListener onFullScreenButtonClicked = new Button.OnClickListener()
	{

		public void onClick(View v)
		{
			if (video == null)
			{
				Toast toast = Toast.makeText(getApplicationContext(),
						"No video stream selected", Toast.LENGTH_LONG);
				toast.show();
			} else
			{
				sendToFullScreen();
			}
		}

	};

	// *********************************** CHAT TAB LISTENERS
	// *******************************************
	private Button.OnClickListener onSendChatButtonClicked = new Button.OnClickListener()
	{

		public void onClick(View v)
		{
			sendChat(enterChat.getText().toString(), currentChatGroup.getText()
					.toString());
			hideKeyboard(v);
			enterChat.setText("");
		}

	};

	private Button.OnClickListener buttonOnClickListener = new Button.OnClickListener()
	{

		@Override
		public void onClick(View v)
		{
			// int index = v.getId();

			int index = buttonArrayList.indexOf(v);

			chatPostsView.setAdapter(chatPostsAdapterList.get(index));

			Iterator<Button> iterator = buttonArrayList.iterator();

			while (iterator.hasNext())
			{
				iterator.next().setBackgroundResource(
						android.R.color.darker_gray);
			}

			v.setBackgroundResource(android.R.color.background_light);

			currentChatGroup = buttonArrayList.get(index);

		}

	};

	private Button.OnClickListener onAddChatGroupButtonClicket = new Button.OnClickListener()
	{

		@Override
		public void onClick(final View v)
		{
			AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());

			// final Context alertContext = v.getContext();

			alert.setTitle("Add/Remove Chat Group");
			alert.setMessage("Add or Remove Chat Group");

			/*
			 * final EditText newStringLabel = new EditText(v.getContext());
			 * alert.setView(newStringLabel);
			 */

			alert.setPositiveButton("Add", new OnClickListener()
			{

				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					addChatGroup(v);
					hideKeyboard(v);
				}
			});

			alert.setNeutralButton("Delete", new OnClickListener()
			{

				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					deleteChatGroup(v);
				}

			});

			alert.setNegativeButton("Cancel", new OnClickListener()
			{

				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					// Do nothing.....

				}

			});

			// alert.setCancelable(true);

			alert.show();

		}

	};

	public ArrayList<String> getButtonLabelList()
	{
		ArrayList<String> buttonLabelList = new ArrayList<String>();

		Iterator<Button> iterator = buttonArrayList.iterator();

		while (iterator.hasNext())
		{
			buttonLabelList.add(iterator.next().getText().toString());
		}

		return buttonLabelList;
	}

	private void deleteChatGroup(final View v)
	{
		// Pop up an AlertDialog with a List of chat tabs
		AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());

		ArrayList<String> buttonLabelList = getButtonLabelList();

		ArrayAdapter<String> buttonArrayAdapter = new ArrayAdapter<String>(v
				.getContext(), R.layout.chat_group_display_row, buttonLabelList);

		alert.setTitle("Delete Chat Group");

		alert.setPositiveButton("Cancel", new OnClickListener()
		{

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				// Do nothing, just let the dialog disapear...
			}

		});

		alert.setAdapter(buttonArrayAdapter, new OnClickListener()
		{
			// User clicks a row
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				final int deleteWhich = which;
				// Pop up an AlertDialog with name of room, Delete button, and
				// Cancel button
				final Button button = buttonArrayList.get(which);

				AlertDialog.Builder deleteAlert = new AlertDialog.Builder(v
						.getContext());

				deleteAlert.setTitle("Confirm Chat Group Delettion");
				deleteAlert.setMessage("Delete " + button.getText().toString()
						+ "?");

				deleteAlert.setPositiveButton("Delete", new OnClickListener()
				{

					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						deleteChatGroupTab(deleteWhich);

					}

				});

				deleteAlert.setNegativeButton("Cancel", new OnClickListener()
				{

					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						// Do nothing, let the dialog die.......

					}

				});

				deleteAlert.show();
			}

		});

		alert.show();

		// Delete executes deleteChatGroupTab
		// Cancel just goes back to chat -- not elegant, should have 3 buttons,
		// but I'm burning out here....
	}

	private void addChatGroup(final View v)
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());

		alert.setTitle("Create/Join a Chat Group");
		alert
				.setMessage("Enter name of the chat group, if group does not already exist, it will be created.");

		final EditText chatGroupName = new EditText(v.getContext());

		chatGroupName.setInputType(InputType.TYPE_CLASS_TEXT);

		alert.setView(chatGroupName);

		alert.setPositiveButton("Join/Create", new OnClickListener()
		{

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				if (chatGroupName.getText().length() > 0)
				{
					createChatGroupTab(chatGroupName.getText().toString());
					hideKeyboard(myTabHost);
				} else
				{
					Toast toast = Toast.makeText(v.getContext(),
							"Chat group name cannot be blank",
							Toast.LENGTH_LONG);
					toast.show();
				}
			}

		});

		alert.setNegativeButton("Cancel", new OnClickListener()
		{

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				// Do nothing, just let the dialog die.....
			}

		});

		alert.show();
	}

	private void createChatGroupTab(String chatGroupName)
	{

		try
		{
			jsChatClientService.join(chatGroupName);

			Button localButton;

			localButton = makeGenericButton(chatGroupName, tabRow.getContext());
			buttonArrayList.add(localButton);

			// can see all the buttons all the time, but you don't add below b/c
			// you see ONE listview at a time!!!
			tabRow.addView(localButton);

			chatPostsAdapterList.add(new ArrayAdapter<String>(chatPostsView
					.getContext(), R.layout.chat_row, new ArrayList<String>()));

			localButton.performClick();

		} catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e)
		{
			Log.e("create chat", "IOException", e);
			Toast.makeText(this, "Chat connection appears down.",
					Toast.LENGTH_LONG);
		}
	}

	private void deleteChatGroupTab(int which)
	{
		try
		{
			jsChatClientService.join(buttonArrayList.get(which).getText()
					.toString());

			tabRow.removeView(buttonArrayList.get(which));
			buttonArrayList.remove(which);
			chatPostsAdapterList.remove(which);

		} catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e)
		{
			Log.e("create chat", "IOException", e);
			Toast.makeText(this, "Chat connection appears down.",
					Toast.LENGTH_LONG);
		}

	}

	// *********************************LOGIN/OPTION TAB
	// LISTENERS**************************************

	// *********************************** LOGIN LAYOUT LISTENERS
	// **************************************
	/*
	 * private Button.OnClickListener onLoginButtonClicked = new
	 * Button.OnClickListener() {
	 * 
	 * public void onClick(View view) { String usernameString =
	 * username.getText().toString(); String passwordString =
	 * password.getText().toString(); hideKeyboard(view);
	 * 
	 * login(usernameString, passwordString);
	 * 
	 * username.setText(""); password.setText(""); }
	 * 
	 * };
	 * 
	 * private Button.OnClickListener onOptionsButtonClicked = new
	 * Button.OnClickListener() {
	 * 
	 * public void onClick(View v) { loginOptionViewFlipper.showNext(); }
	 * 
	 * };
	 */

	// ****************** OPTION LAYOUT LISTENERS
	// ********************************
	/*
	 * private Button.OnClickListener onShowOptionChatToggleButtonClicked = new
	 * Button.OnClickListener() {
	 * 
	 * public void onClick(View v) { if (showOptionChatToggleButton.isChecked())
	 * { showChatPopUps = true; seeChatToggleButton.setChecked(true); } else {
	 * showChatPopUps = false; seeChatToggleButton.setChecked(false); } }
	 * 
	 * };
	 */

	/*
	 * private Button.OnClickListener onStreamSelectionTypeToggleButtonClicked =
	 * new Button.OnClickListener() {
	 * 
	 * public void onClick(View v) { //Do grid vs list thing here. }
	 * 
	 * };
	 */

	/*
	 * private Button.OnClickListener onShowChatIconsToggleButtonClicked = new
	 * Button.OnClickListener() {
	 * 
	 * @Override public void onClick(View v) { createNotification("Test ticker",
	 * "Test Title", "Test text", INTENT_KEY_ENTERING_NOTIFICATION); }
	 * 
	 * };
	 */

	/*
	 * private Button.OnClickListener onOptionsDoneButtonClicked = new
	 * Button.OnClickListener() {
	 * 
	 * @Override public void onClick(View v) {
	 * loginOptionViewFlipper.showPrevious();
	 * 
	 * }
	 * 
	 * };
	 */

	// ******************* ACTIVITY MANAGEMENT/RECEIVER METHODS
	// ***********************
	private void hideKeyboard(View view)
	{

		// Below hide-keyboard code copied from
		// http://stackoverflow.com/questions/1109022/how-to-close-hide-the-android-soft-keyboard

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
		// End copy from stackoverflow
	}

	/*
	 * private ListAdapter makeListAdapter(View v, int rowContext, ArrayList
	 * arrayList) { ListAdapter listAdapter = new
	 * ArrayAdapter<Adapter>(v.getContext(), rowContext, arrayList);
	 * 
	 * return listAdapter; }
	 */

	public class StreamReceiver extends BroadcastReceiver
	{
		// Display an alert that we've received a message.
		@Override
		public void onReceive(Context context, Intent intent)
		{
			Bundle bundle = intent.getExtras();

			String feedArrayString = bundle.getString("feeds");
			/*
			 * Toast toast = Toast.makeText(context, "received feed: " +
			 * feedArrayString, Toast.LENGTH_LONG); toast.show();
			 */

			processJSONFeedArray(feedArrayString);
		}
	};

	public class AOIReceiver extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context context, Intent intent)
		{
			Bundle bundle = intent.getExtras();

			double[] localAOIDoubles = bundle
					.getDoubleArray(FirstResponder.INTENT_KEY_AOILIST);

			aoiDoubles.clear();

			// Okay, I know there are Arrays method to handle this, but they're
			// not working.....
			for (int i = 0; i < localAOIDoubles.length; i++)
			{
				aoiDoubles.add(localAOIDoubles[i]);
			}

		}

	}

	public class ChatReceiver extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context context, Intent intent)
		{
			Bundle bundle = intent.getExtras();

			// String room = bundle.getString(INTENT_KEY_CHAT_ROOM);
			String message = bundle.getString(INTENT_KEY_CHAT_MESSAGE);

			processChatMessage(message);

		}

	}

	public class LoginReceiver extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context context, Intent intent)
		{
			/*jsChatClientService.connect(CHAT_SERVER_URL, 6789, RestJsonClient
					.getUser());*/
			loginToChatServer();
			
		}

	}

	private void loginToChatServer()
	{
		Log.i("FirstResponder", "Logging into chat server");
		Resources res = getResources();
		
		String chatServerKey = res.getString(R.string.chat_server_key);
		String chatServerDefault = res.getString(R.string.chat_server_default);
		String chatServerAddress = prefs.getString(chatServerKey, chatServerDefault);
		String chatUser = RestJsonClient.getUser();
		
		jsChatClientService.connect(prefs.getString(res.getString(R.string.chat_server_key), 
				res.getString(R.string.chat_server_default)), 
				6789, 
				RestJsonClient.getUser());
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		/*
		 * if (resultCode == REPLY_CODE_CANCEL) { //Do nothing, use for all
		 * cancel replies from any finishing View/Activity started from an Inent
		 * sent by FirstResponder } else { //Get the string values replied from
		 * the spawned View/Activity Bundle result = data.getExtras();
		 * 
		 * switch (requestCode) { //Stream chooser is the View returning a value
		 * case REQ_CODE_STREAM:
		 * 
		 * setStream(result.getString(INTENT_KEY_STREAM));
		 * 
		 * break;
		 * 
		 * //Options is the View returning a value case REQ_CODE_OPTIONS:
		 * 
		 * switch (resultCode) { //Put actions from options reply (if any) here
		 * } break; } }
		 */
	}

	public void processChatMessage(String chatLine)
	{
		JSONObject json;
		try
		{
			json = new JSONObject(chatLine);

			String time = json.getString("time");

			JSONObject jsonInner = new JSONObject(json.getString("message"));

			String room = jsonInner.getString("room");
			String user = jsonInner.getString("user");
			String message = jsonInner.getString("message");

			for (int i = 0; i < buttonArrayList.size(); i++)
			{
				if (buttonArrayList.get(i).getText().toString()
						.equalsIgnoreCase(room))
				{
					chatPostsAdapterList.get(i).add(user + ": " + message);
					break;
				}
			}
		} catch (JSONException e)
		{
			Log.e("PROCESS_CHAT", "Received JSON in chat, didn't parse", e);
			Toast.makeText(this, "Received bad chat message!",
					Toast.LENGTH_LONG);
		}
	}
}