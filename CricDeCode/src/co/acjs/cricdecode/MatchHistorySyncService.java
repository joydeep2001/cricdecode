package co.acjs.cricdecode;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import co.acjs.cricdecode.util.IabHelper;
import co.acjs.cricdecode.util.IabResult;
import co.acjs.cricdecode.util.Inventory;
import co.acjs.cricdecode.util.Purchase;

public class MatchHistorySyncService extends IntentService {
	public static boolean started = true;
	public static Context who;
	static int match_count;
	JSONObject jn = null;
	int trial = 0;
	JSONParser jsonParser;
	Cursor c, c1;
	List<NameValuePair> params;
	private static IabHelper mHelper;
	static final String SKU_REMOVE_ADS = "ad_removal",
			SKU_SUB_INFI = "sub_infi", SKU_SUB_INFI_SYNC = "sub_infi_sync",
			SKU_SUB_SYNC = "sub_sync";

	public MatchHistorySyncService() {
		super("MatchCreateService");
	}

	String getMD5() {
		try {
			java.security.MessageDigest md = java.security.MessageDigest
					.getInstance("MD5");
			byte[] array = md.digest(AccessSharedPrefs.mPrefs.getString("id",
					"").getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100)
						.substring(1, 3));
			}
			return sb.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
		}
		return null;
	}

	String genMD5(String seed) {
		try {
			java.security.MessageDigest md = java.security.MessageDigest
					.getInstance("MD5");
			byte[] array = md.digest(seed.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100)
						.substring(1, 3));
			}
			return sb.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
		}
		return null;
	}

	public static String decrypt(String val1, String val2, String val3,
			String val4, String seq, int ci) {
		String val = val2 + val4 + val1 + val3;
		int num = val.length() / 10;
		char h[][] = new char[num + 1][10];
		int start = 0;
		int end = 10;
		for (int i = 0; i < num; i++) {
			String s = val.substring(start, end);
			h[i] = s.toCharArray();
			start = end;
			end = end + 10;
		}
		h[num] = val.substring(start, val.length()).toCharArray();
		char[][] un = new char[10][num];
		char s[] = seq.toCharArray();
		for (int i = 0; i < num; i++) {
			for (int j = 0; j < 10; j++) {
				String n = new String("" + s[j]);
				int ind = Integer.parseInt(n);
				un[ind][i] = h[i][j];
			}
		}
		String dec = "";
		for (int i = 0; i < 10; i++) {
			String n = new String(un[i]);
			dec = dec + n;
		}
		String ex = new String(h[num]);
		dec = dec + ex;
		char[] us = dec.toCharArray();
		char[] sh = new char[us.length];
		for (int i = 0; i < us.length; i++) {
			sh[i] = (char) (us[i] - ci);
		}
		return new String(sh);
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

	public void sendData(String regids) {

		c = getContentResolver().query(
				CricDeCodeContentProvider.CONTENT_URI_MATCH,
				new String[] { MatchDb.KEY_ROWID, MatchDb.KEY_DEVICE_ID,
						MatchDb.KEY_MATCH_DATE, MatchDb.KEY_MY_TEAM,
						MatchDb.KEY_OPPONENT_TEAM, MatchDb.KEY_VENUE,
						MatchDb.KEY_OVERS, MatchDb.KEY_INNINGS,
						MatchDb.KEY_RESULT, MatchDb.KEY_LEVEL,
						MatchDb.KEY_FIRST_ACTION, MatchDb.KEY_DURATION,
						MatchDb.KEY_REVIEW, MatchDb.KEY_STATUS,
						MatchDb.KEY_SYNCED },
				MatchDb.KEY_SYNCED + "=" + "0 and " + MatchDb.KEY_STATUS + "='"
						+ MatchDb.MATCH_HISTORY + "'", null, MatchDb.KEY_ROWID);
		match_count = c.getCount();

		int tcount = 0;
		if (c.getCount() != 0) {
			c.moveToFirst();
			do {
				try {
					JSONObject cm = new JSONObject();
					JSONObject per = new JSONObject();
					JSONObject gcm_cm = new JSONObject();
					JSONObject gcm_per = new JSONObject();
					cm.put("uid", AccessSharedPrefs.mPrefs.getString("id", ""));
					cm.put("mid", Integer.parseInt(c.getString(c
							.getColumnIndexOrThrow(MatchDb.KEY_ROWID))));
					cm.put("did", Integer.parseInt(AccessSharedPrefs.mPrefs
							.getString("device_id", "")));
					cm.put("a", c.getString(c
							.getColumnIndexOrThrow(MatchDb.KEY_MATCH_DATE)));
					cm.put("b", c.getString(c
							.getColumnIndexOrThrow(MatchDb.KEY_MY_TEAM)));
					cm.put("c", c.getString(c
							.getColumnIndexOrThrow(MatchDb.KEY_OPPONENT_TEAM)));
					cm.put("d", c.getString(c
							.getColumnIndexOrThrow(MatchDb.KEY_VENUE)));
					cm.put("e", Integer.parseInt(c.getString(c
							.getColumnIndexOrThrow(MatchDb.KEY_OVERS))));
					cm.put("f", Integer.parseInt(c.getString(c
							.getColumnIndexOrThrow(MatchDb.KEY_INNINGS))));
					cm.put("g", c.getString(c
							.getColumnIndexOrThrow(MatchDb.KEY_RESULT)));
					cm.put("h", c.getString(c
							.getColumnIndexOrThrow(MatchDb.KEY_LEVEL)));
					cm.put("i", c.getString(c
							.getColumnIndexOrThrow(MatchDb.KEY_FIRST_ACTION)));
					cm.put("j", c.getString(c
							.getColumnIndexOrThrow(MatchDb.KEY_DURATION)));
					cm.put("k", c.getString(c
							.getColumnIndexOrThrow(MatchDb.KEY_REVIEW)));

					gcm_cm.put("mid", Integer.parseInt(c.getString(c
							.getColumnIndexOrThrow(MatchDb.KEY_ROWID))));
					gcm_cm.put("did",
							AccessSharedPrefs.mPrefs.getString("device_id", ""));
					gcm_cm.put("a", c.getString(c
							.getColumnIndexOrThrow(MatchDb.KEY_MATCH_DATE)));
					gcm_cm.put("b", c.getString(c
							.getColumnIndexOrThrow(MatchDb.KEY_MY_TEAM)));
					gcm_cm.put("c", c.getString(c
							.getColumnIndexOrThrow(MatchDb.KEY_OPPONENT_TEAM)));
					gcm_cm.put("d", c.getString(c
							.getColumnIndexOrThrow(MatchDb.KEY_VENUE)));
					gcm_cm.put("e", Integer.parseInt(c.getString(c
							.getColumnIndexOrThrow(MatchDb.KEY_OVERS))));
					gcm_cm.put("f", Integer.parseInt(c.getString(c
							.getColumnIndexOrThrow(MatchDb.KEY_INNINGS))));
					gcm_cm.put("g", c.getString(c
							.getColumnIndexOrThrow(MatchDb.KEY_RESULT)));
					gcm_cm.put("h", c.getString(c
							.getColumnIndexOrThrow(MatchDb.KEY_LEVEL)));
					gcm_cm.put("i", c.getString(c
							.getColumnIndexOrThrow(MatchDb.KEY_FIRST_ACTION)));
					gcm_cm.put("j", c.getString(c
							.getColumnIndexOrThrow(MatchDb.KEY_DURATION)));
					gcm_cm.put("k", c.getString(c
							.getColumnIndexOrThrow(MatchDb.KEY_REVIEW)));

					String matchId = c.getString(c
							.getColumnIndexOrThrow(MatchDb.KEY_ROWID));
					String devid = c.getString(c
							.getColumnIndexOrThrow(MatchDb.KEY_DEVICE_ID));
			
					c1 = getContentResolver().query(
							CricDeCodeContentProvider.CONTENT_URI_PERFORMANCE,
							new String[] { PerformanceDb.KEY_ROWID,
									PerformanceDb.KEY_BAT_BALLS,
									PerformanceDb.KEY_BAT_BOWLER_TYPE,
									PerformanceDb.KEY_BAT_CHANCES,
									PerformanceDb.KEY_BAT_FIELDING_POSITION,
									PerformanceDb.KEY_BAT_FOURS,
									PerformanceDb.KEY_BAT_HOW_OUT,
									PerformanceDb.KEY_BAT_NUM,
									PerformanceDb.KEY_BAT_RUNS,
									PerformanceDb.KEY_BAT_SIXES,
									PerformanceDb.KEY_BAT_TIME,
									PerformanceDb.KEY_BOWL_BALLS,
									PerformanceDb.KEY_BOWL_CATCHES_DROPPED,
									PerformanceDb.KEY_BOWL_FOURS,
									PerformanceDb.KEY_BOWL_MAIDENS,
									PerformanceDb.KEY_BOWL_NOBALLS,
									PerformanceDb.KEY_BOWL_RUNS,
									PerformanceDb.KEY_BOWL_SIXES,
									PerformanceDb.KEY_BOWL_SPELLS,
									PerformanceDb.KEY_BOWL_WIDES,
									PerformanceDb.KEY_BOWL_WKTS_LEFT,
									PerformanceDb.KEY_BOWL_WKTS_RIGHT,
									PerformanceDb.KEY_FIELD_BYES,
									PerformanceDb.KEY_FIELD_CATCHES_DROPPED,
									PerformanceDb.KEY_FIELD_CIRCLE_CATCH,
									PerformanceDb.KEY_FIELD_CLOSE_CATCH,
									PerformanceDb.KEY_FIELD_DEEP_CATCH,
									PerformanceDb.KEY_FIELD_MISFIELDS,
									PerformanceDb.KEY_FIELD_RO_CIRCLE,
									PerformanceDb.KEY_FIELD_RO_DEEP,
									PerformanceDb.KEY_FIELD_RO_DIRECT_CIRCLE,
									PerformanceDb.KEY_FIELD_RO_DIRECT_DEEP,
									PerformanceDb.KEY_FIELD_SLIP_CATCH,
									PerformanceDb.KEY_FIELD_STUMPINGS,
									PerformanceDb.KEY_INNING,
									PerformanceDb.KEY_MATCHID,
									PerformanceDb.KEY_DEVICE_ID,
									PerformanceDb.KEY_STATUS },
							PerformanceDb.KEY_SYNCED + "=" + "0 and "
									+ PerformanceDb.KEY_STATUS + "='"
									+ MatchDb.MATCH_HISTORY + "' and "
									+ PerformanceDb.KEY_MATCHID + "= "
									+ matchId + " and "
									+ PerformanceDb.KEY_DEVICE_ID + "= '"
									+ devid + "'", null, null);

					JSONArray ja = new JSONArray();
					JSONArray gcm_ja = new JSONArray();
					if (c1.getCount() != 0) {
						c1.moveToFirst();
						do {
							JSONObject jo1 = new JSONObject();
							jo1.put("uid", AccessSharedPrefs.mPrefs.getString(
									"id", ""));
						
							jo1.put("mid",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_MATCHID))));
							jo1.put("did",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_DEVICE_ID))));
							jo1.put("pid",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_ROWID))));

							jo1.put("l",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_INNING))));
							jo1.put("m",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BAT_NUM))));
							jo1.put("n",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BAT_RUNS))));
							jo1.put("o",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BAT_BALLS))));
							jo1.put("p",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BAT_TIME))));
							jo1.put("q",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BAT_FOURS))));
							jo1.put("r",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BAT_SIXES))));
							jo1.put("s",
									c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BAT_HOW_OUT)));
							jo1.put("t",
									c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BAT_BOWLER_TYPE)));
							jo1.put("u",
									c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BAT_FIELDING_POSITION)));
							jo1.put("v",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BAT_CHANCES))));
							jo1.put("w",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_BALLS))));
							jo1.put("x",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_SPELLS))));
							jo1.put("y",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_MAIDENS))));
							jo1.put("z",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_RUNS))));
							jo1.put("a1",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_FOURS))));
							jo1.put("a2",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_SIXES))));
							jo1.put("a3",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_WKTS_LEFT))));
							jo1.put("a4",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_WKTS_RIGHT))));
							jo1.put("a5",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_CATCHES_DROPPED))));
							jo1.put("a6",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_NOBALLS))));
							jo1.put("a7",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_WIDES))));
							jo1.put("a8",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_SLIP_CATCH))));
							jo1.put("a9",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_CLOSE_CATCH))));
							jo1.put("a0",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_CIRCLE_CATCH))));
							jo1.put("b1",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_DEEP_CATCH))));
							jo1.put("b2",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_RO_CIRCLE))));
							jo1.put("b3",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_RO_DIRECT_CIRCLE))));
							jo1.put("b4",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_RO_DEEP))));
							jo1.put("b5",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_RO_DIRECT_DEEP))));
							jo1.put("b6",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_STUMPINGS))));
							jo1.put("b7",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_BYES))));
							jo1.put("b8",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_MISFIELDS))));
							jo1.put("b9",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_CATCHES_DROPPED))));

							ja.put(jo1);

							JSONObject gcm_jo1 = new JSONObject();

							gcm_jo1.put(
									"pid",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_ROWID))));

							gcm_jo1.put(
									"l",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_INNING))));
							gcm_jo1.put(
									"m",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BAT_NUM))));
							gcm_jo1.put(
									"n",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BAT_RUNS))));
							gcm_jo1.put(
									"o",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BAT_BALLS))));
							gcm_jo1.put(
									"p",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BAT_TIME))));
							gcm_jo1.put(
									"q",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BAT_FOURS))));
							gcm_jo1.put(
									"r",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BAT_SIXES))));
							gcm_jo1.put(
									"s",
									c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BAT_HOW_OUT)));
							gcm_jo1.put(
									"t",
									c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BAT_BOWLER_TYPE)));
							gcm_jo1.put(
									"u",
									c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BAT_FIELDING_POSITION)));
							gcm_jo1.put(
									"v",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BAT_CHANCES))));
							gcm_jo1.put(
									"w",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_BALLS))));
							gcm_jo1.put(
									"x",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_SPELLS))));
							gcm_jo1.put(
									"y",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_MAIDENS))));
							gcm_jo1.put(
									"z",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_RUNS))));
							gcm_jo1.put(
									"a1",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_FOURS))));
							gcm_jo1.put(
									"a2",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_SIXES))));
							gcm_jo1.put(
									"a3",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_WKTS_LEFT))));
							gcm_jo1.put(
									"a4",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_WKTS_RIGHT))));
							gcm_jo1.put(
									"a5",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_CATCHES_DROPPED))));
							gcm_jo1.put(
									"a6",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_NOBALLS))));
							gcm_jo1.put(
									"a7",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_WIDES))));
							gcm_jo1.put(
									"a8",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_SLIP_CATCH))));
							gcm_jo1.put(
									"a9",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_CLOSE_CATCH))));
							gcm_jo1.put(
									"a0",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_CIRCLE_CATCH))));
							gcm_jo1.put(
									"b1",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_DEEP_CATCH))));
							gcm_jo1.put(
									"b2",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_RO_CIRCLE))));
							gcm_jo1.put(
									"b3",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_RO_DIRECT_CIRCLE))));
							gcm_jo1.put(
									"b4",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_RO_DEEP))));
							gcm_jo1.put(
									"b5",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_RO_DIRECT_DEEP))));
							gcm_jo1.put(
									"b6",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_STUMPINGS))));
							gcm_jo1.put(
									"b7",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_BYES))));
							gcm_jo1.put(
									"b8",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_MISFIELDS))));
							gcm_jo1.put(
									"b9",
									Integer.parseInt(c1.getString(c1
											.getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_CATCHES_DROPPED))));

							gcm_ja.put(gcm_jo1);
							c1.moveToNext();
						} while (!c1.isAfterLast());
					}
					c1.close();

					per.put("per", ja);
					gcm_per.put("per", gcm_ja);

					Random r = new Random();
					int rand = r.nextInt(8) + 1;

					String handkey = AccessSharedPrefs.mPrefs.getString("id",
							"");
					for (int i = 0; i < rand; i++) {
						handkey = genMD5(handkey);
					}
					handkey = handkey.substring(0, 3) + rand
							+ handkey.substring(3, handkey.length());
					params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("matchData", cm
							.toString()));
					params.add(new BasicNameValuePair("perData", per.toString()));
					params.add(new BasicNameValuePair("user_id",
							AccessSharedPrefs.mPrefs.getString("id", "")));
					params.add(new BasicNameValuePair("hSAhnedk", handkey));
					jsonParser = new JSONParser();
					trial = 1;
					jn = null;
					while (jsonParser.isOnline(who)) {
						Log.w("JSONParser", "MatchHistory: Called");

						jn = jsonParser.makeHttpRequest(getResources()
								.getString(R.string.gae_match_insert), "POST",
								params, who);

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
							Uri uri = Uri
									.parse(CricDeCodeContentProvider.CONTENT_URI_MATCH
											+ "/"
											+ c.getString(c
													.getColumnIndexOrThrow(MatchDb.KEY_ROWID))
											+ "/"
											+ c.getString(c
													.getColumnIndexOrThrow(MatchDb.KEY_DEVICE_ID)));
							ContentValues matchvalues = new ContentValues();

							matchvalues.put(MatchDb.KEY_SYNCED, 1);
							getApplicationContext().getContentResolver()
									.update(uri, matchvalues, null, null);
							uri = Uri
									.parse(CricDeCodeContentProvider.CONTENT_URI_PERFORMANCE
											+ "/"
											+ c.getString(c
													.getColumnIndexOrThrow(MatchDb.KEY_ROWID))
											+ "/"
											+ c.getString(c
													.getColumnIndexOrThrow(MatchDb.KEY_DEVICE_ID)));
							ContentValues values = new ContentValues();

							values.put(PerformanceDb.KEY_SYNCED, 1);
							getApplicationContext().getContentResolver()
									.update(uri, values, null, null);
							gcm_cm.put("gcmid", 2);
							gcm_cm.put("per", gcm_ja);
							params = new ArrayList<NameValuePair>();
							params.add(new BasicNameValuePair("SendToArrays",
									regids));
							params.add(new BasicNameValuePair("MsgToSend",
									gcm_cm.toString()));
							params.add(new BasicNameValuePair("uid",
									AccessSharedPrefs.mPrefs
											.getString("id", "")));
							jsonParser = new JSONParser();
							trial = 1;
							jn = null;
							while (jsonParser.isOnline(who)) {
								jn = jsonParser.makeHttpRequest(getResources()
										.getString(R.string.azure_sendgcm),
										"POST", params, who);
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
									jn = jsonParser.makeHttpRequest(
											getResources().getString(
													R.string.gae_send_gcm),
											"POST", params, who);
									Log.w("JSON returned",
											"ProfileEditService: " + jn);
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
									jn = jsonParser.makeHttpRequest(
											getResources().getString(
													R.string.ping_hansa_gcm),
											"POST", params, who);
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
									jn = jsonParser.makeHttpRequest(
											getResources().getString(
													R.string.ping_acjs_gcm),
											"POST", params, who);
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
							tcount++;

						} else {
							break;
						}
					} else {
						break;
					}
				} catch (Exception e) {
				}

				c.moveToNext();
			} while (!c.isAfterLast());
		}
		c.close();

		if (tcount == match_count)
			AccessSharedPrefs.setString(who, "MatchHistorySyncServiceCalled",
					CDCAppClass.DOESNT_NEED_TO_BE_CALLED);

	}

	@Override
	protected void onHandleIntent(Intent intent) {

		mHelper = new IabHelper(
				this,
				decrypt("C,sCZBgBPDBE,p8OF0U[RLcYTHjI:iFsKulbsFD,Gs4Q2L1qh,BfWJRSnY9OBCY1mUI5UQPe0Y:wsNJ4",
						"uDL4NVye4e[B8oJFm40g2R45Jf3JuehFp4CH8K3lZBkRJBvef9dmJ",
						"D7FN4KtfNwB4iYV3G0e6Rn98JcPEsC9[qwE6F:BJB{Uw7g9O36NV53heRgz3JL:NlxlKdqJixRhBoVP6CBJlUHhgHtDPEzM7PXlehTBT8EJ:xLL8RHrgBC",
						"HkE5Pd47RYKBuSCFBHw22OvZdpPmzP8CZsEfS9WKqhbPYgQNk4qlWy0ouq[f{rco2gkuGoxi[pKkGSfDslUjKtfkPRcje2{:Lrd3cHztXv0BN2q:YHxu7MI:gx4O7whSqCs1jOHg[0n4W",
						"5143079682", 1));

		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			@Override
			public void onIabSetupFinished(IabResult result) {
				if (!result.isSuccess()) {
					Log.d("Billing", "Problem setting up In-app Billing: "
							+ result);
				} else {

					try {
						mHelper.queryInventoryAsync(new IabHelper.QueryInventoryFinishedListener() {
							public void onQueryInventoryFinished(
									IabResult result, Inventory inventory) {
								if (result.isFailure()) {
								} else {

									if (inventory
											.hasPurchase(SKU_SUB_INFI_SYNC)) {

										Purchase p1 = inventory
												.getPurchase(SKU_SUB_INFI_SYNC);
										if (p1.getDeveloperPayload().equals(
												getMD5())) {

											params = new ArrayList<NameValuePair>();
											params.add(new BasicNameValuePair(
													"user_id",
													AccessSharedPrefs.mPrefs
															.getString("id", "")));
											params.add(new BasicNameValuePair(
													"token", p1.getToken()));
											params.add(new BasicNameValuePair(
													"sign", p1.getSignature()));
											params.add(new BasicNameValuePair(
													"orderId", p1.getOrderId()));
											jsonParser = new JSONParser();
											trial = 1;
											jn = null;
											while (jsonParser.isOnline(who)) {
												jn = jsonParser
														.makeHttpRequest(
																who.getResources()
																		.getString(
																				R.string.gae_infisync_check),
																"POST", params,
																who);
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

											try {
												if (jn.getInt("status") == 1) {
													AccessSharedPrefs
															.setString(
																	who,
																	"infi_sync",
																	"yes");
													sendData(jn
															.getString("reg_ids"));
												} else if (jn.getInt("status") == 0) {
													AccessSharedPrefs
															.setString(
																	who,
																	"infi_sync",
																	"no");
													AccessSharedPrefs
															.setString(
																	who,
																	"MatchHistorySyncServiceCalled",
																	CDCAppClass.DOESNT_NEED_TO_BE_CALLED);
												}
											} catch (NullPointerException e) {
											} catch (JSONException e) {
												e.printStackTrace();
											}

										} else {
											AccessSharedPrefs.setString(who,
													"infi_sync", "no");
											AccessSharedPrefs
													.setString(
															who,
															"MatchHistorySyncServiceCalled",
															CDCAppClass.DOESNT_NEED_TO_BE_CALLED);
										}
									} else if (inventory
											.hasPurchase(SKU_SUB_SYNC)) {

										Purchase p1 = inventory
												.getPurchase(SKU_SUB_SYNC);
										if (p1.getDeveloperPayload().equals(
												getMD5())) {
											params = new ArrayList<NameValuePair>();
											params.add(new BasicNameValuePair(
													"user_id",
													AccessSharedPrefs.mPrefs
															.getString("id", "")));
											params.add(new BasicNameValuePair(
													"token", p1.getToken()));
											params.add(new BasicNameValuePair(
													"sign", p1.getSignature()));
											params.add(new BasicNameValuePair(
													"orderId", p1.getOrderId()));
											jsonParser = new JSONParser();
											trial = 1;
											jn = null;
											while (jsonParser.isOnline(who)) {

												jn = jsonParser
														.makeHttpRequest(
																who.getResources()
																		.getString(
																				R.string.gae_sync_check),
																"POST", params,
																who);
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

											try {
												if (jn.getInt("status") == 1) {
													AccessSharedPrefs
															.setString(who,
																	"sync",
																	"yes");
													sendData(jn
															.getString("reg_ids"));
												} else if (jn.getInt("status") == 0) {
													AccessSharedPrefs
															.setString(who,
																	"sync",
																	"no");
													AccessSharedPrefs
															.setString(
																	who,
																	"MatchHistorySyncServiceCalled",
																	CDCAppClass.DOESNT_NEED_TO_BE_CALLED);
												}
											} catch (NullPointerException e) {
											} catch (JSONException e) {
												e.printStackTrace();
											}
										} else {
											AccessSharedPrefs.setString(who,
													"sync", "no");
											AccessSharedPrefs
													.setString(
															who,
															"MatchHistorySyncServiceCalled",
															CDCAppClass.DOESNT_NEED_TO_BE_CALLED);
										}
									} else {

										AccessSharedPrefs.setString(who,
												"sync", "no");
										AccessSharedPrefs
												.setString(
														who,
														"MatchHistorySyncServiceCalled",
														CDCAppClass.DOESNT_NEED_TO_BE_CALLED);
									}

								}
							}
						});

					} catch (Exception e) {
					}
				}

			}
		});
	}

	

}