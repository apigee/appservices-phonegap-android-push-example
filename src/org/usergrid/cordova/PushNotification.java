package org.usergrid.cordova;


import java.util.HashMap;

import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.usergrid.android.client.Client;
import org.usergrid.android.client.callbacks.ApiResponseCallback;
import org.usergrid.android.client.callbacks.DeviceRegistrationCallback;
import org.usergrid.java.client.entities.Device;
import org.usergrid.java.client.entities.Entity;
import org.usergrid.java.client.response.ApiResponse;
import org.usergrid.java.client.utils.JsonUtils;

import android.util.Log;

import com.google.android.gcm.GCMRegistrar;

@SuppressWarnings("deprecation")
public class PushNotification extends Plugin {

	static final String TAG = "md.mdobs.andyPush";
	
	public static Plugin webView;
	private static Device device;
	
	@Override
	public PluginResult execute(String action, JSONArray args, String callbackId) {
		webView = this;
		try{
			if (action.equals("registerWithPushProvider")) {
				
				Client client = new Client();
				JSONObject options = args.getJSONObject(0);
				String apiUrl = options.getString("apiUrl");
				if(apiUrl == null) {
					apiUrl = "https://api.usergrid.org/";
				}
				
				
				final String callback = callbackId;
				client.setApiUrl(apiUrl);
				client.setOrganizationId(options.getString("orgName"));
				client.setApplicationId(options.getString("appName"));
				client.registerDeviceForPushAsync(cordova.getActivity().getApplicationContext(), options.getString("notifier"), options.getString("deviceId"), null, new DeviceRegistrationCallback(){
					@Override
					public void onResponse(Device device) {
						PushNotification.device = device;
				        PluginResult result = new PluginResult(PluginResult.Status.OK);
				        result.setKeepCallback(false);
				        success(result, callback);
					}
					
					@Override
					public void onException(Exception e) {
						Log.i("Push.plugin", "connect exception: " + e);
				        PluginResult result = new PluginResult(PluginResult.Status.ERROR);
				        result.setKeepCallback(false);
				        error(result, callback);
					}

					@Override
					public void onDeviceRegistration(Device device) {
						// This is never called
						
					}
				});
				
				PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
				result.setKeepCallback(true);
				return result;
				
				
			} else if (action.equals("registerDevice")) {
				Log.i("Push.plugin", "register device");
				JSONObject options = args.getJSONObject(0);
				String gcmId = options.getString("gcmSenderId");
				GCMRegistrar.checkDevice(cordova.getActivity().getApplicationContext());
				GCMRegistrar.checkManifest(cordova.getActivity().getApplicationContext());
				GCMRegistrar.register(cordova.getActivity().getApplicationContext(), gcmId);
				return new PluginResult(PluginResult.Status.OK);
			} else if (action.equals("pushNotificationToDevice")) {
				
				Client client = new Client();
				JSONObject options = args.getJSONObject(0);
				String apiUrl = options.getString("apiUrl");
				if(apiUrl == null) {
					apiUrl = "https://api.usergrid.org/";
				}
				
				
				final String callback = callbackId;
				client.setApiUrl(apiUrl);
				client.setOrganizationId(options.getString("orgName"));
				client.setApplicationId(options.getString("appName"));
				
				String entityPath = "devices/" + device.getUuid().toString() + "/notifications";
			    Entity notification = new Entity(entityPath);
	
			    HashMap<String,String> payloads = new HashMap<String, String>();
			    payloads.put("android_push", options.getString("message"));
			    notification.setProperty("payloads", JsonUtils.toJsonNode(payloads));
			    client.createEntityAsync(notification, new ApiResponseCallback() {
			
			      @Override
			      public void onResponse(ApiResponse apiResponse) {
			        Log.i("Push.plugin", "send response: " + apiResponse);
			        PluginResult result = new PluginResult(PluginResult.Status.OK);
			        result.setKeepCallback(false);
			        success(result, callback);
			      }
			
			      @Override
			      public void onException(Exception e) {
			    	  Log.i("Push.plugin", "send exception: " + e);
			    	  PluginResult result = new PluginResult(PluginResult.Status.ERROR);
			          result.setKeepCallback(false);
			          error(result, callback);
			      }
			     });
			     PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
				 result.setKeepCallback(true);
				 return result;
			} else {
				return new PluginResult(PluginResult.Status.INVALID_ACTION);
			}
		} catch (JSONException e) {
			Log.i("Push.plugin", "connect exception: " + e);
			return new PluginResult(PluginResult.Status.JSON_EXCEPTION);
		}
	}
	
	public static void sendPush(JSONObject pushMessage) {
		Log.i("Push.plugin", "NOTIFICATION CALLBACK");
		String javascript = String.format("window.pushNotification.notificationCallback(%s)", pushMessage.toString());
		webView.sendJavascript(javascript);
	}
	
	public static void sendRegistration(JSONObject registrationMessage) {
		Log.i("Push.plugin", "REGISTRATION CALLBACK");
		String javascript = String.format("window.pushNotification.registrationCallback(%s)", registrationMessage.toString());
		webView.sendJavascript(javascript);
	}
}
