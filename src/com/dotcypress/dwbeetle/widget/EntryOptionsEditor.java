package com.dotcypress.dwbeetle.widget;

import android.app.Dialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.dotcypress.database.DatabaseDispatcher;
import com.dotcypress.dwbeetle.App;
import com.dotcypress.dwbeetle.R;
import com.dotcypress.dwbeetle.adapters.UserpicAdapter;
import com.dotcypress.dwbeetle.client.LiveJournalException;
import com.dotcypress.dwbeetle.client.LjClient;
import com.dotcypress.dwbeetle.core.DbUtils;
import com.dotcypress.dwbeetle.core.Logger;
import com.dotcypress.dwbeetle.model.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EntryOptionsEditor extends FrameLayout {

	private Event _event;

	public EntryOptionsEditor(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initalize();
	}

	public EntryOptionsEditor(Context context) {
		super(context);
		initalize();
	}

	public EntryOptionsEditor(Context context, AttributeSet attrs) {
		super(context, attrs);
		initalize();
	}

	private void initalize() {
		addView(View.inflate(getContext(), R.layout.entry_options_editor, null));
		ImageView imageView = (ImageView) findViewById(R.id.upicView);
		LjClient client = ((App) EntryOptionsEditor.this.getContext().getApplicationContext()).client;
		if (client == null) {
			return;
		}
		final User user = client.getCurrentUser();
		imageView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				if (_event == null || user.userpics == null || user.userpics.size() < 2) {
					return;
				}
				final Dialog dialog = new Dialog(getContext());
				dialog.setTitle(R.string.select_upic);
				dialog.setCanceledOnTouchOutside(true);
				dialog.setContentView(R.layout.upic_selector);
				GridView gridView = (GridView) dialog.findViewById(R.id.upicsGridView);
				gridView.setAdapter(new UserpicAdapter(getContext(), user.userpics));
				gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
						Userpic upic = (Userpic) ((UserpicAdapter) adapterView.getAdapter()).getItem(i);
						_event.userpic = upic.name;
						updateUserpic();
						dialog.dismiss();
					}
				});
				dialog.show();
			}
		});

		Spinner journalSpinner = (Spinner) findViewById(R.id.journalSpinner);
		journalSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long is) {
				AccessedJournal journal = (AccessedJournal) adapterView.getItemAtPosition(pos);
				updateTagsAdapter();
				new LoadTagsTask().execute(journal.journal);
			}

			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});

		ArrayAdapter<AccessedJournal> journalAdapter = new ArrayAdapter<AccessedJournal>(getContext(), android.R.layout.simple_spinner_item, user.journals);
		journalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		journalSpinner.setAdapter(journalAdapter);
		journalSpinner.setSelection(0);

		Spinner privacySpinner = (Spinner) findViewById(R.id.privacySpinner);
		ArrayAdapter<String> privacyAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.privacy_variants));
		privacyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		privacySpinner.setAdapter(privacyAdapter);
		privacySpinner.setSelection(0);

		Button locateButton = (Button) findViewById(R.id.locateButton);
		locateButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				locateMe();
			}
		});

		CheckBox disableCommentsCheckbox = (CheckBox) findViewById(R.id.disableCommentsCheckbox);
		disableCommentsCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				CheckBox screenCommentsCheckbox = (CheckBox) findViewById(R.id.screenCommentsCheckbox);
				screenCommentsCheckbox.setEnabled(!isChecked);
			}
		});
	}

	protected void locateMe() {
		LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
		List<String> providers = locationManager.getProviders(true);
		if (providers != null && providers.size() > 0) {
			for (String provider : providers) {
				Location location = locationManager.getLastKnownLocation(provider);
				if (location == null || !location.hasAccuracy()) {
					continue;
				} else {
					TextView locationEdit = (TextView) findViewById(R.id.locationEdit);
					Geocoder gc = new Geocoder(getContext());
					try {
						List<Address> adreses = gc.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
						if (adreses != null && adreses.size() > 0) {
							Address address = adreses.get(0);
							String fullAddress = "";
							int maxLine = address.getMaxAddressLineIndex();
							for (int pos = maxLine; pos > 0; pos--) {
								fullAddress += address.getAddressLine(pos) + ", ";
							}
							if (maxLine != -1) {
								fullAddress += address.getAddressLine(0);
							}
							locationEdit.setText(fullAddress);
							return;
						}

					} catch (IOException e) {
						Logger.error("Error when reverse geocoding", e);
					}
				}
			}
		}
		Toast.makeText(getContext(), R.string.cant_locate, Toast.LENGTH_SHORT).show();
	}

	public static String extractAddress(Address address) {
		String fullAddress = "";
		int maxLine = address.getMaxAddressLineIndex();
		for (int pos = maxLine; pos > 0; pos--) {
			fullAddress += address.getAddressLine(pos) + ", ";
		}
		if (maxLine != -1) {
			fullAddress += address.getAddressLine(0);
		}
		return fullAddress;
	}

	public void setEvent(Event event) {
		_event = event;
		if (_event == null) {
			return;
		}
		updateUserpic();

		Spinner journalSpinner = (Spinner) findViewById(R.id.journalSpinner);
		SpinnerAdapter adapter = journalSpinner.getAdapter();
		int journalCount = adapter.getCount();
		journalSpinner.setSelection(0);
		for (int pos = 0; pos < journalCount; pos++) {
			if (adapter.getItem(pos).toString().equals(_event.journal)) {
				journalSpinner.setSelection(pos, true);
			}
		}
		Spinner privacySpinner = (Spinner) findViewById(R.id.privacySpinner);
		privacySpinner.setSelection(_event.privacy);

		MultiAutoCompleteTextView tagsEdit = (MultiAutoCompleteTextView) findViewById(R.id.tagsEdit);
		tagsEdit.setText(_event.tags);

		TextView locationEdit = (TextView) findViewById(R.id.locationEdit);
		locationEdit.setText(_event.location);

		TextView moodEdit = (TextView) findViewById(R.id.moodEdit);
		moodEdit.setText(_event.mood);

		TextView musicEdit = (TextView) findViewById(R.id.musicEdit);
		musicEdit.setText(_event.music);

		CheckBox screenCommentsCheckbox = (CheckBox) findViewById(R.id.screenCommentsCheckbox);
		screenCommentsCheckbox.setChecked(_event.screening > 0);

		CheckBox disableCommentsCheckbox = (CheckBox) findViewById(R.id.disableCommentsCheckbox);
		disableCommentsCheckbox.setChecked(_event.nocomments > 0);
	}

	public void updateUserpic() {
		User user = ((App) getContext().getApplicationContext()).client.getCurrentUser();
		if (user.userpics == null) {
			return;
		}
		ImageView imageView = (ImageView) findViewById(R.id.upicView);
		imageView.setVisibility(View.GONE);
		for (Userpic upic : user.userpics) {
			if ((_event.userpic == null && user.defaultUserpicName.equals(upic.name)) || upic.name.equals(_event.userpic)) {
				File file = new File(getContext().getCacheDir(), upic.getFileName());
				imageView.setImageURI(Uri.parse(file.toString()));
				imageView.setVisibility(View.VISIBLE);
				return;
			}
		}
	}

	public void commitChanges() {
		if (_event == null) {
			return;
		}
		Spinner journalSpinner = (Spinner) findViewById(R.id.journalSpinner);
		_event.journal = journalSpinner.getSelectedItem().toString();

		Spinner privacySpinner = (Spinner) findViewById(R.id.privacySpinner);
		_event.privacy = privacySpinner.getSelectedItemPosition();

		MultiAutoCompleteTextView tagsEdit = (MultiAutoCompleteTextView) findViewById(R.id.tagsEdit);
		_event.tags = tagsEdit.getText().toString();

		TextView locationEdit = (TextView) findViewById(R.id.locationEdit);
		_event.location = locationEdit.getText().toString();

		TextView moodEdit = (TextView) findViewById(R.id.moodEdit);
		_event.mood = moodEdit.getText().toString();

		TextView musicEdit = (TextView) findViewById(R.id.musicEdit);
		_event.music = musicEdit.getText().toString();

		CheckBox screenCommentsCheckbox = (CheckBox) findViewById(R.id.screenCommentsCheckbox);
		_event.screening = screenCommentsCheckbox.isChecked() ? 1 : 0;

		CheckBox disableCommentsCheckbox = (CheckBox) findViewById(R.id.disableCommentsCheckbox);
		_event.nocomments = disableCommentsCheckbox.isChecked() ? 1 : 0;
	}

	public void updateTagsAdapter() {
		Spinner journalSpinner = (Spinner) findViewById(R.id.journalSpinner);
		DatabaseDispatcher databaseDispatcher = ((App) getContext().getApplicationContext()).databaseDispatcher;

		ArrayList<Tag> tags = databaseDispatcher.getEntities(Tag.class, String.format("journal='%s'", journalSpinner.getSelectedItem().toString()));
		ArrayAdapter<Tag> adapter = new ArrayAdapter<Tag>(getContext(), android.R.layout.simple_dropdown_item_1line, tags);

		MultiAutoCompleteTextView tagsEdit = (MultiAutoCompleteTextView) findViewById(R.id.tagsEdit);
		tagsEdit.setAdapter(adapter);
		tagsEdit.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
	}

	private class LoadTagsTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			LjClient client = ((App) getContext().getApplicationContext()).client;
			DatabaseDispatcher databaseDispatcher = ((App) getContext().getApplicationContext()).databaseDispatcher;
			try {
				String[] tags = client.loadTags(params[0]);
				DbUtils.syncTags(databaseDispatcher, params[0], tags);
			} catch (LiveJournalException e) {
				Logger.error(String.format("Can't download tags for journal:%s", params[0]), e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			updateTagsAdapter();
		}
	}
}
