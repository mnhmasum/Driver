package com.netcabs.jsonparser;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.netcabs.datamodel.BookingInfo;
import com.netcabs.datamodel.CreditCardInfo;
import com.netcabs.datamodel.DriverInfo;
import com.netcabs.datamodel.FaqInfo;
import com.netcabs.datamodel.FastTripsInfo;
import com.netcabs.datamodel.HailedCabInfo;
import com.netcabs.datamodel.JourneyReportInfo;
import com.netcabs.datamodel.PaymentsInfo;
import com.netcabs.datamodel.ProfileInfo;
import com.netcabs.datamodel.UserInfo;
import com.netcabs.utils.ConstantValues;
import com.netcabs.utils.DriverApp;
import com.netcabs.utils.Utils;

public class CommunicationLayer {
	
	//API #25
	public static String getPaymentMethodList(String func_id) throws Exception {
		List<NameValuePair> postData = new ArrayList<NameValuePair>(1);
		postData.add((NameValuePair) new BasicNameValuePair("func_id", func_id));

		String serverResponse = WebServerOperation.sendHTTPPostRequestToServer(DriverApp.getInstance().getBaseUrl(), postData, true);
		return parsePaymentMethodList(serverResponse);
	}

	private static String parsePaymentMethodList(String jData) throws JSONException {
		String str = "";
		JSONArray jsonArray = new JSONArray(jData);
		ArrayList<PaymentsInfo> list = new ArrayList<PaymentsInfo>();
		if(jsonArray != null && jsonArray.length() > 0) {
			for(int i = 0; i < jsonArray.length(); i++) {
				str = "2001";
				PaymentsInfo info = new PaymentsInfo();
				
				JSONObject dataObj = jsonArray.getJSONObject(i);
				info.setMethodName(dataObj.getString("method_name"));
				
				JSONObject idObj = dataObj.getJSONObject("_id");
				info.setId(idObj.optString("$oid"));
				
				list.add(info);
			}
			
			DriverApp.getInstance().setPaymentsInfoList(list);
		} else {
			str = "1001";
			DriverApp.getInstance().setPaymentsInfoList(null);
		}
		return str;
	}

	//API #1001 ...Driver Login API
	public static String getLoginData(String func_id, String fingure_print, String taxi_id) throws Exception {
		List<NameValuePair> postData = new ArrayList<NameValuePair>(3);
		postData.add((NameValuePair) new BasicNameValuePair("func_id", func_id));
		postData.add((NameValuePair) new BasicNameValuePair("fingure_print", fingure_print));
		postData.add((NameValuePair) new BasicNameValuePair("taxi_id", taxi_id));

		Log.e("Login with", postData+"");
		String serverResponse = WebServerOperation.sendHTTPPostRequestToServer(DriverApp.getInstance().getBaseUrl(), postData, true);
		Log.i("Server response for login", "____________"+ serverResponse);
		return parseLoginData(serverResponse);
	}

	private static String parseLoginData(String jData) throws JSONException {
		JSONObject jDataObj = new JSONObject(jData);
		
		if("2001".equals(jDataObj.getString("status_code"))) {
			ProfileInfo info = new ProfileInfo();
			info.setProfilePic(jDataObj.getString("profile_pic"));
			info.setDrivingLicenseImage(jDataObj.getString("driving_license_image"));
			info.setNidImage(jDataObj.getString("nid_image"));
			info.setLastName(jDataObj.getString("last_name"));
			info.setMobileNumber(jDataObj.getString("mobile_no"));
			info.setId(jDataObj.getString("id"));
			info.setFirstName(jDataObj.getString("first_name"));
			info.setEmail(jDataObj.getString("email"));
			info.setIsMobileVerified(jDataObj.getInt("is_mobile_verified"));
			info.setIsEmailVerified(jDataObj.getInt("is_email_verified"));
			
			DriverApp.getInstance().setProfileInfo(info);
		}
		
		return jDataObj.getString("status_code");
	}
	
	//API #1002 Fit to Drive
	public static String getFitToDriveData(String func_id, String taxi_id) throws Exception {
		List<NameValuePair> postData = new ArrayList<NameValuePair>(2);
		postData.add((NameValuePair) new BasicNameValuePair("func_id", func_id));
		postData.add((NameValuePair) new BasicNameValuePair("taxi_id", taxi_id));

		String serverResponse = WebServerOperation.sendHTTPPostRequestToServer(DriverApp.getInstance().getBaseUrl(), postData, true);
		Log.i("server response for Fit to Drive ", "___________"+serverResponse);
		return parseFitToDriveData(serverResponse);
	}

	private static String parseFitToDriveData(String jData) throws JSONException {
		JSONObject jDataObj = new JSONObject(jData);
		
		if("2001".equals(jDataObj.getString("status_code"))) {
			FaqInfo info = new FaqInfo();
			info.setAnswerOne(jDataObj.getJSONObject("data").getJSONObject("answer1").getString("answer"));
			info.setAnswerTwo(jDataObj.getJSONObject("data").getJSONObject("answer2").getString("answer"));
			info.setAnswetThree(jDataObj.getJSONObject("data").getJSONObject("answer3").getString("answer"));
			info.setQuestion(jDataObj.getJSONObject("data").getString("question"));
			info.setRightAnswer(Integer.toString(jDataObj.getJSONObject("data").getInt("right_index")));
			
			DriverApp.getInstance().setFaqInfo(info);
		}
		return jDataObj.getString("status_code");
	}
	
