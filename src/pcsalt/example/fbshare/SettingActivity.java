package pcsalt.example.fbshare;

import java.io.IOException;
import java.net.MalformedURLException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Window;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

public class SettingActivity extends Activity implements Consts {
	private SharedPreferences mPrefs;
	private Facebook facebook;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_setting);
		facebook = new Facebook(FBAPP_ID); // CHANGE FBAPP_ID IN Consts 
		mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		facebookLogin();
	}

	public void dialog(final int title) {
		AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
		builder.setTitle(title);
		builder.setMessage(R.string.logout_message);

		builder.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (title == R.string.title_facebook) {
					SharedPreferences.Editor edit = mPrefs.edit();
					edit.putString(PREF_KEY_ACCESS_TOKEN_FB, null);
					edit.putLong("access_expires", System.currentTimeMillis() - 1000);
					edit.commit();
					if (facebook.isSessionValid())
						try {
							facebook.logout(SettingActivity.this);
						} catch (MalformedURLException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
				}
			}
		});
		builder.setNegativeButton(R.string.btn_cancel, null);

		AlertDialog alert = builder.create();
		alert.show();
	}

	void facebookLogin() {
		if (!facebook.isSessionValid()) {
			facebook.authorize(this, new String[] { "publish_stream" }, -1, new DialogListener() {

				@Override
				public void onCancel() {
					// Function to handle cancel event
				}

				@Override
				public void onComplete(Bundle values) {
					String access = facebook.getAccessToken();
					long access_expires = facebook.getAccessExpires();
					SharedPreferences.Editor editor = mPrefs.edit();
					editor.putString(PREF_KEY_ACCESS_TOKEN_FB, access);
					editor.putLong("access_expires", access_expires);
					editor.commit();
				}

				@Override
				public void onError(DialogError error) { }

				@Override
				public void onFacebookError(FacebookError fberror) { }

			});
		}

	}

}
