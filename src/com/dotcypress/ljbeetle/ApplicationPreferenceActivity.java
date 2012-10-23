package com.dotcypress.ljbeetle;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class ApplicationPreferenceActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		ListPreference hostings = (ListPreference) this.findPreference("photo_hosting");
		hostings.setSummary(hostings.getEntry());
		hostings.setOnPreferenceChangeListener(this);

		EditTextPreference enclosure = (EditTextPreference) this.findPreference("enclosure_text");
		enclosure.setSummary(enclosure.getText());
		enclosure.setOnPreferenceChangeListener(this);
	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference.getClass() == ListPreference.class) {
			ListPreference listPreference = (ListPreference) preference;
			int index = listPreference.findIndexOfValue(String.valueOf(newValue));
			preference.setSummary(listPreference.getEntries()[index]);

		} else if (preference.getClass() == EditTextPreference.class) {
			EditTextPreference text = (EditTextPreference) preference;
			text.setSummary(newValue.toString());
		}
		return true;
	}
}
