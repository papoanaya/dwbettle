package com.dotcypress.ljbeetle.core;

import com.dotcypress.database.DatabaseDispatcher;
import com.dotcypress.ljbeetle.model.AccessedJournal;
import com.dotcypress.ljbeetle.model.Tag;
import com.dotcypress.ljbeetle.model.User;
import com.dotcypress.ljbeetle.model.Userpic;

public class DbUtils {

	public static void syncTags(DatabaseDispatcher databaseDispatcher, String journal, String[] tags) {
		if (tags.length == 0) {
			return;
		}
		databaseDispatcher.delete(Tag.class, String.format("journal='%s'", journal));
		for (int pos = 0; pos < tags.length; pos++) {
			Tag tag = new Tag(journal, tags[pos]);
			databaseDispatcher.save(tag);
		}
	}

	public static void syncUser(DatabaseDispatcher databaseDispatcher, User user) {
		User existsUser = databaseDispatcher.getEntity(User.class, String.format("userName='%s' AND passwordHash='%s'", user.userName, user.passwordHash));
		if (existsUser != null) {
			user.id = existsUser.id;
		}
		databaseDispatcher.save(user);

		databaseDispatcher.delete(AccessedJournal.class, String.format("user='%s'", user.userName));
		for (AccessedJournal journal : user.journals) {
			databaseDispatcher.save(journal);
		}

		databaseDispatcher.delete(Userpic.class, String.format("journal='%s'", user.userName));
		for (Userpic upic : user.userpics) {
			databaseDispatcher.save(upic);
		}
	}

	public static void loadUserData(DatabaseDispatcher databaseDispatcher, User user) {
		user.journals = databaseDispatcher.getEntities(AccessedJournal.class, String.format("user='%s'", user.userName));
		user.userpics = databaseDispatcher.getEntities(Userpic.class, String.format("journal='%s'", user.userName));
	}
}
