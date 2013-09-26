package co.acjs.cricdecode;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PurchasedInfiSyncService extends IntentService {
	public static boolean	started	= true;

	public PurchasedInfiSyncService() {
		super("PurchasedInfiSyncService");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.w("PurchasedInfiSyncService", "Started");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.w("PurchasedInfiSyncService", "Ended");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		AccessSharedPrefs.mPrefs = getApplicationContext()
				.getSharedPreferences("CricDeCode", Context.MODE_PRIVATE);
		if (AccessSharedPrefs.mPrefs.getString(
				"PurchasedInfiSyncServiceCalled",
				CDCAppClass.DOESNT_NEED_TO_BE_CALLED).equals(
				CDCAppClass.NEEDS_TO_BE_CALLED)) {
			final JSONParser jsonParser = new JSONParser();
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("id", AccessSharedPrefs.mPrefs
					.getString("id", "")));
			params.add(new BasicNameValuePair("json", AccessSharedPrefs.mPrefs
					.getString("pur_infi_sync_data", "")));
			Log.w("Sending User Data...",
					"ProfileEditService:" + jsonParser.isOnline(this));
			int trial = 1;
			JSONObject jn = null;
			while (jsonParser.isOnline(this)) {
				jn = jsonParser.makeHttpRequest(
						getResources().getString(R.string.purchase_infi_sync),
						"POST", params, this);
				Log.w("JSON returned", "PurchasedInfiSyncServiceService: " + jn);
				Log.w("trial value", "PurchasedInfiSyncServiceService: " + trial);
				if (jn != null) break;
				try {
					Thread.sleep(10 * trial);
				} catch (InterruptedException e) {
				}
				trial++;
			}
			try {
				if (jn.getInt("status") == 1) {
					AccessSharedPrefs.setString(this,
							"PurchasedInfiSyncServiceCalled",
							CDCAppClass.DOESNT_NEED_TO_BE_CALLED);
					AccessSharedPrefs.setString(this, "pur_infi_sync_data", "");
				}
			} catch (Exception e) {
			}
		}
	}
}