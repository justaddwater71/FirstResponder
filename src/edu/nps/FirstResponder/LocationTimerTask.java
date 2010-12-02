package edu.nps.FirstResponder;

import java.util.TimerTask;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.maps.GeoPoint;


public class LocationTimerTask extends TimerTask {

	String address = "http://" + FirstResponderParameters.SERVER_HOST + "/users/1/location.json";
	MapsActivity parent;
	//Added by JHG to support getting feeds information and to later generalize the latitude update
	SharedPreferences settings;
	
	public void setParent(MapsActivity aParent) {
		parent = aParent;
	}
	
	@Override
	public void run() {
		// To send location here
		GeoPoint curLoc = parent.getCurLocation();
		Log.i(FirstResponderParameters.DEB_TAG, "Location Timer Task Started.");
		
		double lat = 0;
		double lng = 0;
		
		if( curLoc != null ) 
		{
			lat = ((double)curLoc.getLatitudeE6())/1E6;
			lng = ((double)curLoc.getLongitudeE6())/1E6;
			Log.i(FirstResponderParameters.DEB_TAG, "Sending location : " + lat + ", " + lng );
			RestJsonClient.sendLocation(address, lat, lng);
			Log.i(FirstResponderParameters.DEB_TAG, "Location Timer Task Ended.");
		}
		
		Context context = parent.getBaseContext();
		settings = context.getSharedPreferences(LoginActivity.PREFS_NAME, 0);
		JSONObject feedsJSON = RestJsonClient.connect("http://" + FirstResponderParameters.SERVER_HOST + "/feeds.json",settings.getString("Login", ""), settings.getString("Password", ""));
		
		Intent intent = new Intent("geo_update");//FIXME make this a ref to a central string constant
		intent.putExtra("feeds", feedsJSON.toString());
		intent.putExtra("latitude", lat);
		intent.putExtra("longitude", lng);
		parent.sendBroadcast(intent);
		
		Log.i(FirstResponderParameters.DEB_TAG, "Feed Timer Task Ended.");
	}

}