	//API #1003 Check availability
	public static String getCheckAvailabilityData(String func_id, String taxi_id, String is_available) throws Exception {
		List<NameValuePair> postData = new ArrayList<NameValuePair>(3);
		postData.add((NameValuePair) new BasicNameValuePair("func_id", func_id));
		postData.add((NameValuePair) new BasicNameValuePair("taxi_id", taxi_id));
		postData.add((NameValuePair) new BasicNameValuePair("is_available", is_available));

		String serverResponse = WebServerOperation.sendHTTPPostRequestToServer(DriverApp.getInstance().getBaseUrl(), postData, true);
		Log.i("server response for Check Availability", "__________" + serverResponse);
		return parseCheckAvailabilityData(serverResponse);
	}

	private static String parseCheckAvailabilityData(String jData) throws JSONException {
		JSONObject jDataObj = new JSONObject(jData);
		return jDataObj.getString("status_code");
	}
	
	//API #1004 Booking request
	public static String getBookingRequestData(String func_id, String taxi_id) throws Exception {
		List<NameValuePair> postData = new ArrayList<NameValuePair>(2);
		Log.i("taxi id is", "__________"+taxi_id);
		postData.add((NameValuePair) new BasicNameValuePair("func_id", func_id));
		postData.add((NameValuePair) new BasicNameValuePair("taxi_id", taxi_id));
		Log.i("server request for Booking Request", "__________"+postData);
		String serverResponse = WebServerOperation.sendHTTPPostRequestToServer(DriverApp.getInstance().getBaseUrl(), postData, true);
		Log.i("server response for Booking Request", "__________"+serverResponse);
		return parseBookingRequestData(serverResponse);
	}

	private static String parseBookingRequestData(String jData) throws JSONException {
		JSONObject jDataObj = new JSONObject(jData);
		
		if("2001".equals(jDataObj.getString("status_code"))) {
			JSONObject jsonDataInfo = jDataObj.getJSONObject("Data");
			BookingInfo bookingInfo = new BookingInfo();
			
			bookingInfo.setFirstName(jsonDataInfo.getString("first_name"));
			bookingInfo.setLastName(jsonDataInfo.getString("last_name"));
			bookingInfo.setUserId(jsonDataInfo.getString("user_id"));
			bookingInfo.setPaymentMethod(jsonDataInfo.getString("payment_method"));
			bookingInfo.setPickupLat(jsonDataInfo.getJSONObject("pickup_loc").getJSONArray("coordinates").getDouble(1));
			bookingInfo.setPickupLon(jsonDataInfo.getJSONObject("pickup_loc").getJSONArray("coordinates").getDouble(0));
			bookingInfo.setBookingTime(jsonDataInfo.getString("booking_time"));
			bookingInfo.setPassengerNumber(jsonDataInfo.getInt("passenger_num"));
			bookingInfo.setBookingId(jsonDataInfo.getString("booking_id"));
			bookingInfo.setPickupAddress(jsonDataInfo.getString("pickup_address"));
			bookingInfo.setDestinationLat(jsonDataInfo.getJSONObject("destination_loc").getJSONArray("coordinates").getDouble(1));
			bookingInfo.setDestinationLon(jsonDataInfo.getJSONObject("destination_loc").getJSONArray("coordinates").getDouble(0));
			bookingInfo.setBookingDate(jsonDataInfo.getString("booking_date"));
			bookingInfo.setBookingTime(jsonDataInfo.getString("booking_time"));
			bookingInfo.setDestinationAddress(jsonDataInfo.getString("destination_address"));
			bookingInfo.setPassengerEmailId(jsonDataInfo.getString("email"));
			bookingInfo.setBookingVia(jsonDataInfo.getInt("booking_via"));
			
			//jsonDataInfo.getJSONObject("pickup_location").getJSONArray("coordinates").get(0);
			//jsonDataInfo.getJSONObject("pickup_location").getJSONArray("coordinates").get(1);
			
			DriverApp.getInstance().setBookingInfo(bookingInfo);
		}
		
		return jDataObj.getString("status_code");
	}
	
	//API #1005 Booking Reject
	public static String getBookingRejectData(String func_id, String taxi_id, String booking_id, String driver_id, String is_confirmed) throws Exception {
		List<NameValuePair> postData = new ArrayList<NameValuePair>(5);
		postData.add((NameValuePair) new BasicNameValuePair("func_id", func_id));
		postData.add((NameValuePair) new BasicNameValuePair("taxi_id", taxi_id));
		postData.add((NameValuePair) new BasicNameValuePair("booking_id", booking_id));
		postData.add((NameValuePair) new BasicNameValuePair("driver_id", driver_id));
		postData.add((NameValuePair) new BasicNameValuePair("is_confirmed", is_confirmed));

		Log.e("api hit values for booking reject", ""+postData);
		String serverResponse = WebServerOperation.sendHTTPPostRequestToServer(DriverApp.getInstance().getBaseUrl(), postData, true);
		Log.i("server response for Booking reject", "__________"+serverResponse);
		return parseBookingRejectData(serverResponse);
	}

	private static String parseBookingRejectData(String jData) throws JSONException {

		JSONObject jDataObj = new JSONObject(jData);
		return jDataObj.getString("status_code");
	}
	
	//API #1006 Booking Cancel
	public static String getBookingCancelData(String func_id, String taxi_id, String booking_id, String reason) throws Exception {
		List<NameValuePair> postData = new ArrayList<NameValuePair>(4);
		postData.add((NameValuePair) new BasicNameValuePair("func_id", func_id));
		postData.add((NameValuePair) new BasicNameValuePair("taxi_id", taxi_id));
		postData.add((NameValuePair) new BasicNameValuePair("booking_id", booking_id));
		postData.add((NameValuePair) new BasicNameValuePair("reason", reason));

		String serverResponse = WebServerOperation.sendHTTPPostRequestToServer(DriverApp.getInstance().getBaseUrl(), postData, true);
		Log.i("server response for Booking cancel", "__________"+serverResponse);
		return parseBookingCancelData(serverResponse);
	}

