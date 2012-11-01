package com.dotcypress.dwbeetle.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dotcypress.dwbeetle.R;
import com.dotcypress.dwbeetle.model.Event;

import java.util.ArrayList;

public class EventListAdapter extends ArrayAdapter<Event> {
	private ArrayList<Event> items;

	public EventListAdapter(Context context, ArrayList<Event> items) {
		super(context, R.layout.entry_list_item, items);
		this.items = items;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(getContext());
			convertView = inflater.inflate(R.layout.entry_list_item, null);
		}

		Event ev = items.get(position);
		if (ev != null) {
			TextView subject = (TextView) convertView.findViewById(R.id.entry_item_subject);
			TextView author = (TextView) convertView.findViewById(R.id.entry_item_author);
			subject.setText(ev.subject);
			author.setText(ev.user);
		}

		return convertView;
	}

	public Event getItemAtPosition(int position) {
		return items.get(position);
	}
}
