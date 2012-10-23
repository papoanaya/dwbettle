package com.dotcypress.ljbeetle;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import com.dotcypress.database.DatabaseDispatcher;
import com.dotcypress.ljbeetle.client.LjClient;
import com.dotcypress.ljbeetle.model.Event;

public final class App extends Application {

	public static final String PREFERENCES_REMEMBER_ME = "REMEMBER_ME";
	public static final String PREFERENCES_USER_ID = "USER_ID";

	private static Event currentyViewedEvent;

	public LjClient client;
	public DatabaseDispatcher databaseDispatcher;

	@Override
	public void onCreate() {
		databaseDispatcher = new DatabaseDispatcher(this, getResources().getXml(R.xml.orm));
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		Editor edit = prefs.edit();
		if (!prefs.contains("photo_hosting")) {
			edit.putString("photo_hosting", "imageshack");
		}
		if (!prefs.contains("enable_enclosure")) {
			edit.putBoolean("enable_enclosure", true);
		}
		if (!prefs.contains("enclosure_text")) {
			edit.putString("enclosure_text", "Posted via LjBeetle");
		}
		edit.commit();
	}

	/**
	 * set/get CurrentlyViewedEvent is for exchanging data between event-list & event-reader activities
	 *
	 * @param e
	 */
	public static void setCurrentlyViewedEvent(Event e) {
		currentyViewedEvent = e;
	}

	public static Event getCurrentlyViewedEvent() throws NullPointerException {
		if (null == currentyViewedEvent)
			throw new NullPointerException("Did you forget to call setCurrentlyViewedEvent() first?");

		return currentyViewedEvent;
	}
}
