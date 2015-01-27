package com.netcabs.driver;

import java.io.File;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.netcabs.asynctask.LoginAsyncTask;
import com.netcabs.customviews.CustomToast;
import com.netcabs.interfacecallback.OnRequestComplete;
import com.netcabs.internetconnection.InternetConnectivity;
import com.netcabs.obdmonitor.BluetoothChatService;
import com.netcabs.utils.ConstantValues;
import com.netcabs.utils.DriverApp;

public class LoginActivity extends Activity implements OnClickListener {
	
	public static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int REQUEST_ENABLE_BT = 3;
	private static BluetoothChatService mChatService = null;
	private BluetoothAdapter mBluetoothAdapter = null;
	
	private String mConnectedDeviceName = null;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final String DEVICE_NAME = "device_name";
	
	private int message_number = 0;
	private static StringBuffer mOutStringBuffer;
	
	private TextView txtViewMPH;
	private TextView txtViewDistance;
	static Activity thisActivity = null;
	
	private Button btnLogin;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_login);
		thisActivity = this; 
		
		initViews();
		setListener();
		loadData();
	}
	
	//Device related module
	private void initViews() {
		 txtViewMPH = (TextView) findViewById(R.id.txt_view_mph);
		 txtViewDistance = (TextView) findViewById(R.id.txt_view_distance);
		 
		 btnLogin = (Button) findViewById(R.id.btn_finger_print);
	}

	private void setListener() {
		btnLogin.setOnClickListener(this);
		((Button) findViewById(R.id.btn_settings)).setOnClickListener(this);
	}
	
	//Device related module
	private void loadData() {
		//mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		 mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		 mOutStringBuffer = new StringBuffer("");

        // If the adapter is null, then Bluetooth is not supported
        /*if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }*/
	}
	

	//Device related module
	@Override
	protected void onStart() {
		/*if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			if (mChatService == null) {
				 mChatService = new BluetoothChatService(this, mHandler);
			}
		}*/
		
		if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the chat session
        } else {
            if (mChatService == null) { 
            	mChatService = new BluetoothChatService(this, mHandler);
                // Initialize the buffer for outgoing messages
            }
        	
        }
		
		super.onStart();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_finger_print:
			if(InternetConnectivity.isConnectedToInternet(LoginActivity.this)) {
				new LoginAsyncTask(LoginActivity.this, new OnRequestComplete() {
					
					@Override
					public void onRequestComplete(String result) {
						
						try {
							if("2001".equals(result)) {
								//finish();
								startActivity(new Intent(LoginActivity.this, FAQActivity.class));
							} else {
								new CustomToast(LoginActivity.this, "Data not found").showToast();
							}
						} catch (Exception e) {
							new CustomToast(LoginActivity.this, "" + e.getMessage()).showToast();
						}
						
						
					}
				}).execute("1001", DriverApp.getInstance().getDriverInfo().getFingurePrint(), DriverApp.getInstance().getDriverInfo().getTaxiId());
			} else {
				new CustomToast(LoginActivity.this, ConstantValues.internetConnectionMsg).showToast();
			}
			
			break;
			
		case R.id.btn_settings:
			Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
			break;

		default:
			break;
		}
	}
	
	/****** ODB Meter integration *******/
	//Device related module
	int prev_intake = 0;
 	int prev_load = 0;
 	int prev_coolant = 0;
 	int prev_MPH = 0;
 	int prev_RPM = 0;
 	int prev_voltage = 0;
	
	private final Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			
			String dataRecieved;
        	int value = 0;
        	int value2 = 0;
        	int PID = 0;
			
        	//Toast.makeText(LoginActivity.this, "CASE " + msg.what, Toast.LENGTH_SHORT).show();
        	
			switch (msg.what) {
				case MESSAGE_STATE_CHANGE:
					Log.i("LOGIN ACTIVITY", "MESSAGE_STATE_CHANGE: " + msg.arg1);
					switch (msg.arg1) {
						case BluetoothChatService.STATE_CONNECTED:
		                    setStatus("Connected to " + mConnectedDeviceName);
		                    //btnLogin.setClickable(true);
		                   // btnLogin.setAlpha(1.0f);
		                    //startTransmission(); 
		                    //mConversationArrayAdapter.clear();
		                break;
		                case BluetoothChatService.STATE_CONNECTING:
		                	//btnLogin.setClickable(false);
		                    //btnLogin.setAlpha(0.5f);
		                    setStatus("Connecting");
		                break;
		                case BluetoothChatService.STATE_LISTEN:
		                case BluetoothChatService.STATE_NONE:
		                	//btnLogin.setClickable(false);
		                   // btnLogin.setAlpha(0.5f);
		                    setStatus("Not connected");
		                break;
					}
				break;
				case MESSAGE_DEVICE_NAME:
	                // save the connected device's name
	                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
	            break;
				case MESSAGE_WRITE:
	                byte[] writeBuf = (byte[]) msg.obj;
	                // construct a string from the buffer
	                String writeMessage = new String(writeBuf);
	            break;
				case MESSAGE_READ:
					//Toast.makeText(LoginActivity.this, "DATA IS READING", Toast.LENGTH_SHORT).show();
					Log.e("DATA IS SENDING", "SENDING.... OK");
					
					byte[] readBuf = (byte[]) msg.obj;
					// construct a string from the valid bytes in the buffer
					String readMessage = new String(readBuf, 0, msg.arg1);
	
					// ------- ADDED CODE FOR OBD -------- //
					dataRecieved = readMessage;
	
					// RX.setText(dataRecieved);
	
					if ((dataRecieved != null) && (dataRecieved.matches("\\s*[0-9A-Fa-f]{2} [0-9A-Fa-f]{2}\\s*\r?\n?"))) {
	
						dataRecieved = dataRecieved.trim();
						String[] bytes = dataRecieved.split(" ");
	
						if ((bytes[0] != null) && (bytes[1] != null)) {
	
							PID = Integer.parseInt(bytes[0].trim(), 16);
							value = Integer.parseInt(bytes[1].trim(), 16);
							Log.e("GET PID", "PID 1 " + PID);
							
							Toast.makeText(LoginActivity.this, "1 PID " + PID, Toast.LENGTH_SHORT).show();
						}
	
						switch (PID) {
	
							case 13:// PID(0D): MPH
		
								value = (value * 5) / 8; // convert KPH to MPH
								int needle_value = ((value * 21) / 20) - 85;
		
								if (prev_MPH == 0) {
									prev_MPH = needle_value;
								} else {
									prev_MPH = needle_value;
								}
		
								String displayMPH = String.valueOf(value);
								// MPH.setText(displayMPH);
								txtViewMPH.setText("" + displayMPH);
								ConstantValues.CURRENT_SPEED = displayMPH+"";
								
								if (value >= 1) {
									ConstantValues.METER_HIT_COUNTER += 1;
									ConstantValues.METER_SPEED_MPH_TOTAL += value;
									ConstantValues.UPDATE_TIME = System.currentTimeMillis();
									
								} else {
									ConstantValues.TIME_LOST += System.currentTimeMillis() - ConstantValues.UPDATE_TIME;
								}
								
								Log.e("SPEED MPH 1", "*********1**** " + displayMPH);
								Toast.makeText(thisActivity, "FIRST MPH : " + value, Toast.LENGTH_LONG).show();
								break;
								
							case 31:// PID(0D): MPH
								
								/*value = (value * 5) / 8; // convert KPH to MPH
								int needle_value = ((value * 21) / 20) - 85;
		
								if (prev_MPH == 0) {
									prev_MPH = needle_value;
								} else {
									prev_MPH = needle_value;
								}
		
								String displayMPH = String.valueOf(value);*/
								// MPH.setText(displayMPH);
								//txtViewMPH.setText("" + value);
								
								Log.e("Distance", "*********1**** " + value);
								break;
		
							default:
								;
	
						}
	
					} else if ((dataRecieved != null) && (dataRecieved.matches("\\s*[0-9A-Fa-f]{1,2} [0-9A-Fa-f]{2} [0-9A-Fa-f]{2}\\s*\r?\n?"))) {
	
						dataRecieved = dataRecieved.trim();
						String[] bytes = dataRecieved.split(" ");
	
						if ((bytes[0] != null) && (bytes[1] != null) && (bytes[2] != null)) {
	
							PID = Integer.parseInt(bytes[0].trim(), 16);
							value = Integer.parseInt(bytes[1].trim(), 16);
							value2 = Integer.parseInt(bytes[2].trim(), 16);
							Log.e("PID BLOCK", "PID 2");
							//Toast.makeText(LoginActivity.this, "2 PID " + PID, Toast.LENGTH_SHORT).show();
						}
	
						// PID(0C): RPM
						if (PID == 12) {
	
							int RPM_value = ((value * 256) + value2) / 4;
							int needle_value = ((RPM_value * 22) / 1000) - 85;
	
							if (prev_RPM == 0) {
								prev_RPM = needle_value;
							} else {
								prev_RPM = needle_value;
							}
	
							String displayRPM = String.valueOf(RPM_value);
							
							//txtViewDistance.setText("" + displayRPM);
							Log.e("SPEED MPH 1", "*********1**** " + displayRPM);
							
							// RPM.setText(displayRPM);
	
						} else if ((PID == 1) || (PID == 65)) {
	
							switch (value) {
	
							case 13:// PID(0D): MPH
	
								value2 = (value2 * 5) / 8; // convert to MPH
								int needle_value = ((value2 * 21) / 20) - 85;
	
								if (prev_MPH == 0) {
									prev_MPH = needle_value;
								} else {
									prev_MPH = needle_value;
								}
	
								String displayMPH = String.valueOf(value2);
								
								txtViewMPH.setText("" + displayMPH);
								ConstantValues.CURRENT_SPEED = displayMPH+"";
								
								//Summation of meter speed as MPH
								
								if (value2 >= 1) {
									ConstantValues.METER_HIT_COUNTER += 1;
									ConstantValues.METER_SPEED_MPH_TOTAL += value2;
									ConstantValues.UPDATE_TIME = System.currentTimeMillis();
								} else {
									ConstantValues.TIME_LOST += System.currentTimeMillis() - ConstantValues.UPDATE_TIME;
								}
								
								Log.e("SPEED MPH2 ", "****2***** " + displayMPH);
								Toast.makeText(thisActivity, "SECOND MPH : " + value2, Toast.LENGTH_LONG).show();
								
								// MPH.setText(displayMPH);
								break;
	
							default:
								;
							}
							
						} else if (PID == 31) {
	
							switch (value) {
	
							case 49:// Distance PID(0D): MPH
								
								Double miles = (((value*256)+value2)*0.62137);
								String displayDistance = String.valueOf("Miles: " + miles); 
								
								txtViewDistance.setText("" + displayDistance);
								Log.e("Meter Distance ", "****2***** " + displayDistance);
								// MPH.setText(displayMPH);
								break;
	
							default:
								;
							}
						}
	
					} else if ((dataRecieved != null) && (dataRecieved.matches("\\s*[0-9]+(\\.[0-9]?)?V\\s*\r*\n*"))) {
	
						dataRecieved = dataRecieved.trim();
						String volt_number = dataRecieved.substring(0, dataRecieved.length() - 1);
						double needle_value = Double.parseDouble(volt_number);
						needle_value = (((needle_value - 11) * 21) / 0.5) - 100;
						int volt_value = (int) (needle_value);
	
						if (prev_voltage == 0) {
							prev_voltage = volt_value;
						} else {
							prev_voltage = volt_value;
						}
						// voltage.setText(dataRecieved);
	
					} else if ((dataRecieved != null) && (dataRecieved.matches("\\s*[0-9]+(\\.[0-9]?)?V\\s*V\\s*>\\s*\r*\n*"))) {
	
						dataRecieved = dataRecieved.trim();
						String volt_number = dataRecieved.substring(0, dataRecieved.length() - 1);
						double needle_value = Double.parseDouble(volt_number);
						needle_value = (((needle_value - 11) * 21) / 0.5) - 100;
						int volt_value = (int) (needle_value);
	
						if (prev_voltage == 0) {
							prev_voltage = volt_value;
						} else {
							prev_voltage = volt_value;
						}
	
						// voltage.setText(dataRecieved);
	
					} else if((dataRecieved != null) && (dataRecieved.matches("\\s*[ .A-Za-z0-9\\?*>\r\n]*\\s*>\\s*\r*\n*" ))) {
		            	
						if(message_number == 3) {
							message_number = 1;
						}
		            	
						getData(message_number++);
		            	   
		            } else {
	
						;
					}
				break;
	                
			}

		}
	};
	
	//Device related module
	public static void startTransmission() {
		ConstantValues.METER_START_TIME = System.currentTimeMillis();
		Log.e("TRANSMISSION START", "SENDING....");
		//Toast.makeText(thisActivity, "Sending...", Toast.LENGTH_SHORT).show();
		sendMessage("01 00" + '\r');

	}
	
	//Device related module
	private static void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(thisActivity, "Please connect the OBD meter", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            //mOutEditText.setText(mOutStringBuffer);
        }
    }
	
	//Device related module
	public void getData(int messagenumber) {
		
		//final TextView TX = (TextView) findViewById(R.id.TXView2); 
		
		switch(messagenumber) {
    	
        	case 1:
        		sendMessage("01 0C" + '\r'); //get RPM
        		//TX.setText("01 0C");
        		messagenumber++;
        		break;
        		
        	case 2:
        		sendMessage("01 0D" + '\r'); //get MPH
        		//TX.setText("01 0D");
        		messagenumber++;
        		break;
        	case 3:
        		sendMessage("01 31" + '\r'); //Distance command
        		//sendMessage("01 04" + '\r'); //get Engine Load
        		//TX.setText("01 04");
        		messagenumber++;
        		break;
        	case 4:
        		sendMessage("01 05" + '\r'); //get Coolant Temperature
        		//TX.setText("01 05");
        		messagenumber++;
        		break;
        	case 5:
        		sendMessage("01 0F" + '\r'); //get Intake Temperature
        		//TX.setText("01 0F");
        		messagenumber++;
        		break;
        	
        	case 6:
        		sendMessage("AT RV" + '\r'); //get Voltage
        		//TX.setText("AT RV");
        		messagenumber++;
        		break;
        		
        	default: ; 		 
		}
    }
	
	//Device related module
	@Override
	protected void onResume() {
		ConstantValues.isBack = false;
		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
				
			}
		}
		
		/*try {
			Intent intent = new Intent(LoginActivity.this, com.netcabs.services.SpeedGettingService.class);
			startService(intent);
			
		} catch (Exception e) {
			
		}*/
		
		super.onResume();
	}
	
	
	//Device related module
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
		
		case REQUEST_CONNECT_DEVICE_SECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data, true);
            }
            break;
        case REQUEST_CONNECT_DEVICE_INSECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data, false);
            }
            break;

		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				//Bluetooth is now enabled, so set up a chat session
				if (mChatService == null) {
					mChatService = new BluetoothChatService(this, mHandler);

				} else {
					// User did not enable Bluetooth or an error occurred
					Log.d("Blutooth no enable", "BT not enabled");
					//Toast.makeText(this, "Blutooth not enable", Toast.LENGTH_SHORT).show();
					finish();
				}
			}
		}
	}
	
	//Device related module
	private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }
	
	//Device related module
	private final void setStatus(CharSequence subTitle) {
		((TextView) findViewById(R.id.txt_view_connection_status)).setText(subTitle);
    }
	
	@Override
	protected void onDestroy() {
		try {
    		//unregisterReceiver(broadcastReceiver);
    		stopService(new Intent(LoginActivity.this, com.netcabs.services.ServiceTaxiLocationUpdate.class));
    	} catch (Exception e) {
    		Log.i("Service Stop Exception Taxi update","****" + e.getMessage());
    	}
		
		try {
			if (mChatService != null) {
	        	mChatService.stop();
	        }
			
		} catch (Exception e) {
			
		}
        
        Log.e("Driver", "--- ON DESTROY ---");
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		System.exit(0);
		super.onBackPressed();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	public void trimCache() {
		   try {
		     File dir = getCacheDir();
		     if (dir != null && dir.isDirectory()) {
		     }
		  } catch (Exception e) {
		     // TODO: handle exception
		  }
	}

}
