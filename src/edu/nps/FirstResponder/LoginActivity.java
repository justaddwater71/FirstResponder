package edu.nps.FirstResponder;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import java.util.*;
import android.util.Log;
import java.lang.reflect.Type;

import org.json.JSONException;
import org.json.JSONObject;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class LoginActivity extends Activity implements OnClickListener {
	/** Called when the activity is first created. */
	// @Override
	// public void onCreate(Bundle savedInstanceState) {
	// super.onCreate(savedInstanceState);
	// TextView tv = new TextView(this);
	//
	// // Instantiate the Web Service Class with he URL of the web service not
	// that you must pass
	//
	// WebService webService = new
	// WebService("http://www.sumasoftware.com/alerts/GetAlerts.php");
	//
	// //Pass the parameters if needed , if not then pass dummy one as follows
	// Map<String, String> params = new HashMap<String, String>();
	// params.put("var", "");
	//
	// //Get JSON response from server the "" are where the method name would
	// normally go if needed example
	// // webService.webGet("getMoreAllerts", params);
	// String response = webService.webGet("", params);
	// Log.d("Test: ", "test");
	// //webService.webGet("", params);
	// //try
	// //{
	// //Parse Response into our object
	// Type collectionType = new TypeToken<List<Alerts>>(){}.getType();
	// List<Alerts> alert = new Gson().fromJson(response, collectionType);
	// //Alerts alert = new Gson().fromJson(response, Alerts.class);
	//
	// //}
	// //catch(Exception e)
	// //{
	// // Log.d("Error: ", e.getMessage());
	// //}
	//
	//
	// //tv.setText("JSON Test");
	// String s = new String();
	// for(Alerts item: alert)
	// {
	// s = s + "\n" + item.toString();
	// //tv.setText(item.toString());
	// }
	// tv.setText(s.toString());
	// setContentView(tv);
	// //setContentView(R.layout.main);
	// }
	//
	// public void onClick(View arg0) {
	// // TODO Auto-generated method stub
	//
	// }
	private static final int SERVER_PORT = 80;

	private static final String DEB_TAG = "Json_Android";
	

	public static final String PREFS_NAME = "HelloAndroidPREFS";

	private SharedPreferences settings;

	private ProgressDialog progress;

	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle icicle) {

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		super.onCreate(icicle);

		// Restore preferences

		settings = getSharedPreferences(PREFS_NAME, 0);

		// load up the layout

		setContentView(R.layout.login_main); //FIXME this should not reference the main main -- I'm doofing this. --JHG

		// get the button resource in the xml file and assign it to a local
		// variable of type Button

		Button login = (Button) findViewById(R.id.login_button);

		Log.i(DEB_TAG, "onCreate");

		login.setOnClickListener(this);

		setUserNameText(settings.getString("Login", ""));

		setPasswordText(settings.getString("Password", ""));

	}

	public void setUserNameText(String $username) {
		EditText usernameEditText = (EditText) findViewById(R.id.txt_username);
		usernameEditText.setText($username);
	}

	public void setPasswordText(String $username) {
		EditText passwordEditText = (EditText) findViewById(R.id.txt_password);
		passwordEditText.setText($username);

	}

	/*
	 * 
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */

	public void onClick(View v) {

		// Handle based on which view was clicked.

		Log.i(DEB_TAG + " onClick ", "onClick");

		// this gets the resources in the xml file

		// and assigns it to a local variable of type EditText

		EditText usernameEditText = (EditText) findViewById(R.id.txt_username);

		EditText passwordEditText = (EditText) findViewById(R.id.txt_password);

		// the getText() gets the current value of the text box

		// the toString() converts the value to String data type

		// then assigns it to a variable of type String

		String sUserName = usernameEditText.getText().toString();

		String sPassword = passwordEditText.getText().toString();

		// call the backend using Get parameters (discouraged but works good for
		// this exampl ;) )

		String address = "http://" + FirstResponderParameters.SERVER_HOST + "/feeds/1.json?user_credentials=WNbyY6iHFN1sfalesBg";// + ":" + SERVER_PORT
				//+ "/jbackend.php?action=login&Login=" + sUserName
				//+ "&Password=" + sPassword + "";

		if (usernameEditText == null || passwordEditText == null) {
			// show some warning
		} else {
			// display the username and the password in string format
			try {

				showBusyCursor(true);

				progress = ProgressDialog.show(this,

				"Please wait...", "Login in process", true);

				Log.i(DEB_TAG, "Username: " + sUserName + "nPassword: "
						+ sPassword);

				Log.i(DEB_TAG, "Requesting to " + address);

				JSONObject json = RestJsonClient.connect(address, sUserName, sPassword);
				
				//added by JHG so other Activities can access username and password
				settings.edit().putString("Login", sUserName);
				
				settings.edit().putString("Password", sPassword);
				
/*				Timer t = new Timer();
				
				LocationTimerTask locationTimerTask = new LocationTimerTask();
				
				t.schedule(locationTimerTask, 1000, 60000);*/

			} catch (Exception e) {// (JSONException e) {

				// TODO Auto-generated catch block

				e.printStackTrace();

				showBusyCursor(false);

			}// end try

			progress.dismiss();

			
//			SharedPreferences.Editor editor = settings.edit();
//			editor.putString("Login", (String) user.get("Login"));
//			editor.putString("Password", (String) user.get("Password"));
//			editor.commit();
			showBusyCursor(false);

			next();

		}// end else

	}// end OnClick

	/*

	*

	*/

	private void showBusyCursor(Boolean $show) {
		setProgressBarIndeterminateVisibility($show);
	}

	private void next() {
		// you can call another activity by uncommenting the above lines
		 Intent myIntent = new Intent( this.getBaseContext() ,
		 MapsActivity.class);
		 startActivityForResult(myIntent, 0);
		Log.i(DEB_TAG, "Login Successful!");
		//Toast.makeText(getBaseContext(), "Login successful!", Toast.LENGTH_SHORT);
	}

}