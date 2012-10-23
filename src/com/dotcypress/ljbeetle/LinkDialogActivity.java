package com.dotcypress.ljbeetle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class LinkDialogActivity extends Activity {
	public static final String LABEL_1 = "label1";
	public static final String LABEL_2 = "label2";
	public static final String VALUE_1 = "value";
	public static final String VALUE_2 = "hello, Vitalya";
	public static final String DEFAULT_TEXT_EXTRA = "default";
	public static final String CHECK_INPUT_EXTRA = "check input";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.link_dialog);
		Intent intent = getIntent();
		if (!(intent.hasExtra(LABEL_1) && intent.hasExtra(LABEL_2))) {
			finish();
			return;
		}
		TextView label1 = (TextView) findViewById(R.id.dialog_label_1);
		TextView label2 = (TextView) findViewById(R.id.dialog_label_2);

		label1.setText(intent.getIntExtra(LABEL_1, 0));
		label2.setText(intent.getIntExtra(LABEL_2, 0));

		if (intent.hasExtra(VALUE_2)) { // user selected some text
			EditText editText = (EditText) findViewById(R.id.edit_text2_ex);
			editText.setText(intent.getStringExtra(VALUE_2));
			editText.selectAll();
		}

		findViewById(R.id.okButton_ex).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				EditText editText1 = (EditText) findViewById(R.id.edit_text1_ex);
				EditText editText2 = (EditText) findViewById(R.id.edit_text2_ex);
				//Intent intent = getIntent();
				/*if (intent.hasExtra(CHECK_INPUT_EXTRA) && intent.getBooleanExtra(CHECK_INPUT_EXTRA, false) && editText.getText().toString().length() == 0) {
										Toast.makeText(DialogExtendedActivity .this, R.string.enter_text, Toast.LENGTH_LONG).show();
										return;
									}*/
				Intent result = new Intent();
				result.putExtra(VALUE_1, editText1.getText().toString());
				result.putExtra(VALUE_2, editText2.getText().toString());
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
