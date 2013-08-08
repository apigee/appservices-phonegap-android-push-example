package org.usergrid.cordova;


import java.util.HashMap;

import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.android.gcm.GCMRegistrar;

@SuppressWarnings("deprecation")
public class PushNotification extends Plugin {

	static final String TAG = "md.mdobs.andyPush";
	
	public static Plugin webView;
	
	@Override
	public PluginResult execute(String action, JSONArray args, String callbackId) {
		webView = this;
		try{
			if (action.equals("registerDevice")) {
				Log.i("Push.plugin", "register device");
				JSONObject options = args.getJSONObject(0);
				String gcmId = options.getString("gcmSenderId");
				GCMRegistrar.checkDevice(cordova.getActivity().getApplicationContext());
				GCMRegistrar.checkManifest(cordova.getActivity().getApplicationContext());
				GCMRegistrar.register(cordova.getActivity().getApplicationContext(), gcmId);
				return new PluginResult(PluginResult.Status.OK);
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
