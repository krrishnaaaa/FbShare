package pcsalt.example.fbshare;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;

public class AlertMgr {

	Context context;

	public AlertMgr(Context context) {
		this.context = context;
	}

	public void showAlert(String title, String message, String pText, OnClickListener pListener, String nText, OnClickListener nListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		builder.setMessage(message);
		if (pText != null)
			builder.setPositiveButton(pText, pListener);
		if (nText != null)
			builder.setNegativeButton(nText, nListener);
		builder.create().show();
	}

	public void showAlert(int title, int message, int pText, OnClickListener pListener, int nText, OnClickListener nListener) {
		showAlert(context.getString(title), context.getString(message), context.getString(pText), pListener, context.getString(nText), nListener);
	}
	public void showAlert(int title, int message, int pText, OnClickListener pListener, String nText, OnClickListener nListener) {
		showAlert(context.getString(title), context.getString(message), context.getString(pText), pListener, nText, nListener);
	}

}
