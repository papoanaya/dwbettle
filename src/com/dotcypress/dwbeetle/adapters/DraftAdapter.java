package com.dotcypress.dwbeetle.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dotcypress.dwbeetle.App;
import com.dotcypress.dwbeetle.R;
import com.dotcypress.dwbeetle.model.Event;
import com.dotcypress.dwbeetle.model.User;
import com.dotcypress.dwbeetle.model.Userpic;

import java.io.File;
import java.util.ArrayList;

public class DraftAdapter extends ArrayAdapter<Event> {
	private LayoutInflater _inflater;
	private String _journalPattern;
	private User _user;

	public DraftAdapter(Context context, ArrayList<Event> events) {
		super(context, R.layout.draft_row, events);
		_inflater = LayoutInflater.from(context);
		_user = ((App) context.getApplicationContext()).client.getCurrentUser();
		_journalPattern = context.getResources().getString(R.string.draft_journal);
	}

	@Override
	public View getView(int i, View convertView, ViewGroup viewGroup) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = _inflater.inflate(R.layout.draft_row, null);
			holder = new ViewHolder();
			holder.journal = (TextView) convertView.findViewById(R.id.journalCell);
			holder.subject = (TextView) convertView.findViewById(R.id.subjectCell);
			holder.body = (TextView) convertView.findViewById(R.id.bodyCell);
			holder.upic = (ImageView) convertView.findViewById(R.id.upicCell);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		Event event = getItem(i);
		holder.journal.setText(String.format(_journalPattern, event.journal));
		holder.subject.setText(event.subject);
		holder.body.setText(event.body);
		holder.upic.setVisibility(View.GONE);
		for (Userpic upic : _user.userpics) {
			if ((event.userpic == null && _user.defaultUserpicName.equals(upic.name)) || upic.name.equals(event.userpic)) {
				File file = new File(getContext().getCacheDir(), upic.getFileName());
				holder.upic.setImageURI(Uri.parse(file.toString()));
				holder.upic.setVisibility(View.VISIBLE);
			}
		}
		return convertView;
	}

	static class ViewHolder {
		TextView journal;
		TextView subject;
		TextView body;
		ImageView upic;
	}
}
