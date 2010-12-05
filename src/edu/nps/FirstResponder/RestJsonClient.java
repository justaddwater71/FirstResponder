package edu.nps.FirstResponder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class RestJsonClient {

	private static final String DEB_TAG = "Json_Android";
	private static String user;
	private static String password;
	
	public static String getUser() {
		return user;
	}

	public static void setUser(String aUser) {
		user = aUser;
	}

	public static String getPassword() {
		return password;
	}

	public static void setPassword(String aPassword) {
		password = aPassword;
	}	

	private static String getCredentials() {
		
		return (Base64.encodeBytes((user + ":" + password).getBytes()));
	}
	
	public static JSONArray connect(String url) throws JSONException {
		return connect(url, "", "");
	}

	public static JSONArray getFeeds(String url, String username,
			String password) throws JSONException {

		setUser(username);
		setPassword(password);
		HttpClient httpclient = new DefaultHttpClient();

		// Prepare a request object
		HttpGet httpget = new HttpGet(url);
		httpget.setHeader("Content-type", "application/JSON");
		httpget.addHeader("Authorization", "Basic " + getCredentials());

		// Execute the request
		HttpResponse response;

		JSONArray json = new JSONArray();

		try {

			response = httpclient.execute(httpget);

			HttpEntity entity = response.getEntity();

			if (entity != null) {

				// A Simple JSON Response Read
				InputStream instream = entity.getContent();
				String result = convertStreamToString(instream);
				Log.i(DEB_TAG, "Result : " + result);

				json = new JSONArray(result);

				instream.close();
				
			}

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return json;
	}
	
	public static JSONArray connect(String url, String username,
			String password) throws JSONException {

		setUser(username);
		setPassword(password);
		HttpClient httpclient = new DefaultHttpClient();

		// Prepare a request object
		HttpGet httpget = new HttpGet(url);
		httpget.setHeader("Content-type", "application/JSON");
		httpget.addHeader("Authorization", "Basic " + getCredentials());

		// Execute the request
		HttpResponse response;

		JSONArray json = new JSONArray();

		try {

			response = httpclient.execute(httpget);

			HttpEntity entity = response.getEntity();

			if (entity != null) {

				// A Simple JSON Response Read
				InputStream instream = entity.getContent();
				String result = convertStreamToString(instream);
				Log.i(DEB_TAG, "Result : " + result);

				json = new JSONArray(result);

				instream.close();
				
			}

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return json;
	}

	public static void sendLocation(String url, double lat, double lng) {
		Double latitude = new Double(lat);
		Double longitude = new Double(lng);
		sendLocation(url, latitude.toString(), longitude.toString());
	}

	public static void sendLocation(String url, String lat, String lng) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPut httpPut = new HttpPut();
		try {
			httpPut.setURI(new URI(url));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		httpPut.setHeader("Content-type", "application/JSON");
		httpPut.addHeader("Authorization", "Basic " + getCredentials());

		JSONObject json = new JSONObject();
		JSONObject location = new JSONObject();
	       
		try {
			location.put("latitude", lat);
			location.put("longitude", lng);    
			json.put("location", location);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		StringEntity stringEntity;
		try {
			stringEntity = new StringEntity(json.toString());
			httpPut.setEntity(stringEntity);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Execute the request
		HttpResponse response;

		try {
			response = httpclient.execute(httpPut);
			HttpEntity entity = response.getEntity();

			if (entity != null) {

				// A Simple JSON Response Read
				InputStream instream = entity.getContent();
				String result = convertStreamToString(instream);
				Log.i(DEB_TAG, "Result : " + result);

				instream.close();
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param is
	 * @return String
	 */
	public static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
}
