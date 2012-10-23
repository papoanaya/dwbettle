package com.dotcypress.ljbeetle.model;

import com.dotcypress.database.EntityBase;

public class Tag extends EntityBase {
	public String journal;
	public String value;

	public Tag() {

	}

	public Tag(String journal, String value) {
		this.journal = journal;
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}
}
