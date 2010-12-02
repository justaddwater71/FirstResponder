package edu.nps.FirstResponder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class StreamChooser extends Activity 
{
	ListView	streamListView;
	EditText 	enterStream;
	
	DialogInterface.OnClickListener atRootButtonListener = new DialogInterface.OnClickListener()
	{
		//@Override
		public void onClick(DialogInterface arg0, int arg1) 
		{
			//Do nothing
		}
	};
	
	Button 								setStream;
	Button 								cancelSetStream;
	ArrayList<String> 			streamLabels = new ArrayList<String>();
	ArrayList<String> 			streamFQDN = new ArrayList<String>();
	ArrayAdapter	<String>	streamListAdapter;

	 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mainView();
    }
    
    private void mainView()
    {
    	setContentView(R.layout.stream_chooser);
    	
    	//Set up a ListView with clickable rows.  Each row shows a label for a data stream
    	streamListView 	= (ListView)findViewById(R.id.stream_list);
    	
       /* streamFQDN			= FirstResponder.getStreams();*/
        
        /*streamLabels 		= getStreamLabels(streamFQDN)*/;
        
        getStreams();
      
        streamListAdapter	= new ArrayAdapter<String>(this, R.layout.stream_row, streamLabels);
        
        streamListView.setAdapter(streamListAdapter);
        streamListView.setOnItemClickListener(streamListViewRowListener);
        
        enterStream 			= (EditText)findViewById(R.id.enter_stream);
        
        setStream 				= (Button)findViewById(R.id.set_stream);
        cancelSetStream = (Button)findViewById(R.id.cancel_set_stream);
        
        setStream.setOnClickListener(onSetStreamClick);
        
        cancelSetStream.setOnClickListener(onCancelSetStreamClick);
    }
    
    private OnItemClickListener streamListViewRowListener = new OnItemClickListener() 
    {

		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position, long id) 
		{
			enterStream.setText(streamFQDN.get(position));
		}
    	
    };
    
    public void checkStreamAlert(String latitude, String longitude, double aoiLat, double aoiLong)
    {
    	double feedLat = Double.parseDouble(latitude);
    	double feedLong = Double.parseDouble(longitude);
    	
    	//if ()//FIXME need AOI lat/long NOT MY lat/long -- GO TO BED!!!
    	
    }
    
    private void jsonToStreamList(JSONObject outerJSON, double myLat, double myLong) throws JSONException
    {
    	if (outerJSON != null)
    	{
    		JSONObject json = outerJSON.getJSONObject("feed");
    		streamListAdapter.add(json.getString("description"));
    		streamFQDN.add(json.getString("url"));
    		checkStreamAlert(json.getString("latitude"), json.getString("longitude"), myLat, myLong);
    		
    	}
    }
    
    public class StreamReceiver extends BroadcastReceiver{
        // Display an alert that we've received a message.    
        @Override 
        public void onReceive(Context context, Intent intent){
            Bundle bundle = intent.getExtras();
            
            double myLat = bundle.getDouble("latitude");
            double myLong = bundle.getDouble("longitude");
            
            try
			{
				JSONArray jsonArray = new JSONArray(bundle.getString("feeds"));
				JSONObject json = null;
			
			streamListAdapter.clear();
			
			for (int i = 0; i < jsonArray.length(); i++)
			{
				json = jsonArray.getJSONObject(i);
			}
			
			jsonToStreamList(json, myLat, myLong);
			
			} catch (JSONException e)
			{
				Log.e("JSON", "feeds array did not show up correctly", e);
			}
       }
    };  
    
    public void getStreams()
    {
    	try 
    	{
    		InputStream				inputStream		= openFileInput(FirstResponder.STREAM_FILE);
			InputStreamReader streamReader = new InputStreamReader (inputStream);
			BufferedReader		bufferedReader = new BufferedReader(streamReader);
			String line;
			String[] splitTemp = new String[2];

			while ((line = bufferedReader.readLine()) != null)
			{
				try
				{
					splitTemp = line.split("\t");
					streamLabels.add(splitTemp[0]);
					streamFQDN.add(splitTemp[1]);
				}
				catch (IndexOutOfBoundsException i)
				{
					//Do nothing, bad file
				}
			}

			inputStream.close();

		}
    	//IF no file is found, then make one
    	catch (FileNotFoundException e) 
		{
    		//Do nothing, bad file
		}
    	catch (IOException e) 
		{
    		//Do nothing, bad file
		}
    	
    }
    
    private void hideKeyboard(View view)
	{

		 //Below hide-keyboard code copied from
		 //http://stackoverflow.com/questions/1109022/how-to-close-hide-the-android-soft-keyboard

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
        //End copy from stackoverflow
	}
    
    private Button.OnClickListener onSetStreamClick = new Button.OnClickListener()
    {

		public void onClick(View v) 
		{
			//Get the text from the edit text box
			final String enterStreamText = enterStream.getText().toString();

			//If the enter text is left blank, don't update the stream
			if (enterStreamText != null && ! enterStreamText.equalsIgnoreCase(""))
			{
				if (! streamFQDN.contains(enterStreamText))
				{
					streamListAdapter.add(enterStreamText);

					//This file is for testing purposes only, we won't be writing streams out in operation
					try 
					{
						OutputStream outputStream 	=	openFileOutput(FirstResponder.STREAM_FILE, Context.MODE_APPEND);
						final OutputStreamWriter outWriter	=	new OutputStreamWriter(outputStream);

						AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());

						final Context alertContext = v.getContext();

						alert.setTitle("Stream Label");
						alert.setMessage("Enter label to use for stream.");

						final EditText newStringLabel = new EditText(v.getContext());
						alert.setView(newStringLabel);

						alert.setPositiveButton("OK", new OnClickListener()
						{

							@Override
							public void onClick(DialogInterface dialog, int which) 
							{
								try 
								{
									outWriter.write(newStringLabel.getText().toString() + "\t" + enterStreamText);
								} 
								catch (IOException e) 
								{
									Toast toast = Toast.makeText(alertContext, "Your video stream is set for viewing, but has not been saved to history due to IO error --DARN!!", Toast.LENGTH_LONG);
									toast.show();
								}
							}
						});

						alert.setCancelable(true);						

						alert.show();
					} 
					catch (FileNotFoundException e)
					{
						//What?  openFileOutput is both a writer and a file creator.  I've missed something here.
					}
/*					catch (IOException i)
					{
						Toast toast = Toast.makeText(v.getContext(), "Your video stream is set for viewing, but has not been saved to history due to IO error --DARN!!", Toast.LENGTH_LONG);
						toast.show();
					}*/
				}

				//Build an intent, bundle it, add the stream as an extra, pass it back to the caller
				Intent resultIntent = new Intent();

				Bundle results = new Bundle();

				//Use stream key value from FirstResponder so it can handle it
				results.putString(FirstResponder.INTENT_KEY_STREAM, enterStreamText);

				resultIntent.putExtras(results);

				//Use the "success" reply code in the response to FirstResponder
				setResult(FirstResponder.REPLY_CODE_SET_STREAM, resultIntent);

				hideKeyboard(v);

				//Close this view
				finish();
			}
			else
			{
				//Create an alert dialog popup telling user they left the edit text box blank
				DialogInterface.OnClickListener atRootButtonListener = new DialogInterface.OnClickListener()
				{
					//@Override
					public void onClick(DialogInterface arg0, int arg1) 
					{
						//Do nothing
					}
				};

				new AlertDialog.Builder(v.getContext())
				.setTitle("At the top")
				.setMessage("No stream selected or manually entered. Try again.")
				.setPositiveButton("OK", atRootButtonListener)
				.show();
			}
		}
    	
    };
    
    private Button.OnClickListener onCancelSetStreamClick = new Button.OnClickListener()
    {

		public void onClick(View v) 
		{
			finish();
		}
    	
    };
}
