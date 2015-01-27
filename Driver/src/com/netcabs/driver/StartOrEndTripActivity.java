package com.netcabs.driver;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.w3c.dom.Document;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.netcabs.asynctask.EndTripAsyncTask;
import com.netcabs.asynctask.StartTripAsyncTask;
import com.netcabs.customviews.CustomLog;
import com.netcabs.customviews.CustomToast;
import com.netcabs.customviews.DialogController;
import com.netcabs.interfacecallback.OnRequestComplete;
import com.netcabs.internetconnection.InternetConnectivity;
import com.netcabs.utils.ConstantValues;
import com.netcabs.utils.DriverApp;
import com.netcabs.utils.GMapV2GetRouteDirection;
import com.netcabs.utils.GPSTracker;
import com.netcabs.utils.Utils;

public class StartOrEndTripActivity extends Activity implements OnClickListener, LocationListener {

	private Button btnNavigation;
	private TextView txtViewSourceToDestination;
	private TextView txtViewPassengerName;
	private TextView txtviewPassengerField;
	private ImageButton imgBtnTrafficView;
	private Marker pickUpPositionMarker;
	private Marker destinationMarker;
	private int buttonIndex = 0;
	private boolean isHailed = false;
	//private boolean isDriverHailed = false;
	private GoogleMap map;
	private LatLng destinationLatLng;
	private LatLng pickUpLatLon;
	private LatLng curenLatLng;
	private Geocoder gcd;
	private LinearLayout linearInfo;
	
	private GMapV2GetRouteDirection routeDirection;
	private Document doc;
	private PolylineOptions rectLine;
	private Polyline line;
	private ArrayList<LatLng> directionPoint;
	private Dialog dialogConfirm;
	private boolean isLock = false;
	private EndTripAsyncTask endTrip;
	private String bookingId = "";
	private boolean isPathDraw = false;
	private int i = 0;
    private int index= 0;
	private Marker currentPositionMarker;
	private boolean isFirstTime;
	public int defaultCardItemNo;
	public ArrayList<String> taxiPathArray;
	private Dialog dialogNavigation;
	
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = 1000*30; // 1 min
    private TextView txtViewDistance;
    private TextView txtViewFare;
    private TextView txtViewSpeed;
    private CountDownTimer countDownTimer;
    