	private static String parseBookingCancelData(String jData) throws JSONException {
		JSONObject jDataObj = new JSONObject(jData);
		return jDataObj.getString("status_code");
	}
	
	
	//API #1007 Taxi Arriving
	public static String getTaxiArrivingData(String func_id, String taxi_id, String booking_id) throws Exception {
		List<NameValuePair> postData = new ArrayList<NameValuePair>(3);
		postData.add((NameValuePair) new BasicNameValuePair("func_id", func_id));
		postData.add((NameValuePair) new BasicNameValuePair("taxi_id", taxi_id));
		postData.add((NameValuePair) new BasicNameValuePair("booking_id", booking_id));
		
		Log.i("server request for Taxi Arriving", "__________"+postData);
		String serverResponse = WebServerOperation.sendHTTPPostRequestToServer(DriverApp.getInstance().getBaseUrl(), postData, true);
		Log.i("server response for Taxi Arriving", "__________"+serverResponse);
		return parseTaxiArrivingData(serverResponse);
	}

	private static String parseTaxiArrivingData(String jData) throws JSONException {
		JSONObject jDataObj = new JSONObject(jData);
		return jDataObj.getString("status_code");
	}
	
	
	//API #1008 Two minute to Arrive
	public static String getTwoMinuteToArrive(String func_id, String taxi_id, String booking_id) throws Exception {
		List<NameValuePair> postData = new ArrayList<NameValuePair>(3);
		postData.add((NameValuePair) new BasicNameValuePair("func_id", func_id));
		postData.add((NameValuePair) new BasicNameValuePair("taxi_id", taxi_id));
		postData.add((NameValuePair) new BasicNameValuePair("booking_id", booking_id));

		String serverResponse = WebServerOperation.sendHTTPPostRequestToServer(DriverApp.getInstance().getBaseUrl(), postData, true);
		Log.i("server response for Two minute to arrive", "__________" + serverResponse);
		return parseTwoMinuteToArrive(serverResponse);
	}

	private static String parseTwoMinuteToArrive(String jData) throws JSONException {
		JSONObject jDataObj = new JSONObject(jData);
		return Integer.toString(jDataObj.getInt("status_code"));
	}
	
	
	//API #1009 Start Trip
	public static String getStartTripData(String func_id, String taxi_id, String booking_id) throws Exception {
		List<NameValuePair> postData = new ArrayList<NameValuePair>(3);
		postData.add((NameValuePair) new BasicNameValuePair("func_id", func_id));
		postData.add((NameValuePair) new BasicNameValuePair("taxi_id", taxi_id));
		postData.add((NameValuePair) new BasicNameValuePair("booking_id", booking_id));
		
		Log.e("start trip api hit with values", ""+postData);
		String serverResponse = WebServerOperation.sendHTTPPostRequestToServer(DriverApp.getInstance().getBaseUrl(), postData, true);
		Log.i("server response for Start Trip", "__________" + serverResponse);
		return parseStartTripData(serverResponse);
	}

	private static String parseStartTripData(String jData) throws JSONException {
		JSONObject jDataObj = new JSONObject(jData);
		return jDataObj.getString("status_code");
	}
	
	//API #1010 End Trip
	public static String getEndTripData(String func_id, String taxi_id, String booking_id, String drop_off_address_name, String drop_off_address_lat, String drop_off_address_long, String total_distance, String total_time, String  journey_coordinates) throws Exception {
		List<NameValuePair> postData = new ArrayList<NameValuePair>(9);
		postData.add((NameValuePair) new BasicNameValuePair("func_id", func_id));
		postData.add((NameValuePair) new BasicNameValuePair("taxi_id", taxi_id));
		postData.add((NameValuePair) new BasicNameValuePair("booking_id", booking_id));
		postData.add((NameValuePair) new BasicNameValuePair("drop_off_address_name", drop_off_address_name));
		postData.add((NameValuePair) new BasicNameValuePair("drop_off_address_lat", drop_off_address_lat));
		postData.add((NameValuePair) new BasicNameValuePair("drop_off_address_long", drop_off_address_long));
		postData.add((NameValuePair) new BasicNameValuePair("total_distance", total_distance));
		postData.add((NameValuePair) new BasicNameValuePair("total_time", total_time));
		postData.add((NameValuePair) new BasicNameValuePair("journey_coordinates", journey_coordinates));
	
		
		Log.e("api hit for end trip with values", postData+"");
		String serverResponse = WebServerOperation.sendHTTPPostRequestToServer(DriverApp.getInstance().getBaseUrl(), postData, true);
		Log.i("server response for End Trip", "__________" + serverResponse);
		return parseEndTripData(serverResponse);
	}

	private static String parseEndTripData(String jData) throws JSONException {
		JSONObject jDataObj = new JSONObject(jData);
		return jDataObj.getString("status_code");
	}
	
