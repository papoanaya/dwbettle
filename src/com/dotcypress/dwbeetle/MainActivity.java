package com.dotcypress.dwbeetle;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;

import com.dotcypress.dwbeetle.adapters.DraftAdapter;
import com.dotcypress.dwbeetle.client.LiveJournalException;
import com.dotcypress.dwbeetle.client.LjClient;
import com.dotcypress.dwbeetle.core.Logger;
import com.dotcypress.dwbeetle.core.Utils;
import com.dotcypress.dwbeetle.model.Event;
import com.dotcypress.dwbeetle.upload.ImageHosting;
import com.dotcypress.dwbeetle.upload.ImageHostingRegistry;
import com.dotcypress.dwbeetle.widget.EntryEditor;
import com.dotcypress.dwbeetle.widget.EntryOptionsEditor;

import java.io.*;
import java.util.ArrayList;

public class MainActivity extends TabActivity {

	private static final String TEMP_PICTURE_FILENAME = "LjPicture.jpg";

	private static final int MENU_ABOUT = 0;
	private static final int MENU_PREFERENCES = 1;
	private static final int MENU_POST = 2;
	private static final int MENU_SAVE_DRAFT = 3;
	private static final int MENU_CLEAR = 4;
	private static final int MENU_LOGOUT = 5;

	private static final int CONTEXT_MENU_EDIT = 10;
	private static final int CONTEXT_MENU_DELETE = 11;

	private static final String EVENT_ID = "event_id";
	private static final String USERPIC_ID = "userpic_id";

	public static final int COMMAND_CREATE_PHOTO = 1;
	public static final int COMMAND_PICK_PHOTO = 2;
	public static final int COMMAND_INSERT_LJ_CUT = 3;
	public static final int COMMAND_INSERT_LJ_USER = 4;
	public static final int COMMAND_INSERT_LINK = 5;

	private static final int TAKE_PICTURE_REQUEST = 1;
	private static final int PICK_PICTURE_REQUEST = 2;
	private static final int EDIT_LJ_CUT_REQUEST = 3;
	private static final int EDIT_LJ_USER_REQUEST = 4;
	private static final int EDIT_LINK_REQUEST = 5;

	private Event _event;
	private ArrayList<Event> _draft;

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		TabHost tabHost = getTabHost();

		TabSpec tabEditor = tabHost.newTabSpec("tab1");
		tabEditor.setIndicator(getResources().getString(R.string.entry), getResources().getDrawable(R.drawable.tab_entry));

		tabEditor.setContent(R.id.tab_1);
		tabHost.addTab(tabEditor);

		TabSpec tabOptions = tabHost.newTabSpec("tab2");
		tabOptions.setIndicator(getResources().getString(R.string.entry_options), getResources().getDrawable(R.drawable.tab_options));
		tabOptions.setContent(R.id.tab_2);
		tabHost.addTab(tabOptions);

		TabSpec tabDraft = tabHost.newTabSpec("tab3");
		tabDraft.setIndicator(getResources().getString(R.string.draft), getResources().getDrawable(R.drawable.tab_draft));
		tabDraft.setContent(R.id.tab_3);
		tabHost.addTab(tabDraft);

		tabHost.setCurrentTab(0);

		initDraftList();

