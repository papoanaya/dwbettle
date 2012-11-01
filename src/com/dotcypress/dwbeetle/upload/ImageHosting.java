package com.dotcypress.dwbeetle.upload;

import com.dotcypress.dwbeetle.client.LiveJournalException;

public interface ImageHosting {
	public String upload(String localPath) throws LiveJournalException;
}
