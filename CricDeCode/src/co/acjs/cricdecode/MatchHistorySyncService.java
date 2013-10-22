package co.acjs.cricdecode;

import java.util.ArrayList;
import java.util.List;

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

import com.stackmob.sdk.api.StackMobQuery;
import com.stackmob.sdk.api.StackMobQueryField;
import com.stackmob.sdk.callback.StackMobCallback;
import com.stackmob.sdk.callback.StackMobQueryCallback;
import com.stackmob.sdk.exception.StackMobException;


public class MatchHistorySyncService extends IntentService{
	public static boolean	started				= true;
	public static Context	who;
	static int				tot_per				= 0;
	static int				tot_mat_per			= 0;
	static int				cnt_tot_per			= 0;
	static int				cnt_tot_mat_per		= 0;
	static String			deviceId;
	ArrayList<String>		match_id_arr		= new ArrayList<String>();
	ArrayList<String>		performance_id_arr	= new ArrayList<String>();

	public MatchHistorySyncService(){
		super("MatchCreateService");
	}

	@Override
	public void onCreate(){
		super.onCreate();
		who = this;
		Log.w("MatchCreateService", "Started");
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		Log.w("MatchCreateService", "Ended");
	}

	@Override
	protected void onHandleIntent(Intent intent){
		if(AccessSharedPrefs.mPrefs.getString("MatchHistorySyncServiceCalled", CDCAppClass.DOESNT_NEED_TO_BE_CALLED).equals(CDCAppClass.NEEDS_TO_BE_CALLED)){
			final Cursor c = getContentResolver().query(CricDeCodeContentProvider.CONTENT_URI_MATCH, new String[ ]{MatchDb.KEY_ROWID, MatchDb.KEY_DEVICE_ID, MatchDb.KEY_MATCH_DATE, MatchDb.KEY_MY_TEAM, MatchDb.KEY_OPPONENT_TEAM, MatchDb.KEY_VENUE, MatchDb.KEY_OVERS, MatchDb.KEY_INNINGS, MatchDb.KEY_RESULT, MatchDb.KEY_LEVEL, MatchDb.KEY_FIRST_ACTION, MatchDb.KEY_DURATION, MatchDb.KEY_REVIEW, MatchDb.KEY_STATUS, MatchDb.KEY_SYNCED}, MatchDb.KEY_SYNCED + "=" + "0 and " + MatchDb.KEY_STATUS + "='" + MatchDb.MATCH_HISTORY + "'", null, MatchDb.KEY_ROWID);
			final Cursor t = getContentResolver().query(CricDeCodeContentProvider.CONTENT_URI_PERFORMANCE, new String[ ]{PerformanceDb.KEY_ROWID, PerformanceDb.KEY_BAT_BALLS, PerformanceDb.KEY_BAT_BOWLER_TYPE, PerformanceDb.KEY_BAT_CHANCES, PerformanceDb.KEY_BAT_FIELDING_POSITION, PerformanceDb.KEY_BAT_FOURS, PerformanceDb.KEY_BAT_HOW_OUT, PerformanceDb.KEY_BAT_NUM, PerformanceDb.KEY_BAT_RUNS, PerformanceDb.KEY_BAT_SIXES, PerformanceDb.KEY_BAT_TIME, PerformanceDb.KEY_BOWL_BALLS, PerformanceDb.KEY_BOWL_CATCHES_DROPPED, PerformanceDb.KEY_BOWL_FOURS, PerformanceDb.KEY_BOWL_MAIDENS, PerformanceDb.KEY_BOWL_NOBALLS, PerformanceDb.KEY_BOWL_RUNS, PerformanceDb.KEY_BOWL_SIXES, PerformanceDb.KEY_BOWL_SPELLS, PerformanceDb.KEY_BOWL_WIDES, PerformanceDb.KEY_BOWL_WKTS_LEFT, PerformanceDb.KEY_BOWL_WKTS_RIGHT, PerformanceDb.KEY_FIELD_BYES, PerformanceDb.KEY_FIELD_CATCHES_DROPPED, PerformanceDb.KEY_FIELD_CIRCLE_CATCH, PerformanceDb.KEY_FIELD_CLOSE_CATCH, PerformanceDb.KEY_FIELD_DEEP_CATCH, PerformanceDb.KEY_FIELD_MISFIELDS, PerformanceDb.KEY_FIELD_RO_CIRCLE, PerformanceDb.KEY_FIELD_RO_DEEP, PerformanceDb.KEY_FIELD_RO_DIRECT_CIRCLE, PerformanceDb.KEY_FIELD_RO_DIRECT_DEEP, PerformanceDb.KEY_FIELD_SLIP_CATCH, PerformanceDb.KEY_FIELD_STUMPINGS, PerformanceDb.KEY_INNING, PerformanceDb.KEY_MATCHID, PerformanceDb.KEY_STATUS}, PerformanceDb.KEY_SYNCED + "=" + "0 and " + PerformanceDb.KEY_STATUS + "='" + MatchDb.MATCH_HISTORY + "'", null, PerformanceDb.KEY_MATCHID);
			settotPerformance(t.getCount());
			Log.d("Debug", "Number of Un Synced Matches " + c.getCount());
			if(c.getCount() != 0){
				c.moveToFirst();
				do{
					ServerDBCricketMatch cm = new ServerDBCricketMatch(AccessSharedPrefs.mPrefs.getString("id", ""), Integer.parseInt(c.getString(c.getColumnIndexOrThrow(MatchDb.KEY_ROWID))), Integer.parseInt(AccessSharedPrefs.mPrefs.getString("device_id", "")), c.getString(c.getColumnIndexOrThrow(MatchDb.KEY_MATCH_DATE)), c.getString(c.getColumnIndexOrThrow(MatchDb.KEY_MY_TEAM)), c.getString(c.getColumnIndexOrThrow(MatchDb.KEY_OPPONENT_TEAM)), c.getString(c.getColumnIndexOrThrow(MatchDb.KEY_VENUE)), Integer.parseInt(c.getString(c.getColumnIndexOrThrow(MatchDb.KEY_OVERS))), Integer.parseInt(c.getString(c.getColumnIndexOrThrow(MatchDb.KEY_INNINGS))), c.getString(c.getColumnIndexOrThrow(MatchDb.KEY_RESULT)), c.getString(c.getColumnIndexOrThrow(MatchDb.KEY_LEVEL)), c.getString(c.getColumnIndexOrThrow(MatchDb.KEY_FIRST_ACTION)), c.getString(c.getColumnIndexOrThrow(MatchDb.KEY_DURATION)), c.getString(c.getColumnIndexOrThrow(MatchDb.KEY_REVIEW)), 0);
					// Set The StackMob Primary Key ID
					cm.setID(AccessSharedPrefs.mPrefs.getString("device_id", "") + "A" + c.getString(c.getColumnIndexOrThrow(MatchDb.KEY_ROWID)) + "B" + AccessSharedPrefs.mPrefs.getString("id", ""));
					setDeviceId(AccessSharedPrefs.mPrefs.getString("device_id", ""));
					cm.save(new StackMobCallback(){
						String	matchId	= c.getString(c.getColumnIndexOrThrow(MatchDb.KEY_ROWID));

						@Override
						public void failure(StackMobException arg0){
							Log.d("Debug", "Match Sync Failure");
						}

						@Override
						public void success(String arg0){
							final Cursor c1 = getContentResolver().query(CricDeCodeContentProvider.CONTENT_URI_PERFORMANCE, new String[ ]{PerformanceDb.KEY_ROWID, PerformanceDb.KEY_BAT_BALLS, PerformanceDb.KEY_BAT_BOWLER_TYPE, PerformanceDb.KEY_BAT_CHANCES, PerformanceDb.KEY_BAT_FIELDING_POSITION, PerformanceDb.KEY_BAT_FOURS, PerformanceDb.KEY_BAT_HOW_OUT, PerformanceDb.KEY_BAT_NUM, PerformanceDb.KEY_BAT_RUNS, PerformanceDb.KEY_BAT_SIXES, PerformanceDb.KEY_BAT_TIME, PerformanceDb.KEY_BOWL_BALLS, PerformanceDb.KEY_BOWL_CATCHES_DROPPED, PerformanceDb.KEY_BOWL_FOURS, PerformanceDb.KEY_BOWL_MAIDENS, PerformanceDb.KEY_BOWL_NOBALLS, PerformanceDb.KEY_BOWL_RUNS, PerformanceDb.KEY_BOWL_SIXES, PerformanceDb.KEY_BOWL_SPELLS, PerformanceDb.KEY_BOWL_WIDES, PerformanceDb.KEY_BOWL_WKTS_LEFT, PerformanceDb.KEY_BOWL_WKTS_RIGHT, PerformanceDb.KEY_FIELD_BYES, PerformanceDb.KEY_FIELD_CATCHES_DROPPED, PerformanceDb.KEY_FIELD_CIRCLE_CATCH, PerformanceDb.KEY_FIELD_CLOSE_CATCH, PerformanceDb.KEY_FIELD_DEEP_CATCH, PerformanceDb.KEY_FIELD_MISFIELDS, PerformanceDb.KEY_FIELD_RO_CIRCLE, PerformanceDb.KEY_FIELD_RO_DEEP, PerformanceDb.KEY_FIELD_RO_DIRECT_CIRCLE, PerformanceDb.KEY_FIELD_RO_DIRECT_DEEP, PerformanceDb.KEY_FIELD_SLIP_CATCH, PerformanceDb.KEY_FIELD_STUMPINGS, PerformanceDb.KEY_INNING, PerformanceDb.KEY_MATCHID, PerformanceDb.KEY_STATUS}, PerformanceDb.KEY_SYNCED + "=" + "0 and " + PerformanceDb.KEY_STATUS + "='" + MatchDb.MATCH_HISTORY + "' and " + PerformanceDb.KEY_MATCHID + "= " + matchId + " and " + PerformanceDb.KEY_DEVICE_ID + "= '" + getDeviceId() + "'", null, null);
							Log.w("Count of Performance", "" + c1.getCount());
							setMatchPerformance(c1.getCount());
							if(c1.getCount() != 0){
								c1.moveToFirst();
								do{
									Log.w("Syncing", "Putting match");
									ServerDBPerformance sp = new ServerDBPerformance(AccessSharedPrefs.mPrefs.getString("id", ""), Integer.parseInt(c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_MATCHID))), Integer.parseInt(AccessSharedPrefs.mPrefs.getString("device_id", "")), Integer.parseInt(c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_ROWID))), Integer.parseInt(c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_INNING))), Integer.parseInt(c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_BAT_NUM))), Integer.parseInt(c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_BAT_RUNS))), Integer.parseInt(c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_BAT_BALLS))), Integer.parseInt(c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_BAT_TIME))), Integer.parseInt(c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_BAT_FOURS))), Integer.parseInt(c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_BAT_SIXES))), c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_BAT_HOW_OUT)), c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_BAT_BOWLER_TYPE)), c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_BAT_FIELDING_POSITION)), Integer.parseInt(c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_BAT_CHANCES))), Integer.parseInt(c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_BALLS))), Integer.parseInt(c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_SPELLS))), Integer.parseInt(c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_MAIDENS))), Integer.parseInt(c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_RUNS))), Integer.parseInt(c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_FOURS))), Integer.parseInt(c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_SIXES))), Integer.parseInt(c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_WKTS_LEFT))), Integer.parseInt(c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_WKTS_RIGHT))), Integer.parseInt(c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_CATCHES_DROPPED))), Integer.parseInt(c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_NOBALLS))), Integer.parseInt(c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_WIDES))), Integer.parseInt(c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_SLIP_CATCH))), Integer.parseInt(c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_CLOSE_CATCH))), Integer.parseInt(c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_CIRCLE_CATCH))), Integer.parseInt(c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_DEEP_CATCH))), Integer.parseInt(c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_RO_CIRCLE))), Integer.parseInt(c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_RO_DIRECT_CIRCLE))), Integer.parseInt(c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_RO_DEEP))), Integer.parseInt(c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_RO_DIRECT_DEEP))), Integer.parseInt(c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_STUMPINGS))), Integer.parseInt(c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_BYES))), Integer.parseInt(c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_MISFIELDS))), Integer.parseInt(c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_CATCHES_DROPPED))), 0);
									// Set The StackMob Primary Key ID
									sp.setID(AccessSharedPrefs.mPrefs.getString("device_id", "") + "A" + c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_MATCHID)) + "B" + c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_ROWID)) + "C" + AccessSharedPrefs.mPrefs.getString("id", ""));
									sp.save(new StackMobCallback(){
										String	matchId	= c1.getString(c1.getColumnIndexOrThrow(PerformanceDb.KEY_MATCHID));

										@Override
										public void failure(StackMobException arg0){
											Log.d("Debug", "Performance Sync Failure");
										}

										@Override
										public void success(String arg0){
											if(cnt_MatchPerformance() == getMatchPerformance()){
												init_cnt_MatchPerformance();
												Log.d("Debug", "Performance Sync Update Success");
											}
											if(cnt_totPerformance() == gettotPerformance()){
												Log.d("Debug", "Performance Sync Update Success ALL");
												ServerDBAndroidDevices.query(ServerDBAndroidDevices.class, new StackMobQuery().field(new StackMobQueryField("user_id").isEqualTo(AccessSharedPrefs.mPrefs.getString("id", ""))), new StackMobQueryCallback<ServerDBAndroidDevices>(){
													@Override
													public void failure(StackMobException arg0){}

													@Override
													public void success(List<ServerDBAndroidDevices> arg0){
														Log.w("ProfileEditService", "GCM Ids fetched" + arg0.size());
														String regids = "";
														for(int i = 0; i < arg0.size(); i++){
															regids = regids + " " + arg0.get(i).getGcmId();
														}
														final Cursor c2 = getContentResolver().query(CricDeCodeContentProvider.CONTENT_URI_MATCH, new String[ ]{MatchDb.KEY_ROWID, MatchDb.KEY_DEVICE_ID, MatchDb.KEY_MATCH_DATE, MatchDb.KEY_MY_TEAM, MatchDb.KEY_OPPONENT_TEAM, MatchDb.KEY_VENUE, MatchDb.KEY_OVERS, MatchDb.KEY_INNINGS, MatchDb.KEY_RESULT, MatchDb.KEY_LEVEL, MatchDb.KEY_FIRST_ACTION, MatchDb.KEY_DURATION, MatchDb.KEY_REVIEW, MatchDb.KEY_STATUS, MatchDb.KEY_SYNCED}, MatchDb.KEY_SYNCED + "=" + "0 and " + MatchDb.KEY_STATUS + "='" + MatchDb.MATCH_HISTORY + "'", null, MatchDb.KEY_ROWID);
														JSONObject j = new JSONObject();
														try{
															if(c2.getCount() != 0){
																j.put("gcmid", "2");
																JSONArray ja = new JSONArray();
																c2.moveToFirst();
																do{
																	JSONObject jo = new JSONObject();
																	jo.put("mid", "" + c2.getString(c2.getColumnIndexOrThrow(MatchDb.KEY_ROWID)));
																	jo.put("dev", "" + c2.getString(c2.getColumnIndexOrThrow(MatchDb.KEY_DEVICE_ID)));
																	ja.put(jo);
																	c2.moveToNext();
																}while(!c2.isAfterLast());
																j.put("matches", ja);
															}
														}catch(JSONException e){}
														Log.w("Json tosend", "" + j.toString());
														List<NameValuePair> params = new ArrayList<NameValuePair>();
														params.add(new BasicNameValuePair("SendToArrays", regids));
														params.add(new BasicNameValuePair("MsgToSend", j.toString()));
														final JSONParser jsonParser = new JSONParser();
														Log.w("Sending User Data...", "MatchHistory:" + jsonParser.isOnline(who));
														int trial = 1;
														JSONObject jn = null;
														while(jsonParser.isOnline(who)){
															Log.w("JSONParser", "MatchHistory: Called");
															jn = jsonParser.makeHttpRequest(getResources().getString(R.string.edit_profile_sync), "POST", params, who);
															Log.w("JSON returned", "MatchHistory: " + jn);
															Log.w("trial value", "MatchHistory: " + trial);
															if(jn != null) break;
															try{
																Thread.sleep(10 * trial);
															}catch(InterruptedException e){}
															trial++;
														}
														try{
															if(jn.getInt("status") == 1)
																c2.moveToFirst();
															do{
																Uri uri = Uri.parse(CricDeCodeContentProvider.CONTENT_URI_MATCH + "/" + c2.getString(c2.getColumnIndexOrThrow(PerformanceDb.KEY_MATCHID)) + "/" + c2.getString(c2.getColumnIndexOrThrow(PerformanceDb.KEY_DEVICE_ID)));
																ContentValues matchvalues = new ContentValues();
																matchvalues.put(MatchDb.KEY_SYNCED, "1");
																getApplicationContext().getContentResolver().update(uri, matchvalues, null, null);
																uri = Uri.parse(CricDeCodeContentProvider.CONTENT_URI_PERFORMANCE + "/" + c2.getString(c2.getColumnIndexOrThrow(PerformanceDb.KEY_MATCHID)) + "/" + c2.getString(c2.getColumnIndexOrThrow(PerformanceDb.KEY_DEVICE_ID)));
																ContentValues values = new ContentValues();
																values.put(PerformanceDb.KEY_SYNCED, "1");
																getApplicationContext().getContentResolver().update(uri, values, null, null);
																c2.moveToNext();
															}while(!c2.isAfterLast());
															c2.close();
															AccessSharedPrefs.setString(who, "MatchHistorySyncServiceCalled", CDCAppClass.DOESNT_NEED_TO_BE_CALLED);
														}catch(NullPointerException e){}catch(JSONException e){
															e.printStackTrace();
														}
													}
												});
											}
											Log.d("Debug", "Performance Sync Success");
										}
									});
									c1.moveToNext();
								}while(!c1.isAfterLast());
							}
							c1.close();
							Log.d("Debug", "Match Sync Success");
						}
					});
					c.moveToNext();
				}while(!c.isAfterLast());
			}
			c.close();
			/*
			 * final JSONParser jsonParser = new JSONParser();
			 * List<NameValuePair> params = new ArrayList<NameValuePair>();
			 * Cursor c = getContentResolver().query(
			 * CricDeCodeContentProvider.CONTENT_URI_MATCH, new String[] {
			 * MatchDb.KEY_ROWID, MatchDb.KEY_DEVICE_ID, MatchDb.KEY_MATCH_DATE,
			 * MatchDb.KEY_MY_TEAM, MatchDb.KEY_OPPONENT_TEAM,
			 * MatchDb.KEY_VENUE, MatchDb.KEY_OVERS, MatchDb.KEY_INNINGS,
			 * MatchDb.KEY_RESULT, MatchDb.KEY_LEVEL, MatchDb.KEY_FIRST_ACTION,
			 * MatchDb.KEY_DURATION, MatchDb.KEY_REVIEW, MatchDb.KEY_STATUS,
			 * MatchDb.KEY_SYNCED }, MatchDb.KEY_SYNCED + "=" + "0 and " +
			 * MatchDb.KEY_STATUS + "='" + MatchDb.MATCH_HISTORY + "'", null,
			 * MatchDb.KEY_ROWID); Cursor c1 = getContentResolver()
			 * .query(CricDeCodeContentProvider.CONTENT_URI_PERFORMANCE, new
			 * String[] { PerformanceDb.KEY_ROWID, PerformanceDb.KEY_BAT_BALLS,
			 * PerformanceDb.KEY_BAT_BOWLER_TYPE, PerformanceDb.KEY_BAT_CHANCES,
			 * PerformanceDb.KEY_BAT_FIELDING_POSITION,
			 * PerformanceDb.KEY_BAT_FOURS, PerformanceDb.KEY_BAT_HOW_OUT,
			 * PerformanceDb.KEY_BAT_NUM, PerformanceDb.KEY_BAT_RUNS,
			 * PerformanceDb.KEY_BAT_SIXES, PerformanceDb.KEY_BAT_TIME,
			 * PerformanceDb.KEY_BOWL_BALLS,
			 * PerformanceDb.KEY_BOWL_CATCHES_DROPPED,
			 * PerformanceDb.KEY_BOWL_FOURS, PerformanceDb.KEY_BOWL_MAIDENS,
			 * PerformanceDb.KEY_BOWL_NOBALLS, PerformanceDb.KEY_BOWL_RUNS,
			 * PerformanceDb.KEY_BOWL_SIXES, PerformanceDb.KEY_BOWL_SPELLS,
			 * PerformanceDb.KEY_BOWL_WIDES, PerformanceDb.KEY_BOWL_WKTS_LEFT,
			 * PerformanceDb.KEY_BOWL_WKTS_RIGHT, PerformanceDb.KEY_FIELD_BYES,
			 * PerformanceDb.KEY_FIELD_CATCHES_DROPPED,
			 * PerformanceDb.KEY_FIELD_CIRCLE_CATCH,
			 * PerformanceDb.KEY_FIELD_CLOSE_CATCH,
			 * PerformanceDb.KEY_FIELD_DEEP_CATCH,
			 * PerformanceDb.KEY_FIELD_MISFIELDS,
			 * PerformanceDb.KEY_FIELD_RO_CIRCLE,
			 * PerformanceDb.KEY_FIELD_RO_DEEP,
			 * PerformanceDb.KEY_FIELD_RO_DIRECT_CIRCLE,
			 * PerformanceDb.KEY_FIELD_RO_DIRECT_DEEP,
			 * PerformanceDb.KEY_FIELD_SLIP_CATCH,
			 * PerformanceDb.KEY_FIELD_STUMPINGS, PerformanceDb.KEY_INNING,
			 * PerformanceDb.KEY_MATCHID, PerformanceDb.KEY_STATUS },
			 * PerformanceDb.KEY_SYNCED + "=" + "0 and " +
			 * PerformanceDb.KEY_STATUS + "='" + MatchDb.MATCH_HISTORY + "'",
			 * null, PerformanceDb.KEY_MATCHID); JSONObject json = new
			 * JSONObject(); JSONArray matches = new JSONArray(); JSONArray
			 * performance = new JSONArray(); try { if (c1.getCount() != 0) {
			 * c1.moveToFirst(); do { JSONObject row = new JSONObject();
			 * performance_id_arr .add(c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb.KEY_ROWID))); row.put("id",
			 * c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb.KEY_ROWID))); row.put("mat",
			 * c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb.KEY_MATCHID)));
			 * row.put("inn", c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb.KEY_INNING))); row.put("bb",
			 * c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb.KEY_BAT_BALLS)));
			 * row.put("bbt", c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb.KEY_BAT_BOWLER_TYPE)));
			 * row.put("bc", c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb.KEY_BAT_CHANCES)));
			 * row.put("bfp", c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb
			 * .KEY_BAT_FIELDING_POSITION))); row.put("bf", c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb.KEY_BAT_FOURS)));
			 * row.put("bho", c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb.KEY_BAT_HOW_OUT)));
			 * row.put("bn", c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb.KEY_BAT_NUM)));
			 * row.put("br", c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb.KEY_BAT_RUNS)));
			 * row.put("bs", c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb.KEY_BAT_SIXES)));
			 * row.put("bt", c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb.KEY_BAT_TIME)));
			 * row.put("ob", c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_BALLS)));
			 * row.put("ocd", c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_CATCHES_DROPPED)));
			 * row.put("of", c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_FOURS)));
			 * row.put("om", c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_MAIDENS)));
			 * row.put("ono", c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_NOBALLS)));
			 * row.put("oru", c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_RUNS)));
			 * row.put("osx", c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_SIXES)));
			 * row.put("osp", c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_SPELLS)));
			 * row.put("ow", c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_WIDES)));
			 * row.put("owl", c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_WKTS_LEFT)));
			 * row.put("owr", c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb.KEY_BOWL_WKTS_RIGHT)));
			 * row.put("fb", c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_BYES)));
			 * row.put("fcd", c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb
			 * .KEY_FIELD_CATCHES_DROPPED))); row.put("fcc", c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_CIRCLE_CATCH)));
			 * row.put("fco", c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_CLOSE_CATCH)));
			 * row.put("fdc", c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_DEEP_CATCH)));
			 * row.put("fmf", c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_MISFIELDS)));
			 * row.put("fci", c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb
			 * .KEY_FIELD_RO_DIRECT_CIRCLE))); row.put("fdd", c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_RO_DIRECT_DEEP)));
			 * row.put("fsc", c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_SLIP_CATCH)));
			 * row.put("fs", c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_STUMPINGS)));
			 * row.put("sts", c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb.KEY_STATUS)));
			 * row.put("frd", c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_RO_DEEP)));
			 * row.put("frc", c1.getString(c1
			 * .getColumnIndexOrThrow(PerformanceDb.KEY_FIELD_RO_CIRCLE)));
			 * performance.put(row); c1.moveToNext(); } while
			 * (!c1.isAfterLast()); } c1.close(); if (c.getCount() != 0) {
			 * c.moveToFirst(); // Do for every Match Row in the Cursor do {
			 * JSONObject row = new JSONObject(); match_id_arr.add(c.getString(c
			 * .getColumnIndexOrThrow(MatchDb.KEY_ROWID))); row.put("id",
			 * c.getString(c .getColumnIndexOrThrow(MatchDb.KEY_ROWID)));
			 * row.put("dat", c.getString(c
			 * .getColumnIndexOrThrow(MatchDb.KEY_MATCH_DATE))); row.put("myt",
			 * c.getString(c .getColumnIndexOrThrow(MatchDb.KEY_MY_TEAM)));
			 * row.put("opp", c.getString(c
			 * .getColumnIndexOrThrow(MatchDb.KEY_OPPONENT_TEAM)));
			 * row.put("ven", c.getString(c
			 * .getColumnIndexOrThrow(MatchDb.KEY_VENUE))); row.put("ovr",
			 * c.getString(c .getColumnIndexOrThrow(MatchDb.KEY_OVERS)));
			 * row.put("inn", c.getString(c
			 * .getColumnIndexOrThrow(MatchDb.KEY_INNINGS))); row.put("res",
			 * c.getString(c .getColumnIndexOrThrow(MatchDb.KEY_RESULT)));
			 * row.put("lvl", c.getString(c
			 * .getColumnIndexOrThrow(MatchDb.KEY_LEVEL))); row.put("act",
			 * c.getString(c .getColumnIndexOrThrow(MatchDb.KEY_FIRST_ACTION)));
			 * row.put("dur", c.getString(c
			 * .getColumnIndexOrThrow(MatchDb.KEY_DURATION))); row.put("rev",
			 * c.getString(c .getColumnIndexOrThrow(MatchDb.KEY_REVIEW)));
			 * row.put("sts", c.getString(c
			 * .getColumnIndexOrThrow(MatchDb.KEY_STATUS))); matches.put(row);
			 * c.moveToNext(); } while (!c.isAfterLast()); } c.close();
			 * json.put("matches", matches); json.put("performance",
			 * performance); } catch (Exception e) { } params.add(new
			 * BasicNameValuePair("id", AccessSharedPrefs.mPrefs
			 * .getString("id", ""))); params.add(new BasicNameValuePair("dev",
			 * AccessSharedPrefs.mPrefs .getString("device_id", "")));
			 * params.add(new BasicNameValuePair("json", json.toString()));
			 * Log.w("MATCH SYNC", "JSON: " + json.toString());
			 * writeToFile(json.toString()); int trial = 1; JSONObject jn =
			 * null; while (jsonParser.isOnline(this)) { jn =
			 * jsonParser.makeHttpRequest(
			 * getResources().getString(R.string.match_create_sync), "POST",
			 * params, this); Log.w("JSON returned", "MatchCreateService: " +
			 * jn); Log.w("trial value", "MatchCreateService: " + trial); if (jn
			 * != null) break; try { Thread.sleep(10 * trial); } catch
			 * (InterruptedException e) { } trial++; } try { if (jn != null) {
			 * if (jn.getInt("status") == 1) AccessSharedPrefs.setString(this,
			 * "MatchHistorySyncServiceCalled",
			 * CDCAppClass.DOESNT_NEED_TO_BE_CALLED); // TODO sab // sync ho //
			 * gaye String selection = MatchDb.KEY_ROWID + " in (" +
			 * DiaryMatchesFragment.buildSelectedItemString( match_id_arr, true)
			 * + " ) and " + MatchDb.KEY_DEVICE_ID + "='" +
			 * AccessSharedPrefs.mPrefs.getString("device_id", "") + "'";
			 * Log.d("Debug", "Match id list " + DiaryMatchesFragment
			 * .buildSelectedItemString( match_id_arr, true)); ContentValues
			 * matchvalues = new ContentValues();
			 * matchvalues.put(MatchDb.KEY_SYNCED, 1);
			 * getContentResolver().update(
			 * CricDeCodeContentProvider.CONTENT_URI_MATCH, matchvalues,
			 * selection, null); selection = PerformanceDb.KEY_ROWID + " in (" +
			 * DiaryMatchesFragment.buildSelectedItemString( performance_id_arr,
			 * true) + " ) and " + PerformanceDb.KEY_DEVICE_ID + "='" +
			 * AccessSharedPrefs.mPrefs.getString("device_id", "") + "'";
			 * Log.d("Debug", "Performance id list " + DiaryMatchesFragment
			 * .buildSelectedItemString( performance_id_arr, true)); matchvalues
			 * = new ContentValues(); matchvalues.put(PerformanceDb.KEY_SYNCED,
			 * 1); getContentResolver().update(
			 * CricDeCodeContentProvider.CONTENT_URI_PERFORMANCE, matchvalues,
			 * selection, null); Log.w("JSON returned", "" +
			 * jn.getInt("status")); } } catch (JSONException e) { }
			 */}
	}

	static int cnt_totPerformance(){
		cnt_tot_per++;
		return cnt_tot_per;
	}

	static void settotPerformance(int t){
		tot_per = t;
	}

	static int gettotPerformance(){
		return tot_per;
	}

	static int cnt_MatchPerformance(){
		cnt_tot_mat_per++;
		return cnt_tot_mat_per;
	}

	static void init_cnt_MatchPerformance(){
		tot_mat_per = 0;
		cnt_tot_mat_per = 0;
	}

	static void setMatchPerformance(int t){
		tot_mat_per = t;
	}

	static int getMatchPerformance(){
		return tot_mat_per;
	}

	static String getDeviceId(){
		return deviceId;
	}

	static void setDeviceId(String d){
		deviceId = d;
	}
	/*
	 * private void writeToFile(String data) { try { File root = new
	 * File(Environment.getExternalStorageDirectory(), "CricDeCode"); if
	 * (!root.exists()) { root.mkdirs(); } File gpxfile = new File(root,
	 * "log.txt"); FileWriter writer = new FileWriter(gpxfile);
	 * writer.write(data); writer.flush(); writer.close(); } catch (IOException
	 * e) { Log.e("Exception", "File write failed: " + e.toString()); } }
	 */
}
