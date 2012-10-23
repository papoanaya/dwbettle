package com.dotcypress.ljbeetle;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import com.dotcypress.database.DatabaseDispatcher;
import com.dotcypress.ljbeetle.client.LiveJournalException;
import com.dotcypress.ljbeetle.client.LjClient;
import com.dotcypress.ljbeetle.core.DbUtils;
import com.dotcypress.ljbeetle.core.Utils;
import com.dotcypress.ljbeetle.model.User;
import com.dotcypress.ljbeetle.model.Userpic;

import java.io.File;

public class LoginActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		setTitle(R.string.login_title);

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Boolean autoLogin = preferences.getBoolean(App.PREFERENCES_REMEMBER_ME, false);
		int userId = preferences.getInt(App.PREFERENCES_USER_ID, -1);
		if (autoLogin && userId > 0) {
			DatabaseDispatcher databaseDispatcher = ((App) LoginActivity.this.getApplicationContext()).databaseDispatcher;
			User user = databaseDispatcher.getEntityById(User.class, userId);
			if (user != null) {
				DbUtils.loadUserData(databaseDispatcher, user);
				new UpdateUserTask().execute();
				((App) LoginActivity.this.getApplicationContext()).client = LjClient.login(user);
				showMainActivity();
				finish();
				return;
			}
		}

		App app = (App) getApplication();
		if (app.client != null) {
			showMainActivity();
			return;
		}
		findViewById(R.id.buttonOk).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				login();
			}
		});
		findViewById(R.id.buttonCancel).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}

	protected void login() {
		String username = ((EditText) findViewById(R.id.editTextUser)).getText().toString();
		String password = ((EditText) findViewById(R.id.editTextPassword)).getText().toString();
		new LoginTask().execute(username, password);
	}

	private void onAfterLogin() {
		LjClient client = ((App) LoginActivity.this.getApplicationContext()).client;
		DatabaseDispatcher databaseDispatcher = ((App) LoginActivity.this.getApplicationContext()).databaseDispatcher;
		User user = client.getCurrentUser();
		DbUtils.syncUser(databaseDispatcher, user);
		boolean rememberMe = ((CheckBox) findViewById(R.id.checkBoxRemember)).isChecked();
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = preferences.edit();
		editor.putBoolean(App.PREFERENCES_REMEMBER_ME, rememberMe);
		editor.putInt(App.PREFERENCES_USER_ID, user.id);
		editor.commit();
		showMainActivity();
	}

	private void showMainActivity() {
		Intent mainIntent = new Intent(this, MainActivity.class);
		startActivity(mainIntent);
		finish();
	}

	private class LoginTask extends AsyncTask<String, Void, LjClient> {

		private ProgressDialog dialog = new ProgressDialog(LoginActivity.this);
		private String error;

		@Override
		protected void onPreExecute() {
			dialog.setMessage(LoginActivity.this.getString(R.string.logging_in));
			dialog.show();
		}

		@Override
		protected LjClient doInBackground(String... params) {
			try {
				LjClient client = LjClient.login(params[0], Utils.md5(params[1]));
				if (client != null) {
					setMessage(R.string.syncing_upics);
					User user = client.getCurrentUser();
					if (user.userpics != null) {
						File cacheDir = LoginActivity.this.getCacheDir();
						for (Userpic upic : user.userpics) {
							File file = new File(cacheDir, upic.getFileName());
							if (file.exists()) {
								continue;
							}
							client.downloadUpic(upic.url, file);
						}
					}
				}
				return client;
			} catch (LiveJournalException e) {
				error = e.messageId == 0 ? e.message : LoginActivity.this.getResources().getString(e.messageId);
			}
			return null;
		}

		private void setMessage(final int messageId) {
			LoginActivity.this.runOnUiThread(new Runnable() {
				public void run() {
					dialog.setMessage(LoginActivity.this.getResources().getString(messageId));
				}
			});
		}

		@Override
		protected void onPostExecute(LjClient client) {
			dialog.dismiss();
			if (client == null) {
				if (error != null) {
					Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(LoginActivity.this, R.string.common_error, Toast.LENGTH_LONG).show();
				}
			} else {
				((App) LoginActivity.this.getApplicationContext()).client = client;
				onAfterLogin();
			}
		}
	}

	private class UpdateUserTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			if (LoginActivity.this == null || LoginActivity.this.getApplicationContext() == null) {
				return null;
			}
			LjClient existingClient = ((App) LoginActivity.this.getApplicationContext()).client;
			if (existingClient == null) {
				return null;
			}
			User user = existingClient.getCurrentUser();
			DatabaseDispatcher databaseDispatcher = ((App) LoginActivity.this.getApplicationContext()).databaseDispatcher;

			if (user == null || databaseDispatcher == null) {
				return null;
			}
			LjClient client = null;
			try {
				client = LjClient.login(user.userName, user.passwordHash);
			} catch (LiveJournalException e) {

			}
			if (client == null) {
				return null;
			}
			user = client.getCurrentUser();
			if (user.userpics != null) {
				File cacheDir = LoginActivity.this.getCacheDir();
				for (Userpic upic : user.userpics) {
					File file = new File(cacheDir, upic.getFileName());
					if (file.exists()) {
						continue;
					}
					client.downloadUpic(upic.url, file);
				}
			}
			DbUtils.syncUser(databaseDispatcher, user);
			return null;
		}
	}
}
