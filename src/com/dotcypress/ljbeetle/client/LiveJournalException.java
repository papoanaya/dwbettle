package com.dotcypress.ljbeetle.client;

public class LiveJournalException extends Exception {
	public String message;
	public int messageId;

	public LiveJournalException(String errorMessage) {
		message = errorMessage;
	}

	public LiveJournalException(int errorMessageId) {
		messageId = errorMessageId;
	}

	private static final long serialVersionUID = 14512356786l;

}
