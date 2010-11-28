package edu.nps.FirstResponder;

import java.util.ArrayList;
import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MapsActivity extends MapActivity {
	MapView mapView;
	MapController mc;
	GeoPoint p;
	boolean isSelectAOI = false;
	private ArrayList<AreaOfInterest> aoiList = new ArrayList<AreaOfInterest>(); 
	private boolean debugMode = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_main);

		mapView = (MapView) findViewById(R.id.mapView);
		mapView.setBuiltInZoomControls(true);
		mc = mapView.getController();

//		String coordinates[] = { "1.352566007", "103.78921587" };
//		double lat = Double.parseDouble(coordinates[0]);
//		double lng = Double.parseDouble(coordinates[1]);
//
//		String coordinates2[] = { "1.362566007", "103.79921587" };
//		double lat2 = Double.parseDouble(coordinates2[0]);
//		double lng2 = Double.parseDouble(coordinates2[1]);
//
//		GeoPoint geoPoint1 = new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));
//		GeoPoint geoPoint2 = new GeoPoint((int) (lat2 * 1E6),
//				(int) (lng2 * 1E6));
//		mc = mapView.getController();
//		mc.animateTo(geoPoint1);
//		mc.setZoom(17);
//		mapView.invalidate();
		AOIMapOverlay mapOverlay = new AOIMapOverlay(this);

		List<Overlay> listOfOverlays = mapView.getOverlays();
		listOfOverlays.clear();
		listOfOverlays.add(mapOverlay);

		LocationResult locationResult = new LocationResult();
		MyLocation myLocation = new MyLocation();
		// Initialises MyLocation Class
		// 1st Parameter: Context
		// 2nd Parameter: Minimum time interval for notifications (in msec)
		// 3rd Parameter: Minimum distance interval for notifications (in
		// meters)
		// 4th Parameter: LocationResult callback instance
		// Returns true if successful. False if no location providers are
		// available.
		myLocation.init(getApplicationContext(), 0, 0, locationResult);


		final Button button = (Button) findViewById(R.id.aoiButton);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Perform action on click
				enableAoiSelection();
				Toast.makeText(
						getBaseContext(),
						"Click on a grid to select an Area of Interest",
						Toast.LENGTH_SHORT).show();
				mc.stopPanning();
			}
		});

	}
	
	public boolean isDebugMode() {
		return debugMode;
	}
	
	public void enableAoiSelection() {
		isSelectAOI = true;
	}
	
	public void disableAoiSelection() {
		isSelectAOI = false;
	}
	
	public boolean isAoiSelection() {
		return isSelectAOI;
	}
	
	/*
	 * Sets the arraylist
	 */
	public void addToAOIList(AreaOfInterest aoi) {
		aoiList.add(aoi);
	}
	
	/*
	 * Returns arraylist containing the Areas of Interest
	 */
	public ArrayList<AreaOfInterest> getAOIList() {
		return aoiList;
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	private class LocationResult extends MyLocation.LocationResult {

		@Override
		public void gotLocation(final Location location) {
			double lat = location.getLatitude();
			double lng = location.getLongitude();

			p = new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));
			mc.animateTo(p);
			mc.setZoom(17);
			mapView.invalidate();
//			String text = "[Location Update]\nLat = " + lat + "\nLong = " + lng;
//			Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT)
//					.show();
		}
	}
}