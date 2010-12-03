package edu.nps.FirstResponder;


import java.io.Serializable;

import com.google.android.maps.GeoPoint;

public class AreaOfInterest
{
	private GeoPoint geoPt1;
	private GeoPoint geoPt2;
	
	public AreaOfInterest(GeoPoint geoPt1, GeoPoint geoPt2) {
		this.geoPt1 = geoPt1;
		this.geoPt2 = geoPt2;
	}

	public GeoPoint getGeoPt1() {
		return geoPt1;
	}

	public void setGeoPt1(GeoPoint geoPt1) {
		this.geoPt1 = geoPt1;
	}

	public GeoPoint getGeoPt2() {
		return geoPt2;
	}

	public void setGeoPt2(GeoPoint geoPt2) {
		this.geoPt2 = geoPt2;
	}
	
	public double getPt1Latitude() {
		return ((double)geoPt1.getLatitudeE6())/1E6;
	}
	
	public double getPt1Longitude() {
		return ((double)geoPt1.getLongitudeE6())/1E6;
	}
	
	public double getPt2Latitude() {
		return ((double)geoPt2.getLatitudeE6())/1E6;
	}
	
	public double getPt2Longitude() {
		return ((double)geoPt2.getLongitudeE6())/1E6;
	}

}