    private LatLng cur_Latlng;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_start_or_end_trip);
		
		initViews();
		setListener();
		loadData();
		initLocationManager();
		initGoolgeMap();
	}

	private void initLocationManager() {
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
			
		} else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
		
		} else {
			Toast.makeText(this, "Please enable location service", Toast.LENGTH_SHORT).show();
		}
		
	} 

	private void resetMeterConstant() {
		
		//ConstantValues.METER_START_TIME = 0;
		ConstantValues.METER_END_TIME = 0;
		ConstantValues.METER_HIT_COUNTER = 0;
		ConstantValues.METER_END_TOTAL_SPEED_MPH = 0;
		ConstantValues.METER_SPEED_MPH_TOTAL = 0.0;
		ConstantValues.METER_SPEED_AVERAGE = 0.0;
		ConstantValues.UPDATE_TIME = 0;
		ConstantValues.TIME_LOST = 0;
		
	}
	
	private void getRunTimeDistanceAndSpeed(TextView text_distance, TextView text_speed){
		ConstantValues.METER_END_TIME = System.currentTimeMillis();
		ConstantValues.METER_END_TOTAL_SPEED_MPH = ConstantValues.METER_SPEED_MPH_TOTAL;
		text_speed.setText(ConstantValues.CURRENT_SPEED+" MPH");
		double travelledHours = 0.0;
		double travelledDistance = 0.0;
		
		try {
			long final_meter_end_time = ConstantValues.METER_END_TIME - ConstantValues.TIME_LOST;
			travelledHours = Utils.getTimeTraveledInHour(ConstantValues.METER_START_TIME, final_meter_end_time);
		} catch (ParseException e1) {
			e1.printStackTrace();
			travelledHours = 0.0;
		}
		
		travelledDistance = (ConstantValues.METER_END_TOTAL_SPEED_MPH/(double)ConstantValues.METER_HIT_COUNTER) * travelledHours;
		
		if (Double.isNaN(travelledDistance)) {
			travelledDistance = 0.0;
		}
		
		double distanceInKM = travelledDistance * ConstantValues.MPH_TO_KPH_CONVERTION;
		text_distance.setText(Utils.getFormatedDistance(Double.toString(distanceInKM)));
	}

	private void initGoolgeMap() {
			map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
			cur_Latlng = new LatLng(new GPSTracker(StartOrEndTripActivity.this).getLatitude(), new GPSTracker(StartOrEndTripActivity.this).getLongitude());
			
			if(map != null) {
				map.moveCamera(CameraUpdateFactory.newLatLng(cur_Latlng));
				map.animateCamera(CameraUpdateFactory.zoomTo(14.0f));
				map.setMyLocationEnabled(true);
				if(ConstantValues.isTrafficViewEnabled){
					map.setTrafficEnabled(true);
				} else {
					map.setTrafficEnabled(false);
				}
				//map.setOnMyLocationChangeListener(this);
				
				if(isHailed) {
//					map.moveCamera(CameraUpdateFactory.newLatLng(cur_Latlng));
//					map.animateCamera(CameraUpdateFactory.zoomTo(14.0f));
					pickUpPositionMarker = map.addMarker(new MarkerOptions()
					.title("Pick up Location")
					.position(cur_Latlng)
					.flat(true)
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.pickup_add_icon)));
					
					currentPositionMarker	= map.addMarker(new MarkerOptions()
							.title("My Current Position")
							.position(cur_Latlng)
							.flat(true)
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.car_location_icon)));
					if(DriverApp.getInstance().getHailedCabInfo().getDestinationName() != null) {
						destinationLatLng = new LatLng(DriverApp.getInstance().getHailedCabInfo().getDestinationLat(), DriverApp.getInstance().getHailedCabInfo().getDestinationLong());
						destinationMarker = map.addMarker(new MarkerOptions()
						.title("Destination")
						.position(destinationLatLng)
						.flat(true)
						.icon(BitmapDescriptorFactory.fromResource(R.drawable.distination_icon)));
					//	map.setMyLocationEnabled(true);
						
						if (isPathDraw ) {
							 if(InternetConnectivity.isConnectedToInternet(StartOrEndTripActivity.this)) {
						        	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						        		//new GetRouteTask(cur_Latlng, destinationLatLng).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						        	}
						        	else {
						        		//new GetRouteTask(cur_Latlng, destinationLatLng).execute();	
						        	}
						        } else {
									new CustomToast(getApplicationContext(), ConstantValues.internetConnectionMsg).showToast();
								}
						}
					}
					
				} else {
					currentPositionMarker= map.addMarker(new MarkerOptions()
							.title("My Current Position")
							.position(cur_Latlng)
							.flat(true)
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.car_location_icon)));
					
					pickUpLatLon = new LatLng(DriverApp.getInstance().getBookingInfo().getPickupLat(), DriverApp.getInstance().getBookingInfo().getPickupLon());
					pickUpPositionMarker= map.addMarker(new MarkerOptions()
					.title("Pick up Location")
					.position(pickUpLatLon)
					.flat(true)
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.pickup_add_icon)));
					
					destinationLatLng = new LatLng(DriverApp.getInstance().getBookingInfo().getDestinationLat(), DriverApp.getInstance().getBookingInfo().getDestinationLon());
					destinationMarker = map.addMarker(new MarkerOptions()
					.title("Destination")
					.position(destinationLatLng)
					.flat(true)
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.distination_icon)));
				}
			}else {
				new CustomLog(StartOrEndTripActivity.this, "MainMap", "Map is null").showLogI();
			}
		}
	
		
	private void initViews() {
		taxiPathArray = new ArrayList<String>();
		isFirstTime = true;
		btnNavigation = (Button) findViewById(R.id.btn_navigation);
		linearInfo = (LinearLayout) findViewById(R.id.linear_info);
		gcd = new Geocoder(StartOrEndTripActivity.this, Locale.getDefault());
		routeDirection = new GMapV2GetRouteDirection();
		txtViewSourceToDestination = (TextView) findViewById(R.id.txt_view_source_to_destination);
		txtViewPassengerName = (TextView) findViewById(R.id.txt_view_passenger_name);
		txtviewPassengerField = (TextView) findViewById(R.id.txtviewPassenger);
		imgBtnTrafficView = (ImageButton) findViewById(R.id.img_btn_traffic);
		if(ConstantValues.isTrafficViewEnabled){
			imgBtnTrafficView.setBackgroundResource(R.drawable.ans_mark_over);
		} else {
			imgBtnTrafficView.setBackgroundResource(R.drawable.ans_mark);
		}
	}

	private void setListener() {
		((Button) findViewById(R.id.btn_start_or_end_trip)).setOnClickListener(this);
		btnNavigation.setOnClickListener(this);
		imgBtnTrafficView.setOnClickListener(this);
	}

	private void loadData() {
		if(getIntent().getExtras() != null) {
			isHailed = getIntent().getExtras().getBoolean("is_hailed");
			if(isHailed) {
				bookingId = DriverApp.getInstance().getHailedCabInfo().getBookingId();
				//Log.e("Booking id  from start fareis", ""+bookingId);
				buttonIndex = 1;
				((Button) findViewById(R.id.btn_start_or_end_trip)).setBackgroundResource(R.drawable.end_trip);
				if (DriverApp.getInstance().getHailedCabInfo().getDestinationName() != null ) {
					isPathDraw = true;
					linearInfo.setVisibility(View.VISIBLE);
					txtViewSourceToDestination.setText(DriverApp.getInstance().getHailedCabInfo().getPickupName() + " to " +DriverApp.getInstance().getHailedCabInfo().getDestinationName());
					txtviewPassengerField.setVisibility(View.INVISIBLE);
					btnNavigation.setVisibility(View.VISIBLE);
				}  else {
					linearInfo.setVisibility(View.GONE);
					btnNavigation.setVisibility(View.GONE);
				}
				
			} else {
				resetMeterConstant();
				isPathDraw = true;
				bookingId = DriverApp.getInstance().getBookingInfo().getBookingId();
				txtViewSourceToDestination.setText(DriverApp.getInstance().getBookingInfo().getPickupAddress() + " to " +DriverApp.getInstance().getBookingInfo().getDestinationAddress());
				txtViewPassengerName.setText(DriverApp.getInstance().getBookingInfo().getFirstName() + " " + DriverApp.getInstance().getBookingInfo().getLastName());
				
			}
		}
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		
		case R.id.btn_navigation:
			showNavigationDialog();

			break;
		case R.id.img_btn_traffic:
			if(ConstantValues.isTrafficViewEnabled){
				ConstantValues.isTrafficViewEnabled = false;
				map.setTrafficEnabled(false);
				imgBtnTrafficView.setBackgroundResource(R.drawable.ans_mark);
			} else {
				ConstantValues.isTrafficViewEnabled = true;
				map.setTrafficEnabled(true);
				imgBtnTrafficView.setBackgroundResource(R.drawable.ans_mark_over);
			}
			break;
		case R.id.btn_start_or_end_trip:
			if (buttonIndex == 0) {
				ConstantValues.METER_START_TIME = 0;
				
				if(InternetConnectivity.isConnectedToInternet(StartOrEndTripActivity.this)) {
					new StartTripAsyncTask(StartOrEndTripActivity.this, new OnRequestComplete() {
						@Override
						public void onRequestComplete(String result) {
							
							try {
								if ("2001".equals(result)) {
									
									taxiPathArray.add(cur_Latlng.longitude + "," + cur_Latlng.latitude);
									ConstantValues.METER_START_TIME = System.currentTimeMillis();
									LoginActivity.startTransmission();
									
									buttonIndex = 1;
									((Button) findViewById(R.id.btn_start_or_end_trip)).setBackgroundResource(R.drawable.end_trip);
									//pickUpPositionMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.pickup_add_icon));
									curenLatLng = new LatLng(new GPSTracker(StartOrEndTripActivity.this).getLatitude(), new GPSTracker(StartOrEndTripActivity.this).getLongitude());
										
									currentPositionMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.car_location_icon));
	
									 if(isPathDraw) {
										 if(InternetConnectivity.isConnectedToInternet(StartOrEndTripActivity.this)) {
								        	try {
								        		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
									        		//*new GetRouteTask(curenLatLng, destinationLatLng).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
									        	}
									        	else {
									        		//*new GetRouteTask(curenLatLng, destinationLatLng).execute();	
									        	}
											} catch (Exception e) {
												e.printStackTrace();
											}
								        } else {
											new CustomToast(getApplicationContext(), ConstantValues.internetConnectionMsg).showToast();
										}
								 }
								}
							} catch (Exception e) {
								new CustomToast(getApplicationContext(), "Please connect OBD meter").showToast();
							}
							
						}
					}).execute("1009", DriverApp.getInstance().getDriverInfo().getTaxiId(), bookingId);
				} else {
					new CustomToast(getApplicationContext(), ConstantValues.internetConnectionMsg).showToast();
				}
			} else if(buttonIndex == 1) {
				showConfirmPopup();
			} else {
				//new CustomToast(getApplicationContext(), "Problem Occured").showToast();
			}
			break;

		default:
			break;
		}
	}
	
	private void showNavigationDialog() {
		//Intent.ACTION_VIEW,Uri.parse("http://maps.google.com/maps?" + "saddr="+ DriverApp.getInstance().getHailedCabInfo().getPickupLat() + "," + DriverApp.getInstance().getHailedCabInfo().getPickupLong() + "&daddr=" + DriverApp.getInstance().getHailedCabInfo().getDestinationLat() + "," + DriverApp.getInstance().getHailedCabInfo().getDestinationLong())
		dialogNavigation = new DialogController(StartOrEndTripActivity.this).NavigationDialog();
		
		WebView webViewNavigation= (WebView) dialogNavigation.findViewById(R.id.web_view_navigation);
		webViewNavigation.setWebViewClient(new WebViewClient());
		webViewNavigation.getSettings().setJavaScriptEnabled(true);
		if (isHailed) {
			if(DriverApp.getInstance().getHailedCabInfo().getDestinationName() != null) {
				webViewNavigation.loadUrl("http://maps.google.com/maps?" + "saddr="+ DriverApp.getInstance().getHailedCabInfo().getPickupLat() + "," + DriverApp.getInstance().getHailedCabInfo().getPickupLong() + "&daddr=" + DriverApp.getInstance().getHailedCabInfo().getDestinationLat() + "," + DriverApp.getInstance().getHailedCabInfo().getDestinationLong());
			}
		} else {
			webViewNavigation.loadUrl("http://maps.google.com/maps?" + "saddr="+ DriverApp.getInstance().getBookingInfo().getPickupLat() + "," + DriverApp.getInstance().getBookingInfo().getPickupLon() + "&daddr=" + DriverApp.getInstance().getBookingInfo().getDestinationLat() + "," + DriverApp.getInstance().getBookingInfo().getDestinationLon());
		}
	  
		txtViewDistance = (TextView) dialogNavigation.findViewById(R.id.txt_view_distance);
		//txtViewFare = (TextView) dialogNavigation.findViewById(R.id.txt_view_fare);
		txtViewSpeed = (TextView) dialogNavigation.findViewById(R.id.txt_view_fare);
		
		//txtViewDistance.setText();
		getRunTimeDistanceAndSpeed(txtViewDistance, txtViewSpeed);
		dialogNavigation.show();
		
		//showTimer(txtViewDistance);
	}

	private void showConfirmPopup() {
		dialogConfirm = new DialogController(StartOrEndTripActivity.this).ConfirmCommonDialog();
		
		Button btnExit = (Button) dialogConfirm.findViewById(R.id.btn_exit);
		
		Button btnOk = (Button) dialogConfirm.findViewById(R.id.btn_okey);
		btnOk.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialogConfirm.dismiss();
				if(InternetConnectivity.isConnectedToInternet(StartOrEndTripActivity.this)) {
					Log.e("Trip start & end paths", ""+convertArrayToString());
					 endTrip = new EndTripAsyncTask(StartOrEndTripActivity.this, new OnRequestComplete() {
						
						@Override
						public void onRequestComplete(String result) {
							try {
								if("2001".equals(result)) {
									buttonIndex = 0;
									isLock = true;
									
									/*((Button) findViewById(R.id.btn_start_or_end_trip)).setBackgroundResource(R.drawable.start_trip);
									startActivity(new Intent(StartOrEndTripActivity.this, CalculatorActivity.class).putExtra("is_hailed", isHailed));*/
									
									startActivity(new Intent(StartOrEndTripActivity.this, CalculateFareActivity.class).putExtra("is_hailed", isHailed));
									finish(); 
									
								} else {
									new CustomToast(getApplicationContext(), ConstantValues.errorMessage).showToast();
								}
								
							} catch (Exception e) {
								new CustomToast(getApplicationContext(), "Endtrip is" + e.getMessage()).showToast();
							}
							
						}
					});
					
					GPSTracker gps = new GPSTracker(StartOrEndTripActivity.this);
					LatLng cur_Latlng = new LatLng(gps.getLatitude(), gps.getLongitude());
					if(cur_Latlng != null) {
						Log.e("current laat & long is", cur_Latlng.latitude+":::::::::"+cur_Latlng.longitude);
						new GeoAddressAsyncTask(cur_Latlng.latitude, cur_Latlng.longitude).execute();
					}

				} else {
					new CustomToast(getApplicationContext(), ConstantValues.internetConnectionMsg).showToast();
				}
				
				//dialogConfirm.dismiss();
			}
		});
		
	
		
		btnExit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialogConfirm.dismiss();
			}
		});
		
		dialogConfirm.show();
	}
	
	
	private class GetRouteTask extends AsyncTask<String, Void, String> {

		private ProgressDialog Dialog;
		String response = "";
		boolean isProgress = false;

		LatLng pickUpPosition, DestinationPostion;

		public GetRouteTask(LatLng pickUpPosition, LatLng DestinationPostion) {
			this.pickUpPosition = pickUpPosition;
			this.DestinationPostion = DestinationPostion;
		}

		@Override
		protected void onPreExecute() {
			Dialog = new ProgressDialog(StartOrEndTripActivity.this);
			Dialog.setMessage("Loading...");
			Dialog.setCancelable(false);
			if(isProgress){
				Dialog.show();
			}
			
		}

		@Override
		protected String doInBackground(String... urls) {
			// Get All Route values
			try {
				doc = routeDirection.getDocument(pickUpPosition, DestinationPostion, GMapV2GetRouteDirection.MODE_DRIVING, "");
				response = "Success";
					
			} catch (Exception e) {
				//response = e.getMessage().toString();
			}
			
			return response;
			

		}

		@Override
		protected void onPostExecute(String result) {
			
			try {
				if (response.equalsIgnoreCase("Success")) {
				
					if (Dialog.isShowing()) {
						Dialog.dismiss();
						
					} else{
						
					}
					
					directionPoint = routeDirection.getDirection(doc);
					if(directionPoint != null) {
						if(isFirstTime) {
							LatLngBounds bounds = Utils.centerIncidentRouteOnMap(directionPoint);
							map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120)); 
							isFirstTime = false;
						}
						//animateMarker(currentPositionMarker, pickUpPosition, false);
						rectLine = new PolylineOptions().width(10).color(Color.RED);
						for (int i = 0; i < directionPoint.size(); i++) {
							rectLine.add(directionPoint.get(i));
						}
						// Adding route on the map
						
						Log.e("New path drawing", pickUpPosition.latitude+"::"+pickUpPosition.longitude+"___for__"+i);
						i++;
						
						Polyline newline = map.addPolyline(rectLine);
						
						if (line!= null) {
							line.remove();
						}
						line = newline;
						
					}
					
				}	

			} catch (Exception e) {
				new CustomToast(getApplicationContext(), "" + e.getMessage()).showToast();
				Log.e("Error 1038", "---" + e.getMessage());
			}
			
		}
	}
	
	@Override
	public void onBackPressed() {
		isLock = true;
		// TODO Auto-generated method stub
		//super.onBackPressed();
	}
	
	 @Override
		protected void onResume() {
			super.onResume();
		}

		@Override
		protected void onPause() {
			super.onPause();
		}
		
		
		private class GeoAddressAsyncTask extends AsyncTask<Void, Void, Void> {
			double Lat, Lon;
			List<Address> addresses = null;
			private ProgressDialog progressDialog;
			boolean isProgress = false;
			public GeoAddressAsyncTask(double Lat, double Lon) {
				this.Lat = Lat;
				this.Lon = Lon;
			}
		
			@Override
			protected void onPreExecute() {
				progressDialog = ProgressDialog.show(StartOrEndTripActivity.this, "", "Loading...", true, false);
			}

			@Override
			protected Void doInBackground(Void... arg0) {
				//new GeoAddressAsyncTask(this.Lat, this.Lon).execute();
				//googleMap.clear();
				//googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(arg0));
				try {
					addresses = gcd.getFromLocation(Lat, Lon, 1);
				
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				//try {
				try {
					if ((progressDialog != null) && progressDialog.isShowing()) {
						progressDialog.dismiss();
					}
				} catch (final IllegalArgumentException e) {
				} catch (final Exception e) {
				} finally {
					progressDialog = null;
				}
					
					String formatedAddress = "";
					if (addresses != null) {
						if (addresses.size() > 0) {
							Log.i("Value found", "________________");
							String address = addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getSubLocality() + ", " + addresses.get(0).getAdminArea();
							String addressWithoutNullValue = address.replace("null", "");
							Log.e("Address is 1", "" + addressWithoutNullValue);
							formatedAddress = addressWithoutNullValue.replace(", ,", ",");
							Log.e("Address is 2", "" + formatedAddress);
							
						} else {
							Log.i("Value is null", "______empty__________");
						}

					} else {
						Log.i("Value is null", "________________");
					}
						
					if(isHailed) {
						DriverApp.getInstance().getHailedCabInfo().setDestinationLat(Lat);
						DriverApp.getInstance().getHailedCabInfo().setDestinationLong(Lon);
						DriverApp.getInstance().getHailedCabInfo().setDestinationName(formatedAddress);
						
						LatLng pickUpLatLng = new LatLng(DriverApp.getInstance().getHailedCabInfo().getPickupLat(), DriverApp.getInstance().getHailedCabInfo().getPickupLong());
						LatLng destinationLatLng = new LatLng(DriverApp.getInstance().getHailedCabInfo().getDestinationLat(), DriverApp.getInstance().getHailedCabInfo().getDestinationLong());
						Log.e("I am here for hailed endtrip 1","-");
						
						if(InternetConnectivity.isConnectedToInternet(StartOrEndTripActivity.this)) {
							ConstantValues.METER_END_TIME = System.currentTimeMillis();
							ConstantValues.METER_END_TOTAL_SPEED_MPH = ConstantValues.METER_SPEED_MPH_TOTAL;
							double travelledHours = 0.0;
							double travelledDistance = 0.0;
							
							try {
								long final_meter_end_time = ConstantValues.METER_END_TIME - ConstantValues.TIME_LOST;
								travelledHours = Utils.getTimeTraveledInHour(ConstantValues.METER_START_TIME, final_meter_end_time);
							} catch (ParseException e1) {
								e1.printStackTrace();
								travelledHours = 0.0;
							}
							
							travelledDistance = (ConstantValues.METER_END_TOTAL_SPEED_MPH/(double)ConstantValues.METER_HIT_COUNTER) * travelledHours;
							
							if (Double.isNaN(travelledDistance)) {
								travelledDistance = 0.0;
							}
							
							double distanceInKM = travelledDistance * ConstantValues.MPH_TO_KPH_CONVERTION;
							
							String formatedDuration = String.format("%02d:%02d:%02d", 
									TimeUnit.MILLISECONDS.toHours(ConstantValues.METER_END_TIME - ConstantValues.METER_START_TIME ),
									TimeUnit.MILLISECONDS.toMinutes(ConstantValues.METER_END_TIME - ConstantValues.METER_START_TIME ) -  
									TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(ConstantValues.METER_END_TIME - ConstantValues.METER_START_TIME )), // The change is in this line
									TimeUnit.MILLISECONDS.toSeconds(ConstantValues.METER_END_TIME - ConstantValues.METER_START_TIME ) - 
									TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ConstantValues.METER_END_TIME - ConstantValues.METER_START_TIME )));  
							
							DriverApp.getInstance().getHailedCabInfo().setDuration(formatedDuration+ " h");
							DriverApp.getInstance().getHailedCabInfo().setDistance(Utils.getFormatedDistance(Double.toString(distanceInKM)));
							//DriverApp.getInstance().getBookingInfo().setFare("0.0");
							Log.e("I am here for hailed endtrip 2","-");
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
								endTrip.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "1010", DriverApp.getInstance().getDriverInfo().getTaxiId(), DriverApp.getInstance().getHailedCabInfo().getBookingId(), DriverApp.getInstance().getHailedCabInfo().getDestinationName(), Double.toString(DriverApp.getInstance().getHailedCabInfo().getDestinationLat()), Double.toString(DriverApp.getInstance().getHailedCabInfo().getDestinationLong()), DriverApp.getInstance().getHailedCabInfo().getDistance(), DriverApp.getInstance().getHailedCabInfo().getDuration(), convertArrayToString());
							} else {
								endTrip.execute("1010", DriverApp.getInstance().getDriverInfo().getTaxiId(), DriverApp.getInstance().getHailedCabInfo().getBookingId(), DriverApp.getInstance().getHailedCabInfo().getDestinationName(), Double.toString(DriverApp.getInstance().getHailedCabInfo().getDestinationLat()), Double.toString(DriverApp.getInstance().getHailedCabInfo().getDestinationLong()), DriverApp.getInstance().getHailedCabInfo().getDistance(), DriverApp.getInstance().getHailedCabInfo().getDuration(), convertArrayToString());
							}
						} else {
							//new CustomToast(getApplicationContext(), ConstantValues.internetConnectionMsg).showToast();
						}
						
						/*if(InternetConnectivity.isConnectedToInternet(StartOrEndTripActivity.this)) {
							new GetDurationAsyncTask(StartOrEndTripActivity.this, pickUpLatLng, destinationLatLng,  new OnRequestComplete() {
								@Override
								public void onRequestComplete(String result) {
									double fareAmount;
									
									try {
										Log.e("Duration is:", "::::"+result);
										String [] distanceDuration = result.split("//");
										
										DriverApp.getInstance().getHailedCabInfo().setDuration(Utils.getFormatedDuration(distanceDuration[1]));
										DriverApp.getInstance().getHailedCabInfo().setDistance(Utils.getFormatedDistance(distanceDuration[0]));
										
										if (!DriverApp.getInstance().getHailedCabInfo().getDistance().split(" ")[0].equalsIgnoreCase("null") || DriverApp.getInstance().getHailedCabInfo().getDistance().split(" ")[0] !=null) {
											fareAmount = Double.parseDouble(DriverApp.getInstance().getHailedCabInfo().getDistance().split(" ")[0]) * ConstantValues.fareValue;
										} else {
											fareAmount = 0.00;
										}
										
										Log.e("total fare is:", "::::" + fareAmount);
										DriverApp.getInstance().getHailedCabInfo().setFare(Double.toString(fareAmount));
										
											if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
												//kk
												endTrip.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"1010", DriverApp.getInstance().getDriverInfo().getTaxiId(), DriverApp.getInstance().getHailedCabInfo().getBookingId(), DriverApp.getInstance().getHailedCabInfo().getDestinationName(), Double.toString(DriverApp.getInstance().getHailedCabInfo().getDestinationLat()), Double.toString(DriverApp.getInstance().getHailedCabInfo().getDestinationLong()), DriverApp.getInstance().getHailedCabInfo().getDistance(), DriverApp.getInstance().getHailedCabInfo().getDuration());
											} else {
												endTrip.execute("1010", DriverApp.getInstance().getDriverInfo().getTaxiId(), DriverApp.getInstance().getHailedCabInfo().getBookingId(), DriverApp.getInstance().getHailedCabInfo().getDestinationName(), Double.toString(DriverApp.getInstance().getHailedCabInfo().getDestinationLat()), Double.toString(DriverApp.getInstance().getHailedCabInfo().getDestinationLong()), DriverApp.getInstance().getHailedCabInfo().getDistance(), DriverApp.getInstance().getHailedCabInfo().getDuration());
											}
									} catch (Exception e) {
										//new CustomToast(getApplicationContext(), "" + e.getMessage()).showToast();
									}
									
								}
								
							}).execute();
						} else {
							//new CustomToast(getApplicationContext(), ConstantValues.internetConnectionMsg).showToast();
						}*/ 
					} else {
						DriverApp.getInstance().getBookingInfo().setDestinationLat(Lat);
						DriverApp.getInstance().getBookingInfo().setDestinationLon(Lon);
						DriverApp.getInstance().getBookingInfo().setDestinationAddress(formatedAddress);
						
						LatLng pickUpLatLng = new LatLng(DriverApp.getInstance().getBookingInfo().getPickupLat(), DriverApp.getInstance().getBookingInfo().getPickupLon());
						LatLng destinationLatLng = new LatLng(DriverApp.getInstance().getBookingInfo().getDestinationLat(), DriverApp.getInstance().getBookingInfo().getDestinationLon());
						
						
						if(InternetConnectivity.isConnectedToInternet(StartOrEndTripActivity.this)) {
							ConstantValues.METER_END_TIME = System.currentTimeMillis();
							ConstantValues.METER_END_TOTAL_SPEED_MPH = ConstantValues.METER_SPEED_MPH_TOTAL;
							double travelledHours = 0.0;
							double travelledDistance = 0.0;
							
							try {
								long final_meter_end_time = ConstantValues.METER_END_TIME - ConstantValues.TIME_LOST;
								travelledHours = Utils.getTimeTraveledInHour(ConstantValues.METER_START_TIME, final_meter_end_time);
							} catch (ParseException e1) {
								// TODO Auto-generated catch block
								travelledHours = 0.0;
								e1.printStackTrace();
							}
							
							travelledDistance = (ConstantValues.METER_END_TOTAL_SPEED_MPH/(double)ConstantValues.METER_HIT_COUNTER) * travelledHours;
							
							if (Double.isNaN(travelledDistance)) {
								travelledDistance = 0.0;
							}
							
							Log.e("Travalled Distance", "******===----"  + travelledDistance);
							
							double distanceInKM = travelledDistance * ConstantValues.MPH_TO_KPH_CONVERTION;
							
							String formatedDuration = String.format("%02d:%02d:%02d",
									TimeUnit.MILLISECONDS.toHours(ConstantValues.METER_END_TIME - ConstantValues.METER_START_TIME ),
									TimeUnit.MILLISECONDS.toMinutes(ConstantValues.METER_END_TIME - ConstantValues.METER_START_TIME ) -  
									TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(ConstantValues.METER_END_TIME - ConstantValues.METER_START_TIME )), // The change is in this line
									TimeUnit.MILLISECONDS.toSeconds(ConstantValues.METER_END_TIME - ConstantValues.METER_START_TIME ) - 
									TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ConstantValues.METER_END_TIME - ConstantValues.METER_START_TIME )));   
							
							Log.e("Travalled Distance in KM", "******===----"  + distanceInKM);
							DriverApp.getInstance().getBookingInfo().setDuration(formatedDuration+ " h");
							DriverApp.getInstance().getBookingInfo().setDistance(Utils.getFormatedDistance(Double.toString(distanceInKM)));
							DriverApp.getInstance().getBookingInfo().setFare("0.0");
							Log.e("Travalled Distance GOT", "******===----"  + DriverApp.getInstance().getBookingInfo().getDistance());
							
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
								endTrip.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "1010", DriverApp.getInstance().getDriverInfo().getTaxiId(), DriverApp.getInstance().getBookingInfo().getBookingId(), DriverApp.getInstance().getBookingInfo().getDestinationAddress(), Double.toString(DriverApp.getInstance().getBookingInfo().getDestinationLat()), Double.toString(DriverApp.getInstance().getBookingInfo().getDestinationLon()), DriverApp.getInstance().getBookingInfo().getDistance(), DriverApp.getInstance().getBookingInfo().getDuration(), convertArrayToString());
				        	} else {
				        		endTrip.execute("1010", DriverApp.getInstance().getDriverInfo().getTaxiId(), DriverApp.getInstance().getBookingInfo().getBookingId(), DriverApp.getInstance().getBookingInfo().getDestinationAddress(), Double.toString(DriverApp.getInstance().getBookingInfo().getDestinationLat()), Double.toString(DriverApp.getInstance().getBookingInfo().getDestinationLon()), DriverApp.getInstance().getBookingInfo().getDistance(), DriverApp.getInstance().getBookingInfo().getDuration(), convertArrayToString());
							}
						} else {
							//new CustomToast(getApplicationContext(), ConstantValues.internetConnectionMsg).showToast();
						}
						
						/*
						if(InternetConnectivity.isConnectedToInternet(StartOrEndTripActivity.this)) {
							new GetDurationAsyncTask(StartOrEndTripActivity.this, pickUpLatLng, destinationLatLng,  new OnRequestComplete() {
								@Override
								public void onRequestComplete(String result) {
									double fareAmount;
									
									try {
										Log.e("Duration is:", "::::"+result);
										String [] distanceDuration = result.split("//");
										
										DriverApp.getInstance().getBookingInfo().setDuration(Utils.getFormatedDuration(distanceDuration[1]));
										DriverApp.getInstance().getBookingInfo().setDistance(Utils.getFormatedDistance(distanceDuration[0]));
										
										if (!DriverApp.getInstance().getBookingInfo().getDistance().split(" ")[0].equalsIgnoreCase("null") || DriverApp.getInstance().getBookingInfo().getDistance().split(" ")[0] != null) {
											fareAmount = Double.parseDouble(DriverApp.getInstance().getBookingInfo().getDistance().split(" ")[0]) * ConstantValues.fareValue;
										} else {
											fareAmount = 0.00;
										}
										
										Log.e("total fare is:", "::::" + fareAmount);
										DriverApp.getInstance().getBookingInfo().setFare(Double.toString(fareAmount));
										
										if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
											endTrip.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "1010", DriverApp.getInstance().getDriverInfo().getTaxiId(), DriverApp.getInstance().getBookingInfo().getBookingId(), DriverApp.getInstance().getBookingInfo().getDestinationAddress(), Double.toString(DriverApp.getInstance().getBookingInfo().getDestinationLat()), Double.toString(DriverApp.getInstance().getBookingInfo().getDestinationLon()), DriverApp.getInstance().getBookingInfo().getDistance(), DriverApp.getInstance().getBookingInfo().getDuration());
							        	} else {
							        		endTrip.execute("1010", DriverApp.getInstance().getDriverInfo().getTaxiId(), DriverApp.getInstance().getBookingInfo().getBookingId(), DriverApp.getInstance().getBookingInfo().getDestinationAddress(), Double.toString(DriverApp.getInstance().getBookingInfo().getDestinationLat()), Double.toString(DriverApp.getInstance().getBookingInfo().getDestinationLon()), DriverApp.getInstance().getBookingInfo().getDistance(), DriverApp.getInstance().getBookingInfo().getDuration());
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
									
								}
								
							}).execute();
							
						} else {
							//new CustomToast(getApplicationContext(), ConstantValues.internetConnectionMsg).showToast();
						}*/
					}

				/*} catch (Exception e) {
					new CustomToast(getApplicationContext(), "" + e.getMessage()).showToast();
					Log.e("Error 1040", "---" + e.getMessage());
				}*/
			}
		}
		
		
		public void animateMarker(final Marker marker, final LatLng toPosition, final boolean hideMarker) {
		     final Handler handler = new Handler();
		     final long start = SystemClock.uptimeMillis();
		     com.google.android.gms.maps.Projection proj = map.getProjection();
		     Point startPoint = proj.toScreenLocation(marker.getPosition());
		     final LatLng startLatLng = proj.fromScreenLocation(startPoint);
		     final long duration = 500;
		     final Interpolator interpolator = new LinearInterpolator();
		
		     handler.post(new Runnable() {
		      long elapsed;
		      float t;
		      float v;
		         @Override
		         public void run() {
		             elapsed = SystemClock.uptimeMillis() - start;
		             t = interpolator.getInterpolation((float) elapsed/ duration);
		             v = interpolator.getInterpolation(t);
		             double lng = t * toPosition.longitude + (1 - t) * startLatLng.longitude;
		             double lat = t * toPosition.latitude + (1 - t) * startLatLng.latitude;
		             marker.setPosition(new LatLng(lat, lng));
		             //marker.setPosition();
		             if (t < 1.0) {
		         // Post again 10ms later.
		                 handler.postDelayed(this, 2);
		             } else {
		                 if (hideMarker) {
		                     marker.setVisible(false);
		                     
		                 } else {
		                     marker.setVisible(true);
		                 }
		             }
		         }
		     });
		     
		}
		
		public String convertArrayToString() {
			return TextUtils.join(":", taxiPathArray).toString();
		}

		@Override
		public void onLocationChanged(Location location) {
			double latitude = location.getLatitude();
	        double longitude = location.getLongitude();
	        
	       // dialogNavigation.findViewById(R.id.txt_view_distance);
	        if(dialogNavigation != null) {
		        if(dialogNavigation.isShowing()) {
		        	getRunTimeDistanceAndSpeed(txtViewDistance, txtViewSpeed);
		        	dialogNavigation.onContentChanged();
		        }
	        }
	 
	        // Creating a LatLng object for the current location
	        curenLatLng = new LatLng(latitude, longitude);
	        if (isHailed) {
	        	if(buttonIndex != 0) {
		        	taxiPathArray.add(longitude+","+latitude);
	        	}
			} else {
				if(buttonIndex == 1){
					taxiPathArray.add(longitude+","+latitude);
				}
			}

	        // Zoom in the Google Map
	        animateMarker(currentPositionMarker, curenLatLng, false);
	        map.animateCamera(CameraUpdateFactory.zoomTo(map.getCameraPosition().zoom));
	        if(InternetConnectivity.isConnectedToInternet(StartOrEndTripActivity.this)) {
	        	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	        		//*new GetRouteTask(curenLatLng, destinationLatLng).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	        	}
	        	else {
	        		//*new GetRouteTask(curenLatLng, destinationLatLng).execute();	
	        	}
	        } else {
				new CustomToast(getApplicationContext(), ConstantValues.internetConnectionMsg).showToast();
			}
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}
		
		/*private void showTimer(final TextView textView) {
			countDownTimer = new CountDownTimer(10000, 1000) {
				
				@Override
				public void onTick(long millisUntilFinished) {
					// TODO Auto-generated method stub
					
					
					if(dialogNavigation != null) {
				        if(dialogNavigation.isShowing()) {
				        	textView.setText(""+millisUntilFinished);
				        	///dialogNavigation.onContentChanged();
				        }
			        }
				}
				
				@Override
				public void onFinish() {
					// TODO Auto-generated method stub
					
				}
			};
			
			countDownTimer.start();
		}*/

}
