package com.dotcypress.dwbeetle.model;

import com.dotcypress.database.EntityBase;

import java.util.ArrayList;

public class User extends EntityBase {
	public String userName;
	public String passwordHash;
	public String defaultUserpicName;

	public ArrayList<AccessedJournal> journals;
	public ArrayList<Userpic> userpics;
}
