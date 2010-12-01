package edu.nps.FirstResponder;

import java.util.TimerTask;

import android.util.Log;

import com.google.android.maps.GeoPoint;


public class LocationTimerTask extends TimerTask {

	String address = "http://" + FirstResponderParameters.SERVER_HOST + "/users/1/location.json";
	MapsActivity parent;
	
	public void setParent(MapsActivity aParent) {
		parent = aParent;
	}
	
	@Override
	public void run() {
		// To send location here
		GeoPoint curLoc = parent.getCurLocation();
		Log.i(FirstResponderParameters.DEB_TAG, "Location Timer Task Started.");
		if( curLoc != null ) {
			double lat = ((double)curLoc.getLatitudeE6())/1E6;
			double lng = ((double)curLoc.getLongitudeE6())/1E6;
			Log.i(FirstResponderParameters.DEB_TAG, "Sending location : " + lat + ", " + lng );
			RestJsonClient.sendLocation(address, lat, lng);
		}
		Log.i(FirstResponderParameters.DEB_TAG, "Location Timer Task Ended.");
	}

}
