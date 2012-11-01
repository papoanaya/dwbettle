package com.dotcypress.dwbeetle;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import com.dotcypress.dwbeetle.model.Event;

public class EventReaderActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		try {

			Event e = App.getCurrentlyViewedEvent();
			setContentView(R.layout.entry_reader);

			WebView webview = (WebView) findViewById(R.id.entryReaderPanel);
			renderWebView(webview, e.subject, e.body);

		} catch (NullPointerException e) {
			WebView webview = new WebView(this);
			renderWebView(webview, "Error", "<p>An error has occured.</p>" + "<p>Find us at <a href=\"https://bitbucket.org/dotCypress/lj-beetle/wiki/Home\">bitbucket.org</a>"
					+ " and file a bug please.</p>");
		}
	}

	private void renderWebView(WebView view, String title, String body) {
		view.setBackgroundColor(0);
		view.setBackgroundResource(R.drawable.background);
		view.loadDataWithBaseURL(null, getHtml(title, body), "text/html", "utf-8", null);
		setContentView(view);
	}

	private String getHtml(String title, String body) {
		// let's style the page a bit so it looks pretty on the LJ background
		return "<html>" + "<head>" + "<title>" + title + "</title>" + "<style>body { color: #eee; } a { color: lightyellow; } h1 {margin-bottom:.3em;}</style>" + "</head>"
				+ "<body><h1>" + title + "</h1>" + body + "</body>" + "</html>";
	}
}
