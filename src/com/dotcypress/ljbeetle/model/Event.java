package com.dotcypress.ljbeetle.model;

import com.dotcypress.database.EntityBase;

public class Event extends EntityBase {
	public long timestamp;
	public String user;
	public String journal;
	public String subject;
	public String body;
	public String url;
	public String tags;
	public String userpic;
	public String location;
	public String mood;
	public String music;
	public int privacy;
	public int screening;
	public int nocomments;

	@Override
	public String toString() {
		return "Event [body=" + body + ", subject=" + subject + "]";
	}
}
