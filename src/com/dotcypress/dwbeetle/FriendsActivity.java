package com.dotcypress.dwbeetle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.dotcypress.dwbeetle.adapters.EventListAdapter;
import com.dotcypress.dwbeetle.client.LiveJournalException;
import com.dotcypress.dwbeetle.client.LjClient;
import com.dotcypress.dwbeetle.model.Event;

import java.util.ArrayList;

public class FriendsActivity extends Activity {

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friends_activity);
		initFriendsPageList();
	}

	private void initFriendsPageList() {
		ListView lv = (ListView) findViewById(R.id.friendspageList);
		LjClient client = (LjClient) ((App) FriendsActivity.this.getApplicationContext()).client;
		ArrayList<Event> posts;
		try {
			posts = (ArrayList<Event>) client.getFriendsEvents();
		} catch (LiveJournalException e) {
			// TODO log this and possibly ask user to file a bug report?
			posts = new ArrayList<Event>();
		}

		lv.setAdapter(new EventListAdapter(getApplicationContext(), posts));
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
				App.setCurrentlyViewedEvent((Event) adapter.getItemAtPosition(position));
				Intent myIntent = new Intent(FriendsActivity.this, EventReaderActivity.class);
				FriendsActivity.this.startActivity(myIntent);
			}
		});
		registerForContextMenu(lv);
	}
}