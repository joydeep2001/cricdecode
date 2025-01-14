package co.acjs.cricdecode;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ProfileEditService extends IntentService {
	public static boolean started = true;
	public static Context who;

	public ProfileEditService() {
		super("ProfileEditService");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		who = this;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		AccessSharedPrefs.mPrefs = getApplicationContext()
				.getSharedPreferences("CricDeCode", Context.MODE_PRIVATE);
		if (AccessSharedPrefs.mPrefs.getString("ProfileEditServiceCalled",
				CDCAppClass.DOESNT_NEED_TO_BE_CALLED).equals(
				CDCAppClass.NEEDS_TO_BE_CALLED)) {
			
			try {
				final JSONParser jsonParser = new JSONParser();
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("user_id",
						AccessSharedPrefs.mPrefs.getString("id", "")));
				params.add(new BasicNameValuePair("nick_name",
						AccessSharedPrefs.mPrefs.getString("nickname", "")));
				params.add(new BasicNameValuePair("role",
						AccessSharedPrefs.mPrefs.getString("role", "")));
				params.add(new BasicNameValuePair("bowling_style",
						AccessSharedPrefs.mPrefs.getString("bowlingStyle", "")));
				params.add(new BasicNameValuePair("batting_style",
						AccessSharedPrefs.mPrefs.getString("battingStyle", "")));
				Log.w("Sending User Data...", "ProfileEditService:"
						+ jsonParser.isOnline(who));
				int trial = 1;
				JSONObject jn = null;
				while (jsonParser.isOnline(who)) {
					Log.w("JSONParser", "ProfileEditService: Called");
					jn = jsonParser.makeHttpRequest(
							getResources().getString(R.string.gae_user_update),
							"POST", params, who);
					Log.w("JSON returned", "ProfileEditService: " + jn);
					Log.w("trial value", "ProfileEditService: " + trial);
			
					if (jn != null)
						break;
					try {
						Thread.sleep(10 * trial);
					} catch (InterruptedException e) {
					}
					trial++;

					if (trial == 50)
						break;
				}

				if (jn != null) {
					if (jn.getInt("status") == 1) {

						AccessSharedPrefs.setString(who,
								"ProfileEditServiceCalled",
								CDCAppClass.DOESNT_NEED_TO_BE_CALLED);

						params = new ArrayList<NameValuePair>();
						params.add(new BasicNameValuePair("uid",
								AccessSharedPrefs.mPrefs.getString("id", "")));
						params.add(new BasicNameValuePair("SendToArrays", jn
								.getString("reg_ids")));

						JSONObject jo = new JSONObject();
						jo.put("gcmid", 1);
						jo.put("nickname", AccessSharedPrefs.mPrefs.getString(
								"nickname", ""));
						jo.put("role",
								AccessSharedPrefs.mPrefs.getString("role", ""));
						jo.put("battingStyle", AccessSharedPrefs.mPrefs
								.getString("battingStyle", ""));
						jo.put("bowlingStyle", AccessSharedPrefs.mPrefs
								.getString("bowlingStyle", ""));
						params.add(new BasicNameValuePair("MsgToSend", jo
								.toString()));
			
						trial = 1;
						jn = null;

						while (jsonParser.isOnline(who)) {
							Log.w("JSONParser", "ProfileEditService: Called");
							jn = jsonParser.makeHttpRequest(getResources()
									.getString(R.string.azure_sendgcm), "POST",
									params, who);
							Log.w("JSON returned", "ProfileEditService: " + jn);
							Log.w("trial value", "ProfileEditService: " + trial);
						
							if (jn != null)
								break;
							try {
								Thread.sleep(10 * trial);
							} catch (InterruptedException e) {
							}
							trial++;

							if (trial == 50)
								break;
						}
						
						trial = 1;
						if (jn == null) {
							while (jsonParser.isOnline(who)) {
								Log.w("JSONParser",
										"ProfileEditService: Called");
								jn = jsonParser.makeHttpRequest(getResources()
										.getString(R.string.gae_send_gcm),
										"POST", params, who);
								Log.w("JSON returned", "ProfileEditService: "
										+ jn);
								Log.w("trial value", "ProfileEditService: "
										+ trial);
					
								if (jn != null)
									break;
								try {
									Thread.sleep(10 * trial);
								} catch (InterruptedException e) {
								}
								trial++;

								if (trial == 50)
									break;
							}

						}
						
						trial = 1;
						if (jn == null) {
							while (jsonParser.isOnline(who)) {
								Log.w("JSONParser",
										"ProfileEditService: Called");
								jn = jsonParser.makeHttpRequest(getResources()
										.getString(R.string.ping_hansa_gcm),
										"POST", params, who);
								Log.w("JSON returned", "ProfileEditService: "
										+ jn);
								Log.w("trial value", "ProfileEditService: "
										+ trial);
					
								if (jn != null)
									break;
								try {
									Thread.sleep(10 * trial);
								} catch (InterruptedException e) {
								}
								trial++;

								if (trial == 50)
									break;
							}

						}
						trial = 1;
						if (jn == null) {
							while (jsonParser.isOnline(who)) {
								Log.w("JSONParser",
										"ProfileEditService: Called");
								jn = jsonParser.makeHttpRequest(getResources()
										.getString(R.string.ping_acjs_gcm),
										"POST", params, who);
								Log.w("JSON returned", "ProfileEditService: "
										+ jn);
								Log.w("trial value", "ProfileEditService: "
										+ trial);
								if (jn != null)
									break;
								try {
									Thread.sleep(10 * trial);
								} catch (InterruptedException e) {
								}
								trial++;

								if (trial == 50)
									break;
							}

						}
			

					}
				}

			} catch (NullPointerException e) {
			} catch (JSONException e) {
			}
		}
	}

	
}
