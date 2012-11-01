package com.dotcypress.dwbeetle.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.dotcypress.dwbeetle.model.Userpic;

import java.io.File;
import java.util.ArrayList;

public class UserpicAdapter extends BaseAdapter {

	private Context _context;
	private ArrayList<Userpic> _userpics;

	public UserpicAdapter(Context context, ArrayList<Userpic> userpics) {
		_context = context;
		_userpics = userpics;
	}

	public int getCount() {
		return _userpics.size();
	}

	public Object getItem(int position) {
		return _userpics.get(position);
	}

	public long getItemId(int position) {
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		if (convertView == null) {
			imageView = new ImageView(_context);
			imageView.setLayoutParams(new GridView.LayoutParams(100, 100));
			imageView.setScaleType(ImageView.ScaleType.CENTER);
		} else {
			imageView = (ImageView) convertView;
		}
		Userpic upic = (Userpic) getItem(position);
		File file = new File(_context.getCacheDir(), upic.getFileName());
		imageView.setImageURI(Uri.parse(file.toString()));
		return imageView;
	}
}
