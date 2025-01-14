package co.acjs.cricdecode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import co.acjs.cricdecode.CDCAppClass.TrackerName;

import com.actionbarsherlock.app.SherlockFragment;
import com.facebook.widget.ProfilePictureView;
import com.google.android.gms.analytics.HitBuilders;


public class ProfileViewFragment extends SherlockFragment{
	static ProfileViewFragment	profileViewFragment;
	ProfilePictureView			pro_pic;
	ImageView					pro_pic_real;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		profileViewFragment = this;
		View rootView = inflater.inflate(R.layout.profile_view, container, false);
		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);
		onProfileViewing(view);
		
		com.google.android.gms.analytics.Tracker t = ((CDCAppClass) getActivity()
				.getApplication()).getTracker(TrackerName.APP_TRACKER);
		t.setScreenName(getResources().getString(R.string.analyticsProfileView));
		t.send(new HitBuilders.AppViewBuilder().build());
	}

	public void onProfileViewing(View view){
		Log.d("Debug", "on Profile Viewing called");
		AccessSharedPrefs.mPrefs = getSherlockActivity().getSharedPreferences("CricDeCode", Context.MODE_PRIVATE);
		pro_pic = (ProfilePictureView)view.findViewById(R.id.profile_picture);
		pro_pic.setCropped(true);
		pro_pic.setProfileId(AccessSharedPrefs.mPrefs.getString("id", ""));
		((TextView)view.findViewById(R.id.fname)).setText(AccessSharedPrefs.mPrefs.getString("f_name", ""));
		((TextView)view.findViewById(R.id.lname)).setText(AccessSharedPrefs.mPrefs.getString("l_name", ""));
		((TextView)view.findViewById(R.id.nickname)).setText(AccessSharedPrefs.mPrefs.getString("nickname", ""));
		((TextView)view.findViewById(R.id.date_of_birth)).setText(AccessSharedPrefs.mPrefs.getString("dob", ""));
		((TextView)view.findViewById(R.id.role)).setText(AccessSharedPrefs.mPrefs.getString("role", ""));
		((TextView)view.findViewById(R.id.batting_style)).setText(AccessSharedPrefs.mPrefs.getString("battingStyle", ""));
		((TextView)view.findViewById(R.id.bowling_style)).setText(AccessSharedPrefs.mPrefs.getString("bowlingStyle", ""));
	}

	public Bitmap getRoundedShape(Bitmap scaleBitmapImage){
		// TODO Auto-generated method stub
		int targetWidth = 50;
		int targetHeight = 50;
		Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(targetBitmap);
		Path path = new Path();
		path.addCircle(((float)targetWidth - 1) / 2, ((float)targetHeight - 1) / 2, (Math.min(((float)targetWidth), ((float)targetHeight)) / 2), Path.Direction.CCW);
		canvas.clipPath(path);
		Bitmap sourceBitmap = scaleBitmapImage;
		canvas.drawBitmap(sourceBitmap, new Rect(0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight()), new Rect(0, 0, targetWidth, targetHeight), null);
		return targetBitmap;
	}
}
