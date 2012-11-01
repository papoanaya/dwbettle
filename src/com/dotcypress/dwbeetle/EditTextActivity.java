package com.dotcypress.dwbeetle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class EditTextActivity extends Activity {
	public static final String TITLE_EXTRA = "title";
	public static final String RESULT_EXTRA = "result";
	public static final String DEFAULT_TEXT_EXTRA = "default";
	public static final String CHECK_INPUT_EXTRA = "check input";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.text_dialog);
		Intent intent = getIntent();
		if (!intent.hasExtra(TITLE_EXTRA)) {
			finish();
			return;
		}
		TextView title = (TextView) findViewById(R.id.titleTextView);
		title.setText(intent.getIntExtra(TITLE_EXTRA, 0));

		if (intent.hasExtra(DEFAULT_TEXT_EXTRA)) {
			EditText editText = (EditText) findViewById(R.id.editText);
			editText.setText(intent.getIntExtra(DEFAULT_TEXT_EXTRA, 0));
			editText.selectAll();
		}

		findViewById(R.id.okButton).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				EditText editText = (EditText) findViewById(R.id.editText);
				Intent intent = getIntent();
				if (intent.hasExtra(CHECK_INPUT_EXTRA) && intent.getBooleanExtra(CHECK_INPUT_EXTRA, false) && editText.getText().toString().length() == 0) {
					Toast.makeText(EditTextActivity.this, R.string.enter_text, Toast.LENGTH_LONG).show();
					return;
				}
				Intent result = new Intent();
				result.putExtra(RESULT_EXTRA, editText.getText().toString());
				setResult(Activity.RESULT_OK, result);
				finish();
			}
		});

		findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}
}
