package com.dotcypress.dwbeetle.upload;

import android.content.Context;

import com.dotcypress.dwbeetle.App;

public class ImageHostingRegistry {

	public static ImageHosting getProvider(Context context, String index) {
		if (index.equals("imageshack")) {
			return new ImageShackHosting();
		}
		if (index.equals("livejournal")) {

			if (context != null && context.getApplicationContext() != null) {
				App applicationContext = (App) context.getApplicationContext();
				if (applicationContext.client != null && applicationContext.client.getCurrentUser() != null) {
					return new LiveJournalHosting(applicationContext.client.getCurrentUser());
				}
			}
		}
		return null;
	}
}