	//API #1011 Payment
	public static String getPaymentData(String func_id, String taxi_id, String driver_id, String passenger_id, String booking_id, String price,String fare_price, String gst, String extras, String payment_method, String date, String time, String cardNumber, String cvvNumber, String expiredDate, String zipCode, String cardType, String countryId, String cardHolderName) throws Exception {
		
		
		List<NameValuePair> postData = new ArrayList<NameValuePair>(19);
		postData.add((NameValuePair) new BasicNameValuePair("func_id", func_id));
		postData.add((NameValuePair) new BasicNameValuePair("taxi_id", taxi_id));
		postData.add((NameValuePair) new BasicNameValuePair("driver_id", driver_id));
		postData.add((NameValuePair) new BasicNameValuePair("passenger_id", passenger_id));
		postData.add((NameValuePair) new BasicNameValuePair("booking_id", booking_id));
		postData.add((NameValuePair) new BasicNameValuePair("price", price));
		postData.add((NameValuePair) new BasicNameValuePair("fare_price", fare_price));
		postData.add((NameValuePair) new BasicNameValuePair("gst", gst));
		postData.add((NameValuePair) new BasicNameValuePair("extras", extras));
		postData.add((NameValuePair) new BasicNameValuePair("payment_method", payment_method));
		postData.add((NameValuePair) new BasicNameValuePair("date", date));
		postData.add((NameValuePair) new BasicNameValuePair("time", time));
		
		postData.add((NameValuePair) new BasicNameValuePair("card_number", cardNumber));
		postData.add((NameValuePair) new BasicNameValuePair("cvv", cvvNumber));
		postData.add((NameValuePair) new BasicNameValuePair("expire_date", expiredDate));
		postData.add((NameValuePair) new BasicNameValuePair("zip_code", zipCode));
		postData.add((NameValuePair) new BasicNameValuePair("card_type", cardType));
		postData.add((NameValuePair) new BasicNameValuePair("country_id", countryId));
		postData.add((NameValuePair) new BasicNameValuePair("card_holder_name", cardHolderName));
		
		Log.e("payment_value", "" + postData);
		
		String serverResponse = WebServerOperation.sendHTTPPostRequestToServer(DriverApp.getInstance().getBaseUrl(), postData, true);
		Log.i("Server response for Payment", "_____"+serverResponse);
		return parsePaymentData(serverResponse);
	}

	private static String parsePaymentData(String jData) throws JSONException {
		JSONObject jDataObj = new JSONObject(jData);
		return jDataObj.getString("status_code");
	}
	
	//API #1012 Past Trip
	public static String getPastTripData(String func_id, String taxi_id, String driver_id) throws Exception {
		List<NameValuePair> postData = new ArrayList<NameValuePair>(3);
		postData.add((NameValuePair) new BasicNameValuePair("func_id", func_id));
		postData.add((NameValuePair) new BasicNameValuePair("taxi_id", taxi_id));
		postData.add((NameValuePair) new BasicNameValuePair("driver_id", driver_id));

		String serverResponse = WebServerOperation.sendHTTPPostRequestToServer(DriverApp.getInstance().getBaseUrl(), postData, true);
		Log.e("driver_id", "_____"+postData);
		Log.i("Server response for Past Trip", "_____"+serverResponse);
		return parsePastTripData(serverResponse);
	}

	private static String parsePastTripData(String jData) throws JSONException {
		JSONObject jDataObj = new JSONObject(jData);
		if("2001".equals(jDataObj.getString("status_code"))) {
			ArrayList<FastTripsInfo> fastTripsInfoList = new ArrayList<FastTripsInfo>();
			JSONArray jArrayData = jDataObj.getJSONArray("data");
			if (jArrayData != null) {
				Log.i("Size of past trip is", "_____"+jArrayData.length());
				for (int i = 0; i < jArrayData.length(); i++) {
					ArrayList<JourneyReportInfo> journeyReportInfoList = new ArrayList<JourneyReportInfo>();
					JSONObject jArrayDataObj = jArrayData.getJSONObject(i);
					FastTripsInfo fastTrip = new FastTripsInfo();
					JSONObject userObject = jArrayDataObj.getJSONObject("user");
					fastTrip.setName(userObject.getString("first_name") + " "+ userObject.getString("last_name"));
					
					UserInfo user = new UserInfo();
					if (userObject.getString("profile_pic") != null) {
						user.setProfilePic(userObject.getString("profile_pic"));
					} else {
						user.setProfilePic("");
					}
					user.setFirstName(userObject.getString("first_name"));
					user.setLastName(userObject.getString("last_name"));
					user.setEmail(userObject.getString("email"));
					user.setMobileNo(userObject.getString("mobile_no"));
					
					fastTrip.setUser(user);
					
					JSONArray jsonJourneyCoordinateArray = jArrayDataObj.getJSONArray("journey_coordinates");
					if(jsonJourneyCoordinateArray != null) {
						if (jsonJourneyCoordinateArray.length() > 0) {
							for (int j = 0; j < jsonJourneyCoordinateArray.length(); j++) {
								JourneyReportInfo journeyCoordinateInfo = new JourneyReportInfo();
								JSONArray jsonJourneyArray = jsonJourneyCoordinateArray.getJSONArray(j);
								journeyCoordinateInfo.setJourneyLatitude(Double.parseDouble(jsonJourneyArray.getString(1)));
								journeyCoordinateInfo.setJourneyLongitude(Double.parseDouble(jsonJourneyArray.getString(0)));
								journeyReportInfoList.add(journeyCoordinateInfo);
							}
							fastTrip.setJourneyReportInfo(journeyReportInfoList);
						} else {
							fastTrip.setJourneyReportInfo(null);
						}
						
					} else {
						fastTrip.setJourneyReportInfo(null);
					}
					
					fastTrip.setPickupName(jArrayDataObj.getString("pickup_address"));
					JSONObject jsonPicupLocationObj = jArrayDataObj.getJSONObject("pickup_loc");
					JSONArray jArrayPickUpCoordinateData = jsonPicupLocationObj.getJSONArray("coordinates");
					fastTrip.setPickupLat((Double) jArrayPickUpCoordinateData.get(1));
					fastTrip.setPickupLong((Double) jArrayPickUpCoordinateData.get(0));
					
					fastTrip.setDestinationName(jArrayDataObj.getString("destination_address"));
					JSONObject jsonDestinationObj = jArrayDataObj.getJSONObject("destination_loc");
					JSONArray jArrayDestinationCoordinateData = jsonDestinationObj .getJSONArray("coordinates");
					fastTrip.setDestinationLat((Double) jArrayDestinationCoordinateData.get(1));
					fastTrip.setDestinationLong((Double) jArrayDestinationCoordinateData.get(0));
					
					fastTrip.setDate(jArrayDataObj.getString("booking_date"));
					
					JSONObject paymentObject = jArrayDataObj.getJSONObject("payment");
					fastTrip.setFare(paymentObject.getString("amount"));
					fastTrip.setPaymentType(paymentObject.getString("payment_method"));
					
					fastTrip.setDistance(jArrayDataObj.getString("total_distance"));
				
					fastTripsInfoList.add(fastTrip);
				}
				
				DriverApp.getInstance().setFastTripsInfoList(fastTripsInfoList);

			} else {
				DriverApp.getInstance().setFastTripsInfoList(null);
			}
		} else {
			DriverApp.getInstance().setFastTripsInfoList(null);
		}
		
		return jDataObj.getString("status_code");
	}
	
