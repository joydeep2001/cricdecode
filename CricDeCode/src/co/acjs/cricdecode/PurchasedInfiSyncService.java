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
import android.view.View;
import android.widget.TextView;

public class PurchasedInfiSyncService extends IntentService {
	public static boolean started = true;
	public Context con;

	public PurchasedInfiSyncService() {
		super("PurchasedInfiSyncService");
		con = this;
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
		if (AccessSharedPrefs.mPrefs.getString("PurchaseInfiSyncServiceCalled",
				CDCAppClass.DOESNT_NEED_TO_BE_CALLED).equals(
				CDCAppClass.NEEDS_TO_BE_CALLED)) {

			final JSONParser jsonParser = new JSONParser();
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("id", AccessSharedPrefs.mPrefs
					.getString("id", "")));
			params.add(new BasicNameValuePair("product_id", "sub_infi_sync"));
			params.add(new BasicNameValuePair("json", AccessSharedPrefs.mPrefs
					.getString("pur_infi_sync_data", "")));
			Log.w("Sending User Data...",
					"PurchaseInfiSync:" + jsonParser.isOnline(con));
			int trial = 1;
			JSONObject jn = null;
			while (jsonParser.isOnline(con)) {
				jn = jsonParser.makeHttpRequest(
						getResources().getString(R.string.azure_subscription),
						"POST", params, con);
				Log.w("JSON returned", "PurchasedInfiSyncServiceService: " + jn);
				Log.w("trial value", "PurchasedInfiSyncServiceService: "
						+ trial);
				if (jn != null)
					break;
				try {
					Thread.sleep(10 * trial);
				} catch (InterruptedException e) {
				}
				trial++;
				if (trial == 50) {
					break;
				}
			}
			
			try {
				if (jn.getInt("status") == 1) {
					AccessSharedPrefs.setString(con,
							"PurchaseInfiSyncServiceCalled",
							CDCAppClass.DOESNT_NEED_TO_BE_CALLED);
					AccessSharedPrefs.setString(con, "pur_infi_sync_data", "");
					AccessSharedPrefs.setString(con, "infi_sync", "yes");
					try {
						((MainActivity) MainActivity.main_context)
								.runOnUiThread(new Runnable() {
									public void run() {
										try {
											((TextView) ((MainActivity) MainActivity.main_context)
													.findViewById(R.id.infi_sync_pur))
													.setVisibility(View.VISIBLE);
											((TextView) ((MainActivity) MainActivity.main_context)
													.findViewById(R.id.infi_pur))
													.setText("Not Applicable");
											((TextView) ((MainActivity) MainActivity.main_context)
													.findViewById(R.id.infi_pur))
													.setVisibility(View.VISIBLE);
										} catch (Exception e) {
										}
									}
								});
					} catch (Exception e) {
					}
				} else if (jn.getInt("status") == 0) {
					AccessSharedPrefs.setString(con,
							"PurchaseInfiSyncServiceCalled",
							CDCAppClass.DOESNT_NEED_TO_BE_CALLED);
					AccessSharedPrefs.setString(con, "pur_infi_sync_data", "");
					AccessSharedPrefs.setString(con, "infi_sync", "no");
					try {
						((MainActivity) MainActivity.main_context)
								.runOnUiThread(new Runnable() {
									public void run() {
										try {
											((TextView) ((MainActivity) MainActivity.main_context)
													.findViewById(R.id.infi_sync_pur))
													.setVisibility(View.GONE);
										} catch (Exception e) {
										}
									}
								});
					} catch (Exception e) {
					}
				}
			} catch (Exception e) {
			}

		}
	}
}
