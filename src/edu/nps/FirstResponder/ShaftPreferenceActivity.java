package edu.nps.FirstResponder;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class ShaftPreferenceActivity extends PreferenceActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
