package com.dotcypress.ljbeetle.client;

import android.util.Xml.Encoding;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OperationParams extends HashMap<String, String> {

	private static final long serialVersionUID = 1064551409148229285L;

	public String generateQuery() {
		String result = "";
		Object[] keys = this.keySet().toArray();
		for (Object key : keys) {
			result += String.format("%s=%s&", key, get(key));
		}
		return result;
	}

	public UrlEncodedFormEntity getPostEntity() throws UnsupportedEncodingException {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		Object[] keys = this.keySet().toArray();
		for (Object key : keys) {
			nameValuePairs.add(new BasicNameValuePair(key.toString(), get(key)));
		}
		return new UrlEncodedFormEntity(nameValuePairs, Encoding.UTF_8.name());
	}
}
