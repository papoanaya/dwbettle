package com.dotcypress.ljbeetle.client;

import com.dotcypress.ljbeetle.R;
import com.dotcypress.ljbeetle.core.Config;
import com.dotcypress.ljbeetle.core.Logger;
import com.dotcypress.ljbeetle.core.Utils;
import com.dotcypress.ljbeetle.model.AccessedJournal;
import com.dotcypress.ljbeetle.model.Event;
import com.dotcypress.ljbeetle.model.User;
import com.dotcypress.ljbeetle.model.Userpic;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class LjClient {

	private static final String SERVER_URI = Config.SERVER_URI;
	private static final String MOBILE_SERVER_URI = Config.MOBILE_SERVER_URI;

	private static final int DOWNLOAD_BUFFER_SIZE = 100 * 1024;

	private User _user;
	private String _challenge = "";

	public static LjClient login(User user) {
		LjClient client = new LjClient();
		client._user = user;
		return client;
	}

	public static LjClient login(String username, String passwordHash) throws LiveJournalException {
		LjClient client = new LjClient();
		User user = new User();
		user.userName = username;
		user.passwordHash = passwordHash;
		client._user = user;

		OperationParams params = client.generateCommonParams();
		params.put("mode", "login");
		params.put("getpickws", "1");
		params.put("getpickwurls", "1");

		HashMap<String, String> result = client.sendRequest(params);
		if (result == null) {
			return null;
		}
		user.journals = new ArrayList<AccessedJournal>();
		user.journals.add(new AccessedJournal(username, username));
		if (result.containsKey("access_count")) {
			int accesedCount = Integer.parseInt(result.get("access_count"));
			for (int pos = 1; pos < accesedCount + 1; pos++) {
				user.journals.add(new AccessedJournal(username, result.get("access_" + pos)));
			}
		}
		user.userpics = new ArrayList<Userpic>();
		String defaultUserpicUrl = result.get("defaultpicurl");
		if (result.containsKey("pickw_count")) {
			int upicsCount = Integer.parseInt(result.get("pickw_count"));
			for (int pos = 0; pos < upicsCount; pos++) {
				Userpic userpic = new Userpic();
				userpic.journal = username;
				userpic.name = result.get("pickw_" + (pos + 1));
				userpic.url = result.get("pickwurl_" + (pos + 1));
				if (defaultUserpicUrl.equals(userpic.url)) {
					user.defaultUserpicName = userpic.name;
				}
				user.userpics.add(userpic);
			}
		}
		return client;
	}

	public User getCurrentUser() {
		return _user;
	}

	public boolean postEvent(Event event, String enclosure) throws LiveJournalException {
		OperationParams params = generateCommonParams();
		params.put("mode", "postevent");
		params.put("subject", event.subject);
		params.put("event", event.body + (enclosure != null ? "\n\n" + enclosure : ""));
		params.put("lineendings", "\n");
		if (event.journal != null && event.journal.length() > 0) {
			params.put("usejournal", event.journal);
		}

		Calendar date = Calendar.getInstance();
		date.setTimeInMillis(event.timestamp);
		params.put("year", String.valueOf(date.get(Calendar.YEAR)));
		params.put("mon", String.valueOf(date.get(Calendar.MONTH) + 1));
		params.put("day", String.valueOf(date.get(Calendar.DAY_OF_MONTH)));
		params.put("hour", String.valueOf(date.get(Calendar.HOUR_OF_DAY)));
		params.put("min", String.valueOf(date.get(Calendar.MINUTE)));
		params.put("prop_useragent", Config.USER_AGENT);
		if (event.userpic != null) {
			params.put("prop_picture_keyword", event.userpic);
		}
		if (event.tags != null) {
			params.put("prop_taglist", event.tags);
		}
		if (event.location != null) {
			params.put("prop_current_location", event.location);
		}
		if (event.mood != null) {
			params.put("prop_current_mood", event.mood);
		}
		if (event.music != null) {
			params.put("prop_current_music", event.music);
		}
		if (event.screening > 0) {
			params.put("prop_opt_screening", "A");
		}
		if (event.nocomments > 0) {
			params.put("prop_opt_nocomments", "1");
		}
		switch (event.privacy) {
			case 1:
				params.put("security", "public");
				break;
			case 2:
				params.put("security", "usemask");
				params.put("allowmask", "1");
				break;
			case 3:
				params.put("security", "private");
				break;
		}
		HashMap<String, String> result = sendRequest(params);
		if (result != null) {
			event.url = result.get("url");
			return true;
		}
		return false;
	}

	public String[] loadTags(String journal) throws LiveJournalException {
		OperationParams params = generateCommonParams();
		params.put("mode", "getusertags");
		params.put("usejournal", journal);
		HashMap<String, String> result = sendRequest(params);
		if (result != null && result.containsKey("tag_count")) {
			int tagsCount = Integer.parseInt(result.get("tag_count"));
			String[] tagsValues = new String[tagsCount];
			for (int pos = 0; pos < tagsCount; pos++) {
				tagsValues[pos] = result.get("tag_" + (pos + 1) + "_name");
			}
			Logger.verbose(String.format("Loaded %s tag(s) for journal: %s", tagsCount, journal));
			return tagsValues;
		}
		return new String[0];
	}

	public ArrayList<Event> getFriendsEvents() throws LiveJournalException {

		OperationParams params = generateCommonParams();
		ArrayList<Event> events = new ArrayList<Event>();
		params.put("mode", "getfriendspage");
		HashMap<String, String> result = sendRequest(params);

		if (result != null && result.containsKey("entries_count")) {
			int entriesCount = Integer.parseInt(result.get("entries_count"));
			for (int pos = 1; pos <= entriesCount; pos++) {
				Event event = new Event();
				String prefix = "entries_" + pos + "_";
				event.user = result.get(prefix + "postername");
				event.subject = URLDecoder.decode(result.get(prefix + "subject_raw"));
				event.body = URLDecoder.decode(result.get(prefix + "event"));
				events.add(event);
			}
			Logger.verbose(String.format("Loaded %s entries for friendspage", entriesCount));
		}
		return events;
	}

	private void queryChallenge() throws LiveJournalException {
		OperationParams params = new OperationParams();
		params.put("mode", "getchallenge");
		HashMap<String, String> result = sendRequest(params);
		if (result == null || !result.containsKey("challenge")) {
			throw new LiveJournalException(R.string.common_error);
		}
		_challenge = result.get("challenge");
	}

	private OperationParams generateCommonParams() throws LiveJournalException {
		queryChallenge();
		OperationParams params = new OperationParams();
		params.put("user", _user.userName);
		params.put("auth_method", "challenge");
		params.put("ver", "1");
		params.put("auth_challenge", _challenge);
		params.put("auth_response", Utils.md5(_challenge + _user.passwordHash));
		return params;
	}

	private HashMap<String, String> sendRequest(OperationParams operationParams) throws LiveJournalException {
		Logger.verbose(String.format("Service request: %s", operationParams.generateQuery()));
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, Config.CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParams, Config.CONNECTION_TIMEOUT);
		DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
		HttpPost httpPost = new HttpPost(SERVER_URI);

		HttpResponse response;
		try {
			httpPost.setEntity(operationParams.getPostEntity());
			response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream stream = entity.getContent();
				String result = Utils.convertStreamToString(stream);
				stream.close();
				String[] strings = result.split("\n");
				HashMap<String, String> invokeResult = new HashMap<String, String>();
				for (int pos = 0; pos < strings.length - 1; pos += 2) {
					invokeResult.put(strings[pos], strings[pos + 1]);
				}
				if (!invokeResult.get("success").equals("OK")) {
					Logger.warn(invokeResult.get("errmsg"));
					throw new LiveJournalException(invokeResult.get("errmsg"));
				}
				Logger.verbose(String.format("Request success: %s", invokeResult.toString()));
				return invokeResult;
			}
		} catch (LiveJournalException e) {
			throw e;
		} catch (ClientProtocolException e) {
			Logger.error("HTTP error", e);
		} catch (IOException e) {
			Logger.error("HTTP error", e);
		} catch (Throwable e) {
			Logger.error("Download fault", e);
		}
		return null;
	}

	public void downloadUpic(String url, File file) {
		try {

			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setDoInput(true);
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setReadTimeout(Config.CONNECTION_TIMEOUT);
			connection.connect();

			InputStream inputStream = connection.getInputStream();
			BufferedInputStream bufferedNetworkStream = new BufferedInputStream(inputStream, DOWNLOAD_BUFFER_SIZE);
			FileOutputStream fileStream = new FileOutputStream(file);
			BufferedOutputStream bufferedFileStream = new BufferedOutputStream(fileStream);
			byte buf[] = new byte[DOWNLOAD_BUFFER_SIZE];
			int bytesRead;
			do {
				bytesRead = bufferedNetworkStream.read(buf);
				if (bytesRead > 0) {
					bufferedFileStream.write(buf, 0, bytesRead);
				}
			} while (bytesRead > 0);
			bufferedFileStream.flush();
			fileStream.close();
			inputStream.close();
			connection.disconnect();
		} catch (MalformedURLException ex) {
			Logger.error(String.format("Url parsing was failed: %s", url), ex);
		} catch (IOException ex) {
			Logger.error(String.format("IO error. Url:%s.  File:%s", url, file.getAbsolutePath()), ex);
		}
		Logger.verbose(String.format("Upic saved at: %s", file.getAbsolutePath()));
	}
}
