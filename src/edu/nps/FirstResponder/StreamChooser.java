package edu.nps.FirstResponder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class StreamChooser extends Activity 
{
	ListView	streamListView;
	EditText 	enterStream;DialogInterface.OnClickListener atRootButtonListener = new DialogInterface.OnClickListener()
	{
		//@Override
		public void onClick(DialogInterface arg0, int arg1) 
		{
			//Do nothing
		}
	};
	Button 	setStream;
	Button 	cancelSetStream;
	String[] 	streamLabels;
	String[] 	streamFQDN;
	
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
    	
        streamFQDN			= FirstResponder.getStreams();
        
        streamLabels 		= getStreamLabels(streamFQDN);
      
        streamListView.setAdapter(new ArrayAdapter<String>(this, R.layout.stream_row, streamLabels));
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
			enterStream.setText(streamFQDN[position]);
		}
    	
    };
    
    public String[] getStreamLabels(String[] streamFQDN)
    {
    	String[] streamLabels = null;
    	
    	//Parse an array of Strings (FQDN of each stream) into just the label/filename
    	//For testing purposes only
    	streamLabels = new String[5];
    	
    	streamLabels[0] = "Shuttle Test";
    	streamLabels[1] = "YouTube 3GP Sample 1";
    	streamLabels[2] = "YouTube 3GP Sample 2";
    	streamLabels[3] = "Philips Commericial";
    	streamLabels[4] = "My Laptop Streaming";
    	
    	return streamLabels;
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
			String enterStreamText = enterStream.getText().toString();
			
			//If the enter text is left blank, don't update the stream
			if (enterStreamText != null && ! enterStreamText.equalsIgnoreCase(""))
			{
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