	//API #1013 Setting
	public static String getSettingData(String func_id, String taxi_id, String driver_id, String first_name, String last_name, String email_address, String mobile_number) throws Exception {
		List<NameValuePair> postData = new ArrayList<NameValuePair>(7);
		postData.add((NameValuePair) new BasicNameValuePair("func_id", func_id));
		postData.add((NameValuePair) new BasicNameValuePair("taxi_id", taxi_id));
		postData.add((NameValuePair) new BasicNameValuePair("driver_id", driver_id));
		postData.add((NameValuePair) new BasicNameValuePair("first_name", first_name));
		postData.add((NameValuePair) new BasicNameValuePair("last_name", last_name));
		postData.add((NameValuePair) new BasicNameValuePair("email_address", email_address));
		postData.add((NameValuePair) new BasicNameValuePair("mobile_number", mobile_number));
	
		String serverResponse = WebServerOperation.sendHTTPPostRequestToServer(DriverApp.getInstance().getBaseUrl(), postData, true);
		Log.i("Server response for Setting", "_____"+serverResponse);
		return parseSettingData(serverResponse);
	}

	private static String parseSettingData(String jData) throws JSONException {
		JSONObject jDataObj = new JSONObject(jData);
		return jDataObj.getString("status_code");
	}
	
	//API #1014 Driver Identification
	public static String getDriverIdentificationData(String func_id, String taxi_id, String driver_id, String type, String image) throws Exception {
		List<NameValuePair> postData = new ArrayList<NameValuePair>(5);
		postData.add((NameValuePair) new BasicNameValuePair("func_id", func_id));
		postData.add((NameValuePair) new BasicNameValuePair("taxi_id", taxi_id));
		postData.add((NameValuePair) new BasicNameValuePair("driver_id", driver_id));
		postData.add((NameValuePair) new BasicNameValuePair("type", type));
		postData.add((NameValuePair) new BasicNameValuePair("image", image));
	
		String serverResponse = WebServerOperation.sendHTTPPostRequestToServer(DriverApp.getInstance().getBaseUrl(), postData, true);
		Log.i("Server response for Driver Identification", "_____"+serverResponse);
		return parseDriverIdentificationData(serverResponse);
	}

	private static String parseDriverIdentificationData(String jData) throws JSONException {
		JSONObject jDataObj = new JSONObject(jData);
		return jDataObj.getString("status_code");
	}
	
	//API #1015 Hailed Request
	public static String getHailedRequestData(String func_id, String taxi_id) throws Exception {
		List<NameValuePair> postData = new ArrayList<NameValuePair>(2);
		postData.add((NameValuePair) new BasicNameValuePair("func_id", func_id));
		postData.add((NameValuePair) new BasicNameValuePair("taxi_id", taxi_id));

		String serverResponse = WebServerOperation.sendHTTPPostRequestToServer(DriverApp.getInstance().getBaseUrl(), postData, true);
		Log.i("Server response for Hailed Request", "_____"+serverResponse);
		return parseHailedRequestData(serverResponse);
	}

