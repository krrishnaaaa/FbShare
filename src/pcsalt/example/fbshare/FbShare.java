package pcsalt.example.fbshare;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

public class FbShare extends Activity implements Consts {
	Button btnShare;
	EditText etShareText;
	private Facebook mFacebook;
	private AsyncFacebookRunner mAsyncRunner;
	private SharedPreferences mPrefs;
	String access_token_fb;
	String message;
	String filePath = null;

	String TAG = getClass().getSimpleName().toString();
	AlertMgr am = new AlertMgr(FbShare.this);

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// For Twitter
		if (Build.VERSION.SDK_INT > 8)
			StrictMode.enableDefaults();

		getLayoutViews();
		setListeners();

		mFacebook = new Facebook(FBAPP_ID);
		mAsyncRunner = new AsyncFacebookRunner(mFacebook);

		mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	}

	private void getLayoutViews() {
		btnShare = (Button) findViewById(R.id.btnShare);
		etShareText = (EditText) findViewById(R.id.sharetext);
	}

	private void setListeners() {

		btnShare.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (etShareText.length() == 0) {
					am.showAlert(R.string.error, R.string.no_text, R.string.btn_ok, null, null, null);
					return;
				}

				access_token_fb = mPrefs.getString(PREF_KEY_ACCESS_TOKEN_FB, null);

				if (access_token_fb == null) {
					am.showAlert(R.string.error, R.string.not_login, R.string.btn_login, openSetting, null, null);
					return;
				}

				message = etShareText.getText().toString();

				if (access_token_fb != null) {
					StartFacebook fb = new StartFacebook();
					fb.execute();
				} else {
					am.showAlert(R.string.title_facebook, R.string.facebook_not_login, R.string.btn_login, openSetting, R.string.btn_cancel, null);
				}
			}
		});

	}

	private DialogInterface.OnClickListener openSetting = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			startActivity(new Intent(FbShare.this, SettingActivity.class));
			dialog.dismiss();
		}
	};

	void facebookclick() {
		long expires = mPrefs.getLong(PREF_ACCESS_EXPIRES_FB, 0);
		if (access_token_fb != null) {
			mFacebook.setAccessToken(access_token_fb);
			publishon();
		}
		if (expires != 0) {
			mFacebook.setAccessExpires(expires);
		}

	}

	void publishon() {
		try {
			long expires = mPrefs.getLong(PREF_ACCESS_EXPIRES_FB, 0);
			mAsyncRunner = new AsyncFacebookRunner(mFacebook);
			if (access_token_fb != null) {
				mFacebook.setAccessToken(access_token_fb);
			}

			if (expires != 0) {
				mFacebook.setAccessExpires(expires);
			}
			if (mFacebook.isSessionValid()) {
				mAsyncRunner = new AsyncFacebookRunner(mFacebook);
				Bundle params = new Bundle();
				params.putString("message", "" + message);
				if (filePath != null) {
					byte[] data = null;

					// pass filePath in decodeFile()
					Bitmap bi = BitmapFactory.decodeFile(filePath);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					bi.compress(Bitmap.CompressFormat.JPEG, 100, baos);
					data = baos.toByteArray();
					params.putByteArray("picture", data);
					// to share image
					mAsyncRunner.request("me/photos", params, "POST", new FacebookPostListener(), null);
				} else {
					// to share text
					mAsyncRunner.request("me/feed", params, "POST", new FacebookPostListener(), null);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public class FacebookPostListener implements RequestListener {
		@Override
		public void onComplete(final String response, final Object state) {
			log("fpl onComplete: " + response);
		}

		@Override
		public void onFacebookError(FacebookError e, final Object state) {
			log("fpl onFacebookError: " + e);
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e, final Object state) {
			log("fpl onFileNotFoundException: " + e);
		}

		@Override
		public void onIOException(IOException e, final Object state) {
			log("fpl onIOException: " + e);
		}

		@Override
		public void onMalformedURLException(MalformedURLException e, final Object state) {
			log(e);
		}
	}

	class FacebookAuthListener implements DialogListener {

		@Override
		public void onFacebookError(FacebookError e) {
			log("facebook error: " + e);
			showToast(e.getMessage());
		}

		@Override
		public void onError(DialogError e) {
			log("dialog error: " + e);
			showToast(e.getMessage());
		}

		@Override
		public void onComplete(Bundle values) {
			log("onComplete: bundle values: " + values);
		}

		@Override
		public void onCancel() {
			showToast("Cancelled");
		}
	}

	public class StartFacebook extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			facebookclick();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			showToast("image shared");
			super.onPostExecute(result);
		}
	}

	public void showToast(String message) {
		Toast.makeText(FbShare.this, "" + message, Toast.LENGTH_LONG).show();
	}

	public void log(Object text) {
		Log.i("FbShare", "" + text);
	}

	public void login(View v) {
		startActivity(new Intent(FbShare.this, SettingActivity.class));
	}
}
