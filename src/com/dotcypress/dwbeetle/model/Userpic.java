package com.dotcypress.dwbeetle.model;

import com.dotcypress.database.EntityBase;
import com.dotcypress.dwbeetle.core.Utils;

public class Userpic extends EntityBase {
	public String journal;
	public String name;
	public String url;

	public String getFileName() {
		if (url == null) {
			return null;
		}
		return Utils.md5(url);
	}
}
