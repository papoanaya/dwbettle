package com.dotcypress.dwbeetle.upload;

import com.dotcypress.dwbeetle.client.LiveJournalException;
import com.dotcypress.dwbeetle.core.Config;
import com.dotcypress.dwbeetle.core.Logger;
import com.dotcypress.dwbeetle.core.Utils;
import com.dotcypress.dwbeetle.model.User;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LiveJournalHosting implements ImageHosting {

	//public static final String URI = "http://pics.livejournal.com/interface/simple";

	public static final String URI = "http://pics.dreamwidth.org/interface/simple";

	private User _currentUser;

	public LiveJournalHosting(User currentUser) {
		_currentUser = currentUser;
	}

	public String upload(String path) throws LiveJournalException {

		File file = new File(path);
		String challenge = getChallenge();
		if (challenge == null) {
			return null;
		}
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, Config.CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParams, Config.CONNECTION_TIMEOUT);
		DefaultHttpClient httpclient = new DefaultHttpClient(httpParams);
		HttpPut httpPut = new HttpPut(URI);
		httpPut.addHeader("X-FB-User", _currentUser.userName);
		httpPut.addHeader("X-FB-Mode", "UploadPic");
		httpPut.addHeader("X-FB-Auth", "crp:" + challenge + ":" + Utils.md5(challenge + _currentUser.passwordHash));
		httpPut.addHeader("X-FB-UploadPic.Meta.Filename", file.getName());

		HttpResponse response;
		try {
			httpPut.setEntity(new InputStreamEntity(new FileInputStream(file), file.length()));
			response = httpclient.execute(httpPut);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream inputStream = entity.getContent();
				DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document dom = builder.parse(inputStream);
				inputStream.close();

				Element root = dom.getDocumentElement();
				NodeList list = root.getElementsByTagName("URL");
				if (list.getLength() > 0) {
					NodeList childs = list.item(0).getChildNodes();
					StringBuilder html = new StringBuilder();
					for (int pos = 0; pos < childs.getLength(); pos++) {
						html.append(childs.item(pos).getNodeValue());
					}
					Logger.verbose(html.toString());
					return String.format("<img src=\"%s\">", html);
				} else {
					list = root.getElementsByTagName("Error");
					if (list.getLength() > 0) {
						NodeList childs = list.item(0).getChildNodes();
						StringBuilder error = new StringBuilder();
						for (int pos = 0; pos < childs.getLength(); pos++) {
							error.append(childs.item(pos).getNodeValue());
						}
						throw new LiveJournalException(error.toString());
					}
				}
			}
		} catch (ClientProtocolException e) {
			Logger.error("HTTP error", e);
		} catch (IOException e) {
			Logger.error("HTTP error", e);
		} catch (ParserConfigurationException e) {
			Logger.error("Parser error", e);
		} catch (FactoryConfigurationError e) {
			Logger.error("Parser error", e);
		} catch (SAXException e) {
			Logger.error("Parser error", e);
		}

		return null;
	}

	public String getChallenge() {
		Logger.verbose(String.format("Challenge request: %s", URI));
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, Config.CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParams, Config.CONNECTION_TIMEOUT);
		DefaultHttpClient httpclient = new DefaultHttpClient(httpParams);
		HttpGet httpGet = new HttpGet(URI);
		httpGet.addHeader("X-FB-User", _currentUser.userName);
		httpGet.addHeader("X-FB-Mode", "GetChallenge");
		HttpResponse response;
		try {
			response = httpclient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream inputStream = entity.getContent();

				DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document dom = builder.parse(inputStream);
				inputStream.close();

				Element root = dom.getDocumentElement();
				NodeList list = root.getElementsByTagName("Challenge");
				if (list.getLength() > 0) {
					NodeList childs = list.item(0).getChildNodes();
					StringBuilder challenge = new StringBuilder();
					for (int pos = 0; pos < childs.getLength(); pos++) {
						challenge.append(childs.item(pos).getNodeValue());
					}
					return challenge.toString();
				}
			}
		} catch (ClientProtocolException e) {
			Logger.error("HTTP error", e);
		} catch (IOException e) {
			Logger.error("HTTP error", e);
		} catch (Throwable e) {
			Logger.error("Download fault", e);
		}
		return null;
	}
}