		editEvent(new Event());
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState.containsKey(EVENT_ID)) {
			_event.id = savedInstanceState.getInt(EVENT_ID);
			_event.userpic = savedInstanceState.getString(USERPIC_ID);
			((EntryOptionsEditor) findViewById(R.id.entryOptionsEditor)).updateUserpic();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(EVENT_ID, _event.id);
		outState.putString(USERPIC_ID, _event.userpic);
	}

	@Override
	protected void onStop() {
		try {
			saveDraft();
		} catch (Throwable e) {
			Logger.error("onStop", e);
		}
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(1, MENU_POST, 0, R.string.post).setIcon(android.R.drawable.ic_menu_send);
		menu.add(1, MENU_SAVE_DRAFT, 0, R.string.save_draft).setIcon(android.R.drawable.ic_menu_save);
		menu.add(1, MENU_CLEAR, 0, R.string.clear).setIcon(android.R.drawable.ic_menu_delete);
		menu.add(1, MENU_PREFERENCES, 0, R.string.preferences).setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(1, MENU_ABOUT, 0, R.string.menu_about).setIcon(android.R.drawable.ic_menu_info_details);
		menu.add(1, MENU_LOGOUT, 0, R.string.logout).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.getItem(0).setEnabled(hasPostExists());
		menu.getItem(1).setEnabled(hasPostExists());
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_ABOUT:
//                Intent aboutIntent = new Intent(this, FriendsActivity.class);
				Intent aboutIntent = new Intent(this, AboutActivity.class);
				startActivity(aboutIntent);
				return true;
			case MENU_PREFERENCES:
				Intent preferenceIntent = new Intent(this, ApplicationPreferenceActivity.class);
				startActivity(preferenceIntent);
				return true;
			case MENU_POST:
				doPost();
				return true;
			case MENU_SAVE_DRAFT:
				saveDraft();
				return true;
			case MENU_CLEAR:
				editEvent(new Event());
				return true;
			case MENU_LOGOUT:
				saveDraft();
				App app = (App) getApplication();
				app.client = null;
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
				Editor editor = preferences.edit();
				editor.putBoolean(App.PREFERENCES_REMEMBER_ME, false);
				editor.commit();
				finish();
				Intent intent = new Intent(this, LoginActivity.class);
				startActivity(intent);
				return true;
		}
		return false;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, CONTEXT_MENU_EDIT, 0, R.string.edit);
		menu.add(0, CONTEXT_MENU_DELETE, 0, R.string.delete);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		ListView draftList = (ListView) findViewById(R.id.draftList);
		Event event = (Event) draftList.getAdapter().getItem(info.position);
		switch (item.getItemId()) {
			case CONTEXT_MENU_EDIT:
				editEvent(event);
				getTabHost().setCurrentTab(0);
				return true;
			case CONTEXT_MENU_DELETE:
				if (_event != null && _event.id == event.id) {
					setCurrentEvent(new Event());
				}
				App app = (App) getApplication();
				app.databaseDispatcher.delete(event);
				reloadDrafts();
				return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data != null && requestCode == PICK_PICTURE_REQUEST && resultCode == Activity.RESULT_OK) {
			try {
				Uri dataUri = data.getData();
				String path = getRealPathFromURI(dataUri);
				Logger.verbose("picked file:" + path);
				File tempFile = new File(getCacheDir(), TEMP_PICTURE_FILENAME);
				Utils.copy(new File(path), tempFile);
				new UploadImageTask().execute(tempFile.getAbsoluteFile().toString());
			} catch (IOException e) {
				Logger.error("Error copying photo", e);
				Toast.makeText(MainActivity.this, R.string.common_error, Toast.LENGTH_LONG).show();
			}
		}
		if (data != null && requestCode == TAKE_PICTURE_REQUEST && resultCode == Activity.RESULT_OK) {
			File tempFile = new File(getCacheDir(), TEMP_PICTURE_FILENAME);
			Bitmap bitmap = (Bitmap) data.getExtras().get("data");
			try {
				bitmap.compress(CompressFormat.JPEG, 90, new FileOutputStream(tempFile));
				bitmap.recycle();
				String path = tempFile.getAbsoluteFile().toString();
				Logger.verbose("saved to temp file:" + path);
				new UploadImageTask().execute(path);
			} catch (FileNotFoundException e) {
				Logger.error("Can't save photo", e);
				Toast.makeText(MainActivity.this, R.string.common_error, Toast.LENGTH_LONG).show();
			}
		}

		if (data != null && requestCode == EDIT_LJ_CUT_REQUEST && resultCode == Activity.RESULT_OK) {
			String cutText = data.getStringExtra(EditTextActivity.RESULT_EXTRA);
			String result = cutText.length() == 0 ? "<lj-cut>\n\n</lj-cut>" : "<lj-cut text=\"" + cutText + "\">\n\n</lj-cut>";
			((EntryEditor) findViewById(R.id.entryEditor)).insertToBody(result);
		}
		if (data != null && requestCode == EDIT_LJ_USER_REQUEST && resultCode == Activity.RESULT_OK) {
			String userText = data.getStringExtra(EditTextActivity.RESULT_EXTRA);
			String result = "<lj user=\"" + userText + "\">";
			((EntryEditor) findViewById(R.id.entryEditor)).insertToBody(result);
		}

		if (data != null && requestCode == EDIT_LINK_REQUEST && resultCode == Activity.RESULT_OK) {
			String linkValue = data.getStringExtra(LinkDialogActivity.VALUE_1);
			String linkText = data.getStringExtra(LinkDialogActivity.VALUE_2);
			String result = "<a href=\"" + linkValue + "\">" + linkText + "</a>";
			((EntryEditor) findViewById(R.id.entryEditor)).replaceBody(result);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void onCommand(int command) {
		switch (command) {
			case COMMAND_CREATE_PHOTO:
				File tempFile = new File(getCacheDir(), TEMP_PICTURE_FILENAME);
				if (tempFile.exists()) {
					tempFile.delete();
				}
				Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
				photoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
				startActivityForResult(photoIntent, TAKE_PICTURE_REQUEST);
				break;
			case COMMAND_PICK_PHOTO:
				Intent pickIntent = new Intent();
				pickIntent.setType("image/*");
				pickIntent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(Intent.createChooser(pickIntent, getResources().getString(R.string.select_photo_source)), PICK_PICTURE_REQUEST);
				break;
			case COMMAND_INSERT_LJ_CUT:
				Intent cutIntent = new Intent(this, EditTextActivity.class);
				cutIntent.putExtra(EditTextActivity.TITLE_EXTRA, R.string.insert_cut);
				cutIntent.putExtra(EditTextActivity.DEFAULT_TEXT_EXTRA, R.string.cut_default_text);
				startActivityForResult(cutIntent, EDIT_LJ_CUT_REQUEST);
				break;
			case COMMAND_INSERT_LJ_USER:
				Intent userIntent = new Intent(this, EditTextActivity.class);
				userIntent.putExtra(EditTextActivity.TITLE_EXTRA, R.string.insert_user);
				userIntent.putExtra(EditTextActivity.CHECK_INPUT_EXTRA, true);
				startActivityForResult(userIntent, EDIT_LJ_USER_REQUEST);
				break;
			case COMMAND_INSERT_LINK:
				Intent linkIntent = new Intent(this, LinkDialogActivity.class);
				linkIntent.putExtra(LinkDialogActivity.LABEL_1, R.string.insert_link);
				linkIntent.putExtra(LinkDialogActivity.LABEL_2, R.string.insert_text);
				String textSelected = ((EntryEditor) findViewById(R.id.entryEditor)).getSelectedText();
				linkIntent.putExtra(LinkDialogActivity.VALUE_2, textSelected);
				startActivityForResult(linkIntent, EDIT_LINK_REQUEST);
				break;

		}
	}

	private void initDraftList() {
		ListView draftList = (ListView) findViewById(R.id.draftList);
		draftList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
				Event event = (Event) adapter.getItemAtPosition(position);
				getTabHost().setCurrentTab(0);
				if (_event != null && _event.id == event.id) {
					return;
				}
				editEvent(event);
			}
		});
		registerForContextMenu(draftList);
		reloadDrafts();
	}

	private void setCurrentEvent(Event event) {
		_event = event;
		((EntryEditor) findViewById(R.id.entryEditor)).setEvent(_event);
		((EntryOptionsEditor) findViewById(R.id.entryOptionsEditor)).setEvent(_event);
	}

	private void reloadDrafts() {
		new Thread(new Runnable() {
			public void run() {
				App app = (App) getApplication();
				if (app.databaseDispatcher == null || app.client == null) {
					return;
				}
				_draft = app.databaseDispatcher.getEntities(Event.class, String.format("user='%s'", app.client.getCurrentUser().userName));
				runOnUiThread(new Runnable() {
					public void run() {
						ListView draftList = (ListView) findViewById(R.id.draftList);
						draftList.setAdapter(new DraftAdapter(MainActivity.this, _draft));
					}
				});
			}
		}).start();
	}

	private void editEvent(final Event event) {
		if (hasPostExists()) {
			if (_event.id > 0) {
				saveDraft();
				reloadDrafts();
				setCurrentEvent(event);
				return;
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.application_name);
			builder.setMessage(R.string.save_draft_message);
			builder.setPositiveButton(R.string.yes, new OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					saveDraft();
					reloadDrafts();
					setCurrentEvent(event);
				}

			});
			builder.setNeutralButton(R.string.no, new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					App app = (App) getApplication();
					app.databaseDispatcher.delete(_event);
					reloadDrafts();
					setCurrentEvent(event);
				}
			});
			builder.setNegativeButton(R.string.cancel, new OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.create().show();
		} else {
			setCurrentEvent(event);
		}
	}

	private void saveDraft() {
		commitChanges();
		if (!hasPostExists()) {
			return;
		}
		App app = (App) getApplication();
		app.databaseDispatcher.save(_event);
		reloadDrafts();
	}

	private void doPost() {
		commitChanges();
		new DoPostTask().execute(_event);
	}

	private void commitChanges() {
		App app = (App) getApplication();
		if (_event == null || app.client == null) {
			return;
		}
		((EntryEditor) findViewById(R.id.entryEditor)).commitChanges();
		((EntryOptionsEditor) findViewById(R.id.entryOptionsEditor)).commitChanges();
		_event.timestamp = System.currentTimeMillis();
		_event.user = app.client.getCurrentUser().userName;
	}

	private boolean hasPostExists() {
		commitChanges();
		return _event != null && _event.body != null && _event.body.length() > 0;
	}

	private String getRealPathFromURI(Uri contentUri) {

		String[] proj = {MediaColumns.DATA};
		Cursor cursor = managedQuery(contentUri, proj, null, null, null);
		int columnIndex = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
		cursor.moveToFirst();
		return cursor.getString(columnIndex);
	}

	private class DoPostTask extends AsyncTask<Event, Void, Boolean> {

		private ProgressDialog dialog = new ProgressDialog(MainActivity.this);
		private String error;
		private String url;
		private String enclosure;

		@Override
		protected void onPreExecute() {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
			if (prefs.getBoolean("enable_enclosure", false)) {
				enclosure = prefs.getString("enclosure_text", null);
			}
			dialog.setMessage(MainActivity.this.getString(R.string.sending_post));
			dialog.show();
		}

		@Override
		protected Boolean doInBackground(Event... params) {
			try {
				LjClient client = ((App) MainActivity.this.getApplication()).client;

				boolean result = client.postEvent(params[0], enclosure);
				if (result) {
					url = params[0].url;
				}
				return result;
			} catch (LiveJournalException e) {
				error = e.messageId == 0 ? e.message : MainActivity.this.getResources().getString(e.messageId);
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			try {
				dialog.dismiss();
			} catch (Throwable e) {
			}
			if (!result) {
				if (error != null) {
					Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(MainActivity.this, R.string.common_error, Toast.LENGTH_LONG).show();
				}
			} else {
				App app = (App) getApplication();
				app.databaseDispatcher.delete(_event);
				_event = null;
				reloadDrafts();
				editEvent(new Event());
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setMessage(R.string.success_post_dialog_message);
				builder.setCancelable(false);
				builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.addCategory(Intent.CATEGORY_BROWSABLE);
						intent.setData(Uri.parse(url));
						MainActivity.this.startActivity(intent);
					}
				});
				builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
				AlertDialog alert = builder.create();
				try {
					alert.show();
				} catch (Throwable e) {
					Logger.error("Post success dialog.", e);
				}
			}
		}
	}

	private class UploadImageTask extends AsyncTask<String, Void, String> {

		private ProgressDialog dialog = new ProgressDialog(MainActivity.this);
		private int maxPhotoSize = 400;
		private String _hostingId;
		private String _error;

		@Override
		protected void onPreExecute() {
			dialog.setMessage(MainActivity.this.getString(R.string.uploading_image));
			dialog.show();
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
			_hostingId = prefs.getString("photo_hosting", "imageshack");
		}

		@Override
		protected String doInBackground(String... params) {
			File resultFile = new File(getCacheDir(), "compressed_" + TEMP_PICTURE_FILENAME);
			String path = params[0];
			BitmapFactory.Options previewOptions = new BitmapFactory.Options();
			previewOptions.inJustDecodeBounds = true;
			try {
				BitmapFactory.decodeStream(new FileInputStream(path), null, previewOptions);
				int sampleSize = 1;
				Logger.verbose("Source image size:" + previewOptions.outWidth + ", " + previewOptions.outHeight);
				if (previewOptions.outHeight > previewOptions.outWidth && previewOptions.outHeight > maxPhotoSize) {
					sampleSize = (int) Math.floor((double) previewOptions.outHeight / (double) maxPhotoSize);
				}
				if (previewOptions.outHeight < previewOptions.outWidth && previewOptions.outWidth > maxPhotoSize) {
					sampleSize = (int) Math.floor((double) previewOptions.outWidth / (double) maxPhotoSize);
				}
				Logger.verbose("Sample size:" + sampleSize);
				BitmapFactory.Options compressOptions = new BitmapFactory.Options();
				compressOptions.inSampleSize = sampleSize;

				Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(path), null, compressOptions);
				Logger.verbose("Sampled image size:" + bitmap.getWidth() + ", " + bitmap.getHeight());
				bitmap.compress(CompressFormat.JPEG, 90, new FileOutputStream(resultFile));
			} catch (FileNotFoundException e) {
				Logger.error("Error resizing photo", e);
			}
			ImageHosting hosting = ImageHostingRegistry.getProvider(MainActivity.this, _hostingId);
			if (hosting != null) {
				try {
					return hosting.upload(resultFile.getAbsolutePath());
				} catch (LiveJournalException e) {
					_error = e.message;
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			try {
				dialog.dismiss();
			} catch (Throwable e) {

			}
			if (result == null) {
				if (_error != null) {
					Toast.makeText(MainActivity.this, _error, Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(MainActivity.this, R.string.common_error, Toast.LENGTH_LONG).show();
				}
			} else {
				((EntryEditor) findViewById(R.id.entryEditor)).insertToBody(result);
			}
		}
	}

}