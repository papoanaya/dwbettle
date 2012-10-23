package com.dotcypress.ljbeetle.model;

import com.dotcypress.database.EntityBase;

public class AccessedJournal extends EntityBase {
	public String journal;
	public String user;

	public AccessedJournal() {

	}

	public AccessedJournal(String user, String journal) {
		this.journal = journal;
		this.user = user;
	}

	@Override
	public String toString() {
		return journal;
	}
}
