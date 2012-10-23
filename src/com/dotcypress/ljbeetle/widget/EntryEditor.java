package com.dotcypress.ljbeetle.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import com.dotcypress.ljbeetle.MainActivity;
import com.dotcypress.ljbeetle.R;
import com.dotcypress.ljbeetle.model.Event;

public class EntryEditor extends FrameLayout {

	private Event _event;

	public EntryEditor(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public EntryEditor(Context context) {
		super(context);
		init();
	}

	public EntryEditor(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		addView(View.inflate(getContext(), R.layout.entry_editor, null));
		findViewById(R.id.boldButton).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				EditText edit = ((EditText) findViewById(R.id.bodyEditText));
				int startSelection = edit.getSelectionEnd() < edit.getSelectionStart() ? edit.getSelectionEnd() : edit.getSelectionStart();
				int endSelection = edit.getSelectionEnd() > edit.getSelectionStart() ? edit.getSelectionEnd() : edit.getSelectionStart();
				if (startSelection == endSelection) {
					edit.getText().insert(startSelection, "<b></b>");
					edit.setSelection(startSelection + 3);
				} else {
					edit.getText().insert(endSelection, "</b>");
					edit.getText().insert(startSelection, "<b>");
				}
			}
		});

		findViewById(R.id.italicButton).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				EditText edit = ((EditText) findViewById(R.id.bodyEditText));
				int startSelection = edit.getSelectionEnd() < edit.getSelectionStart() ? edit.getSelectionEnd() : edit.getSelectionStart();
				int endSelection = edit.getSelectionEnd() > edit.getSelectionStart() ? edit.getSelectionEnd() : edit.getSelectionStart();
				if (startSelection == endSelection) {
					edit.getText().insert(startSelection, "<i></i>");
					edit.setSelection(startSelection + 3);
				} else {
					edit.getText().insert(endSelection, "</i>");
					edit.getText().insert(startSelection, "<i>");
				}
			}
		});

		findViewById(R.id.strikeButton).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				EditText edit = ((EditText) findViewById(R.id.bodyEditText));
				int startSelection = edit.getSelectionEnd() < edit.getSelectionStart() ? edit.getSelectionEnd() : edit.getSelectionStart();
				int endSelection = edit.getSelectionEnd() > edit.getSelectionStart() ? edit.getSelectionEnd() : edit.getSelectionStart();
				if (startSelection == endSelection) {
					edit.getText().insert(startSelection, "<s></s>");
					edit.setSelection(startSelection + 3);
				} else {
					edit.getText().insert(endSelection, "</s>");
					edit.getText().insert(startSelection, "<s>");
				}
			}
		});

		findViewById(R.id.userButton).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				((MainActivity) getContext()).onCommand(MainActivity.COMMAND_INSERT_LJ_USER);
			}
		});

		findViewById(R.id.ljcutButton).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				((MainActivity) getContext()).onCommand(MainActivity.COMMAND_INSERT_LJ_CUT);
			}
		});

		findViewById(R.id.attachButton).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				((MainActivity) getContext()).onCommand(MainActivity.COMMAND_PICK_PHOTO);
			}
		});


		findViewById(R.id.linkButton).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				((MainActivity) getContext()).onCommand(MainActivity.COMMAND_INSERT_LINK);
			}
		});

	}

	public void insertToBody(String value) {
		EditText edit = ((EditText) findViewById(R.id.bodyEditText));
		int startSelection = edit.getSelectionEnd() < edit.getSelectionStart() ? edit.getSelectionEnd() : edit.getSelectionStart();
		int endSelection = edit.getSelectionEnd() > edit.getSelectionStart() ? edit.getSelectionEnd() : edit.getSelectionStart();
		if (startSelection == endSelection) {
			edit.getText().insert(startSelection, value);
			edit.setSelection(startSelection + value.length());
		} else {
			edit.getText().insert(endSelection, value);
		}
	}


	public void replaceBody(String value) {
		EditText edit = ((EditText) findViewById(R.id.bodyEditText));
		int startSelection = edit.getSelectionEnd() < edit.getSelectionStart() ? edit.getSelectionEnd() : edit.getSelectionStart();
		int endSelection = edit.getSelectionEnd() > edit.getSelectionStart() ? edit.getSelectionEnd() : edit.getSelectionStart();
		edit.getText().delete(startSelection, endSelection);
		edit.getText().insert(startSelection, value);
		edit.setSelection(startSelection + value.length());
	}

	public String getSelectedText() {
		EditText edit = ((EditText) findViewById(R.id.bodyEditText));
		int startSelection = edit.getSelectionEnd() < edit.getSelectionStart() ? edit.getSelectionEnd() : edit.getSelectionStart();
		int endSelection = edit.getSelectionEnd() > edit.getSelectionStart() ? edit.getSelectionEnd() : edit.getSelectionStart();
		return edit.getText().toString().substring(startSelection, endSelection);
	}

	public void setEvent(Event event) {
		_event = event;
		if (_event == null) {
			return;
		}
		((EditText) findViewById(R.id.subjectEditText)).setText(_event.subject);
		((EditText) findViewById(R.id.bodyEditText)).setText(_event.body);
	}

	public void commitChanges() {
		if (_event == null) {
			return;
		}
		_event.subject = ((EditText) findViewById(R.id.subjectEditText)).getText().toString();
		_event.body = ((EditText) findViewById(R.id.bodyEditText)).getText().toString();
	}
}
