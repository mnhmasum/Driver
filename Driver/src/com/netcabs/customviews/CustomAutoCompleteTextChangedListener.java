package com.netcabs.customviews;

import java.util.ArrayList;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import com.netcabs.adapter.AutoCompleteArrayCustomAdapter;
import com.netcabs.asynctask.LocationAsyncTask;
import com.netcabs.driver.R;
import com.netcabs.driver.SearchAddressLocaion;
import com.netcabs.interfacecallback.OnComplete;
import com.netcabs.utils.ConstantValues;
import com.netcabs.utils.GPSTracker;
 
public class CustomAutoCompleteTextChangedListener implements TextWatcher {
 
    public static final String TAG = "CustomAutoCompleteTextChangedListener.java";
    Activity context;
    private ArrayList<String> names;
	private AutoCompleteArrayCustomAdapter myAdapter;
     
    public CustomAutoCompleteTextChangedListener(Activity context){
        this.context = context;
    }
     
    @Override
    public void afterTextChanged(Editable s) {
         
    }
 
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
            int after) {
         
    }
 
    @Override
    public void onTextChanged(CharSequence userInput, int start, int before, int count) {
 
        try{
        	String[] search_text = userInput.toString().trim().split(",");
			String url = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input="+ search_text[0] + "&location=" + new GPSTracker(context).getLatitude() + "," + new GPSTracker(context).getLongitude() + "&radius=500&types=establishment&sensor=true&key=" + ConstantValues.browser_key;
			url = url.replace(" ", "%20");
			if (search_text.length <= 1) {
				names = new ArrayList<String>();
				
				new LocationAsyncTask(context, url, new OnComplete() {
					
					@Override
					public void onComplete(int result,ArrayList<String> data, ArrayList<String> refId) {
						myAdapter = new AutoCompleteArrayCustomAdapter(context, this,R.layout.autocomplete_row, data);
						ConstantValues.refId = refId;
						SearchAddressLocaion.myAutoComplete.setAdapter(myAdapter);
					}

					@Override
					public void onLocationComplete(int result, double lat, double lon) {
						
					}

					@Override
					public void onAddressComplete(int result, String address) {
						
					}
					
				}, search_text, new GPSTracker(context).getLatitude(),new GPSTracker(context).getLongitude()).execute();
			}
            Log.e(TAG, "User input: " + userInput);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
         
    }
     
     
 
}