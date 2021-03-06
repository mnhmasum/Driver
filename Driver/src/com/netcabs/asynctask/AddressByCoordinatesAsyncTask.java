package com.netcabs.asynctask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.netcabs.interfacecallback.OnComplete;
import com.netcabs.jsonparser.JSONParser;

public class AddressByCoordinatesAsyncTask extends AsyncTask<Void, Void, Void> {
	private String url;
	private OnComplete callback;
	private JSONObject json;
	private JSONArray dataArray = null;
	private String taxiLocationAddress = "";
	private ProgressDialog progressDialog;
	private Activity context;
	private String address = "";
	private boolean addComma = true;

	public AddressByCoordinatesAsyncTask(Activity context, String URL, OnComplete callback2) {
		this.callback = (OnComplete) callback2;
		this.url = URL;
		this.context = context;
		
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		progressDialog = ProgressDialog.show(context, "", "Loading...", true, false);
	}

	@Override
	protected Void doInBackground(Void... params) {
		JSONParser jParser = new JSONParser();
		// getting JSON string from URL
		json = jParser.getJSONFromUrl(url.toString());
		Log.i("RESPONSE", "**" + json);
		if (json != null) {
			try {
				// Getting Array of Contacts
				dataArray = json.getJSONArray("results");
				if(dataArray.length() > 0){
					JSONObject dataObject = dataArray.getJSONObject(0);
					JSONArray addressArray = dataObject .getJSONArray("address_components");
					if(addressArray.length() > 0){
						for(int i = 0; i < addressArray.length() - 1; i++) {
							JSONObject addObj = addressArray.getJSONObject(i);
							if (address.equalsIgnoreCase("")) {
								address = addObj.getString("long_name");
								if(address.matches("\\d+(\\-\\d+)?")){
									addComma = false;
								}
							} else {
								if(addComma) {
									address = address + ", "+ addObj.getString("long_name");
								} else {
									addComma = true;
									address = address + " "+ addObj.getString("long_name");
								}
								
							}
						}
					}
					
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		try {
			if ((progressDialog != null) && progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
		} catch (final IllegalArgumentException e) {
		} catch (final Exception e) {
		} finally {
			progressDialog = null;
		}
		
		try {
			callback.onAddressComplete(1, address);
		} catch (Exception e) {
			
		}
		
	}

}
