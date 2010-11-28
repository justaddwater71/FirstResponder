package edu.nps.FirstResponder;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.widget.Toast;

public class MyLocation {
	// set to false during release
	private static final boolean isDebugMode = false;
	
    private LocationManager lm;
    private LocationResult locationResult;
   	private Criteria locationCriteria;
    private Context context;
    private long minTime;
    private float minDist;

    // Initialises MyLocation Class
    // 1st Parameter: Context
    // 2nd Parameter: Minimum time interval for notifications (in msec)
    // 3rd Parameter: Minimum distance interval for notifications (in meters)
    // 4th Parameter: LocationResult callback instance
    // Returns true if successful. False if no location providers are available.
    public boolean init(Context context, long minTime, float minDist, LocationResult locationResult)
    {
        // passed in arguments
        this.context = context; 	
    	this.locationResult = locationResult;
    	this.minTime = minTime;
    	this.minDist = minDist;
    	
    	// setup location manager
    	lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
   
    	// set criteria and get best location provider that matches criteria
    	locationCriteria = new Criteria();
		locationCriteria.setAccuracy(Criteria.ACCURACY_FINE);
		
		return selectProvider();
    }
    
    private boolean selectProvider()
    {
        String text = lm.getBestProvider(locationCriteria, true);
        if(isDebugMode) Toast.makeText(context, "BestProvider: " + text, Toast.LENGTH_SHORT).show();
 
        if(text == null)
    		return false;	// no provider
    	else
    	{
    		lm.removeUpdates(locationListener);
    		lm.requestLocationUpdates(text, minTime, minDist, locationListener);
    		return true;
    	}
    }

    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            locationResult.gotLocation(location);
        }
        public void onProviderDisabled(String provider) {
        	if(isDebugMode) Toast.makeText(context, provider + " disabled", Toast.LENGTH_SHORT).show();
        	selectProvider();	// if current provider disabled, try selecting another provider
        }
        public void onProviderEnabled(String provider) {
        	if(isDebugMode) Toast.makeText(context, provider + " enabled", Toast.LENGTH_SHORT).show();
        }
        public void onStatusChanged(String provider, int status, Bundle extras) {
        	if(isDebugMode){
        		String text = "Unknown";
        		switch(status){
        			case LocationProvider.AVAILABLE: text = "Available";
        			case LocationProvider.OUT_OF_SERVICE: text = "Out Of Service";
        			case LocationProvider.TEMPORARILY_UNAVAILABLE: text = "Temporarily unavailable";
        		}
	        	Toast.makeText(context, provider + ": " + text, Toast.LENGTH_SHORT).show();
        	}
        } 
    };

    public static abstract class LocationResult{
        public abstract void gotLocation(Location location);
    }
}