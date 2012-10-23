package com.dotcypress.ljbeetle.upload;

import com.dotcypress.ljbeetle.client.LiveJournalException;

public interface ImageHosting {
	public String upload(String localPath) throws LiveJournalException;
}