	private static String parseHailedRequestData(String jData) throws JSONException {
		JSONObject jDataObj = new JSONObject(jData);
		
		if("2001".equals(jDataObj.getString("status_code"))) {
			JSONArray jArrayData = jDataObj.getJSONArray("data");
			if (jArrayData != null && jArrayData.length() > 0) {
					JSONObject jArrayDataObj = jArrayData.getJSONObject(0);
					HailedCabInfo hailedCabInfo= new HailedCabInfo();
					JSONObject userObject = jArrayDataObj.getJSONObject("user");
					hailedCabInfo.setPassengerFullName(userObject.getString("first_name") + " "+ userObject.getString("last_name"));
					
					UserInfo user = new UserInfo();
					if (userObject.getString("profile_pic") != null) {
						user.setProfilePic(userObject.getString("profile_pic"));
					} else {
						user.setProfilePic("");
					}
					user.setFirstName(userObject.getString("first_name"));
					user.setLastName(userObject.getString("last_name"));
					user.setEmail(userObject.getString("email"));
					user.setMobileNo(userObject.getString("mobile_no"));
					
					hailedCabInfo.setUser(user);
					
					hailedCabInfo.setPickupName(jArrayDataObj.getString("pickup_address"));
					JSONObject jsonPicupLocationObj = jArrayDataObj.getJSONObject("pickup_loc");
					JSONArray jArrayPickUpCoordinateData = jsonPicupLocationObj.getJSONArray("coordinates");
					hailedCabInfo.setPickupLat((Double) jArrayPickUpCoordinateData.get(1));
					hailedCabInfo.setPickupLong((Double) jArrayPickUpCoordinateData.get(0));
					
					hailedCabInfo.setDestinationName(jArrayDataObj.getString("destination_address"));
					JSONObject jsonDestinationObj = jArrayDataObj.getJSONObject("destination_loc");
					JSONArray jArrayDestinationCoordinateData = jsonDestinationObj .getJSONArray("coordinates");
					hailedCabInfo.setDestinationLat((Double) jArrayDestinationCoordinateData.get(1));
					hailedCabInfo.setDestinationLong((Double) jArrayDestinationCoordinateData.get(0));
					
					try {
						hailedCabInfo.setDate(Utils.commonDateFormat(jArrayDataObj.getString("booking_date")));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					hailedCabInfo.setTime(jArrayDataObj.getString("booking_time"));
					hailedCabInfo.setBookingId(jArrayDataObj.getString("booking_id"));
					hailedCabInfo.setIsParcel(jArrayDataObj.getInt("is_parcel"));
					hailedCabInfo.setPassengerNumber(jArrayDataObj.getInt("passenger_num"));
					
					JSONObject paymentObject = jArrayDataObj.getJSONObject("payment");
					//hailedCabInfo.setFare(paymentObject.getString("amount"));
					//hailedCabInfo.setPaymentType(paymentObject.getString("payment_method"));
					
					//hailedCabInfo.setDistance(jArrayDataObj.getString("total_distance"));
				
					DriverApp.getInstance().setHailedCabInfo(hailedCabInfo);
				
			} else {
				DriverApp.getInstance().setHailedCabInfo(null);
			}
			
		}
		return jDataObj.getString("status_code");
	}
	
//	API #1016 Hailed Add
	public static String getHailedAddData(String func_id, String taxi_id, String driver_id, String rego_no, String src_lat, String src_long, String pickup_address, String destination_lat, String destination_long, String destination_address, String booking_date, String booking_time) throws Exception {
		List<NameValuePair> postData = new ArrayList<NameValuePair>(12);
		postData.add((NameValuePair) new BasicNameValuePair("func_id", func_id));
		postData.add((NameValuePair) new BasicNameValuePair("taxi_id", taxi_id));
		postData.add((NameValuePair) new BasicNameValuePair("driver_id", driver_id));
		postData.add((NameValuePair) new BasicNameValuePair("rego_no", rego_no));
		postData.add((NameValuePair) new BasicNameValuePair("src_lat", src_lat));
		postData.add((NameValuePair) new BasicNameValuePair("src_long", src_long));
		postData.add((NameValuePair) new BasicNameValuePair("pickup_address", pickup_address));
		postData.add((NameValuePair) new BasicNameValuePair("dest_lat", destination_lat));
		postData.add((NameValuePair) new BasicNameValuePair("dest_long", destination_long));
		postData.add((NameValuePair) new BasicNameValuePair("destination_address", destination_address));
		postData.add((NameValuePair) new BasicNameValuePair("booking_date", booking_date));
		postData.add((NameValuePair) new BasicNameValuePair("booking_time", booking_time));
		
		Log.i("value is ", "_________"+postData);

		String serverResponse = WebServerOperation.sendHTTPPostRequestToServer(DriverApp.getInstance().getBaseUrl(), postData, true);
		Log.e("response for hailed add data", ""+serverResponse);
		return parseHailedAddData(serverResponse);
	}

	private static String parseHailedAddData(String jData) throws JSONException {
		JSONObject jDataObj = new JSONObject(jData);
		if(jDataObj.getString("status_code").equals("2001")){
			DriverApp.getInstance().setBookingId(jDataObj.getString("booking_id"));
		} else{
			DriverApp.getInstance().setBookingId("");
		}
		
		Log.e("Booking id is", ""+DriverApp.getInstance().getBookingId());
		return jDataObj.getString("status_code");
	}
	
	//API #31
	public static String getContactEmail(String func_id, String id, String subject, String body) throws Exception {
		List<NameValuePair> postData = new ArrayList<NameValuePair>(4);
		postData.add((NameValuePair) new BasicNameValuePair("func_id", func_id));
		postData.add((NameValuePair) new BasicNameValuePair("id", id));
		postData.add((NameValuePair) new BasicNameValuePair("subject", subject));
		postData.add((NameValuePair) new BasicNameValuePair("body", body));

		String serverResponse = WebServerOperation.sendHTTPPostRequestToServer(DriverApp.getInstance().getBaseUrl(), postData, true);
		return parseContactEmail(serverResponse);
	}
	
	private static String parseContactEmail(String jData) throws JSONException {
		JSONObject jDataObj = new JSONObject(jData);
		return jDataObj.getString("status_code");
	}
	
	//API #1017
	public static String getTaxiLocationUpdate(String func_id, String taxi_id, String latitude, String longitude, String location_name) throws Exception  {
		List<NameValuePair> postData = new ArrayList<NameValuePair>(5);
		postData.add((NameValuePair) new BasicNameValuePair("func_id", func_id));
		postData.add((NameValuePair) new BasicNameValuePair("taxi_id", taxi_id));
		postData.add((NameValuePair) new BasicNameValuePair("lat", longitude));
		postData.add((NameValuePair) new BasicNameValuePair("long", latitude));
		postData.add((NameValuePair) new BasicNameValuePair("location_name", location_name));

		Log.i("taxi location with values", "" + postData);
		String serverResponse = WebServerOperation.sendHTTPPostRequestToServer(DriverApp.getInstance().getBaseUrl(), postData, true);
		Log.e("server response TAXI location Update", ""+serverResponse);
		return parseTaxiLocatonUpdate(serverResponse);
		
	}
	
	private static String parseTaxiLocatonUpdate(String jData) throws JSONException {
		JSONObject jDataObj = new JSONObject(jData);
		return jDataObj.getString("status_code");
	}
	
	//API #1018
	public static String getDeviceRegistration(String func_id, String device_name,  String udid, String registration_key, String check) throws Exception  {
		List<NameValuePair> postData = new ArrayList<NameValuePair>(5);
		postData.add((NameValuePair) new BasicNameValuePair("func_id", func_id));
		postData.add((NameValuePair) new BasicNameValuePair("device_name", device_name));
		postData.add((NameValuePair) new BasicNameValuePair("udid", udid));
		postData.add((NameValuePair) new BasicNameValuePair("registration_key", registration_key));
		postData.add((NameValuePair) new BasicNameValuePair("check", check));

		Log.e("device regi with values", ""+postData);
		String serverResponse = WebServerOperation.sendHTTPPostRequestToServer(DriverApp.getInstance().getBaseUrl(), postData, true);
		Log.e("server response for device regi", ""+serverResponse);
		return parseDeviceRegistration(serverResponse);
		
	}
	
	private static String parseDeviceRegistration(String jData) throws JSONException {
		JSONObject jDataObj = new JSONObject(jData);
		
		if("2001".equals(jDataObj.getString("status_code"))) {
			DriverInfo info = new DriverInfo();
			info.setTaxiId(jDataObj.getString("taxi_id"));
			info.setRegoNo(jDataObj.getString("rego_no"));
			info.setFingurePrint(jDataObj.getString("fingure_print"));
			
			DriverApp.getInstance().setDriverInfo(info);
		} else {
			DriverApp.getInstance().setDriverInfo(null);
		}
		return jDataObj.getString("status_code");
	}
	
	//API #17
		public static String getCardList(String func_id, String id) throws Exception {
			List<NameValuePair> postData = new ArrayList<NameValuePair>(2);
			postData.add((NameValuePair) new BasicNameValuePair("func_id", func_id));
			postData.add((NameValuePair) new BasicNameValuePair("id", id));

			String serverResponse = WebServerOperation.sendHTTPPostRequestToServer(DriverApp.getInstance().getBaseUrl(), postData, true);
			return parseCardList(serverResponse);
		}

		private static String parseCardList(String jData) throws JSONException {
			JSONObject jDataObj = new JSONObject(jData);
			if(jDataObj.getString("status").equals("1")){
				ArrayList<CreditCardInfo> cireditCardInfoArray = new ArrayList<CreditCardInfo>();
				JSONArray dataArray = jDataObj.getJSONArray("data");
				if(dataArray.length() > 0) {
					for(int i = 0 ; i< dataArray.length(); i++){
						JSONObject values = dataArray.getJSONObject(i);
						CreditCardInfo creditCardInfo = new CreditCardInfo();
						creditCardInfo.setPaymentId(values.getString("payment_id"));
						creditCardInfo.setCareType(values.getString("card_type"));
						creditCardInfo.setCvv(values.getInt("cvv"));
						creditCardInfo.setStatus(values.getInt("status"));
						creditCardInfo.setExpireDate(values.getString("expire_date"));
						creditCardInfo.setIsDefault(values.getInt("is_default"));
						creditCardInfo.setCreatedAt(values.getString("created_at"));
						creditCardInfo.setCardNumber(values.getString("card_number"));
						creditCardInfo.setUpdatedAt(values.getString("updated_at"));
						creditCardInfo.setCardHolderName(values.getString("card_holder_name"));
						creditCardInfo.setZip(values.getString("zip_code"));
						creditCardInfo.setCountryId(values.getString("country_id"));
						creditCardInfo.setCountryName(values.getString("country_name"));
						cireditCardInfoArray.add(creditCardInfo);
					}
				
				  DriverApp.getInstance().setCreditCardInfoList(cireditCardInfoArray);
				}else{
				  DriverApp.getInstance().setCreditCardInfoList(null);
				}
				
			}else{
				DriverApp.getInstance().setCreditCardInfoList(null);
			}
			return jDataObj.getString("status_code");
		}
		
		//API #1006 Holiday Checking
		public static String getHolidayChecking(String func_id, String year, String month, String day) throws Exception {
			List<NameValuePair> postData = new ArrayList<NameValuePair>(4);
			postData.add((NameValuePair) new BasicNameValuePair("func_id", func_id));
			postData.add((NameValuePair) new BasicNameValuePair("year", year));
			postData.add((NameValuePair) new BasicNameValuePair("month", month));
			postData.add((NameValuePair) new BasicNameValuePair("day", day));

			String serverResponse = WebServerOperation.sendHTTPPostRequestToServer(DriverApp.getInstance().getBaseUrl(), postData, true);
			Log.i("server response for Holiday Checking", "__________"+serverResponse);
			return parseHolidayChecking(serverResponse);
		}

		private static String parseHolidayChecking(String jData) throws JSONException {
			JSONObject jDataObj = new JSONObject(jData);
			return jDataObj.getString("status_code");
		}
		
		//API #1020 RegionFare
		public static String getRegionFare(String func_id, String flag, String isHoliday, String timeOfTheday, String isHighOccupacy) throws Exception {
			List<NameValuePair> postData = new ArrayList<NameValuePair>(2);
			postData.add((NameValuePair) new BasicNameValuePair("func_id", func_id));
			postData.add((NameValuePair) new BasicNameValuePair("flag", flag));

			String serverResponse = WebServerOperation.sendHTTPPostRequestToServer(DriverApp.getInstance().getBaseUrl(), postData, true);
			Log.i("server response for Region Fare", "__________"+serverResponse);
			return parseRegionFare(serverResponse, Integer.parseInt(flag), Boolean.parseBoolean(isHoliday), Integer.parseInt(timeOfTheday), Boolean.parseBoolean(isHighOccupacy));
		}

		private static String parseRegionFare(String jData, int flag, boolean isHoliday, int timeOfTheday, boolean isHighOccupacy) throws JSONException {
			JSONArray jsonArray = new JSONArray(jData);
			int tripLocationZone = flag;
			if(jsonArray != null && jsonArray.length() > 0) {
				JSONObject dataObj = jsonArray.getJSONObject(0);
				if(tripLocationZone == 1) {
					Log.e("In MFDP", ""+"---------------");
					if(isHoliday) {
						ConstantValues.flagFall = dataObj.getDouble("peak_flagfall");
						ConstantValues.disPerKm = dataObj.getDouble("peak_distance");
						ConstantValues.timePerMin = dataObj.getDouble("Peak_time");
						
						ConstantValues.holidayFee = dataObj.getDouble("holiday_fee");
						ConstantValues.bookingFee = dataObj.getDouble("booking_fee");
						ConstantValues.airportRankFee = dataObj.getDouble("airport_rank_fee");
						ConstantValues.airportBookingFee = dataObj.getDouble("airport_booking_fee");
						ConstantValues.highOccupancyFee = dataObj.getDouble("high_occupancy_fee");
	
					} else {
						if(timeOfTheday == 0) {
							Log.e("In MFDP --- 0", ""+"------  day fare ---------");
							ConstantValues.flagFall = dataObj.getDouble("flagfall");
							ConstantValues.disPerKm = dataObj.getDouble("distance");
							ConstantValues.timePerMin = dataObj.getDouble("time");
							
							ConstantValues.holidayFee = dataObj.getDouble("holiday_fee");
							ConstantValues.bookingFee = dataObj.getDouble("booking_fee");
							ConstantValues.airportRankFee = dataObj.getDouble("airport_rank_fee");
							ConstantValues.airportBookingFee = dataObj.getDouble("airport_booking_fee");
							ConstantValues.highOccupancyFee = dataObj.getDouble("high_occupancy_fee");
	
						} else if (timeOfTheday == 1) {
							Log.e("In MFDP --- 1", ""+"------- over night -------");
							ConstantValues.flagFall = dataObj.getDouble("overnight_flagfall");
							ConstantValues.disPerKm = dataObj.getDouble("overnight_distance");
							ConstantValues.timePerMin = dataObj.getDouble("overnight_time");
							
							ConstantValues.holidayFee = dataObj.getDouble("holiday_fee");
							ConstantValues.bookingFee = dataObj.getDouble("booking_fee");
							ConstantValues.airportRankFee = dataObj.getDouble("airport_rank_fee");
							ConstantValues.airportBookingFee = dataObj.getDouble("airport_booking_fee");
							ConstantValues.highOccupancyFee = dataObj.getDouble("high_occupancy_fee");
							
						} else if (timeOfTheday == 2) {
							Log.e("In MFDP --- 2", ""+"---- Peak values ------");
							ConstantValues.flagFall = dataObj.getDouble("peak_flagfall");
							ConstantValues.disPerKm = dataObj.getDouble("peak_distance");
							ConstantValues.timePerMin = dataObj.getDouble("Peak_time");
							
							ConstantValues.holidayFee = dataObj.getDouble("holiday_fee");
							ConstantValues.bookingFee = dataObj.getDouble("booking_fee");
							ConstantValues.airportRankFee = dataObj.getDouble("airport_rank_fee");
							ConstantValues.airportBookingFee = dataObj.getDouble("airport_booking_fee");
							ConstantValues.highOccupancyFee = dataObj.getDouble("high_occupancy_fee");
						}
					}
				} else if (tripLocationZone == 2) {
					if(!isHighOccupacy) {
						Log.e("In GB", ""+"----- standard ------");
						ConstantValues.flagFall = dataObj.getDouble("flagfall");
						ConstantValues.disPerKm = dataObj.getDouble("distance");
						ConstantValues.timePerMin = dataObj.getDouble("time");
						
						ConstantValues.holidayFee = dataObj.getDouble("holiday_fee");
						ConstantValues.lateNightFree = dataObj.getDouble("late_night_fee");
						ConstantValues.bookingFee = dataObj.getDouble("booking_fee");
						ConstantValues.airportBookingFee = dataObj.getDouble("airport_booking_fee");

					} else {
						Log.e("In GB", ""+"----- HighOccupacy ------");
						ConstantValues.flagFall = dataObj.getDouble("ho_flagfall");
						ConstantValues.disPerKm = dataObj.getDouble("ho_distance");
						ConstantValues.timePerMin = dataObj.getDouble("ho_time");
						
						ConstantValues.holidayFee = dataObj.getDouble("holiday_fee");
						ConstantValues.lateNightFree = dataObj.getDouble("late_night_fee");
						ConstantValues.bookingFee = dataObj.getDouble("booking_fee");
						ConstantValues.airportBookingFee = dataObj.getDouble("airport_booking_fee");
					}
				} else {  // for Country & Regional
					if(!isHighOccupacy) {
						Log.e("In CR", ""+"----- standard ------");
						ConstantValues.flagFall = dataObj.getDouble("flagfall");
						ConstantValues.disPerKm = dataObj.getDouble("distance");
						ConstantValues.timePerMin = dataObj.getDouble("time");
						
						ConstantValues.holidayFee = dataObj.getDouble("holiday_fee");
						ConstantValues.lateNightFree = dataObj.getDouble("late_night_fee");
						ConstantValues.bookingFee = dataObj.getDouble("booking_fee");
						ConstantValues.airportBookingFee = dataObj.getDouble("airport_booking_fee");
					} else {
						Log.e("In CR", ""+"----- HighOccupacy ------");
						ConstantValues.flagFall = dataObj.getDouble("ho_flagfall");
						ConstantValues.disPerKm = dataObj.getDouble("ho_distance");
						ConstantValues.timePerMin = dataObj.getDouble("ho_time");
						
						ConstantValues.holidayFee = dataObj.getDouble("holiday_fee");
						ConstantValues.lateNightFree = dataObj.getDouble("late_night_fee");
						ConstantValues.bookingFee = dataObj.getDouble("booking_fee");
						ConstantValues.airportBookingFee = dataObj.getDouble("airport_booking_fee");
					}
				}
				
			}
			//return jDataObj.getString("status_code");
			return null;
		}
	
	
}
