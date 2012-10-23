package com.dotcypress.ljbeetle.upload;

import com.dotcypress.ljbeetle.core.Config;
import com.dotcypress.ljbeetle.core.Logger;
import org.apache.http.client.ClientProtocolException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageShackHosting implements ImageHosting {

	private static final String API_KEY = "02356HJW5bb940c631717017ec1feab721840177";
	private static final String URI = "http://www.imageshack.us/upload_api.php";

	public String upload(String localPath) {
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****ISBoundarydoyCypress*******";

		try {

			FileInputStream fileInputStream = new FileInputStream(localPath);
			URL url = new URL(URI);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(Config.CONNECTION_TIMEOUT);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			connection.setRequestMethod("POST");

			connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
			connection.connect();

			DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
			outputStream.writeBytes(twoHyphens + boundary + lineEnd);
			outputStream.writeBytes("Content-Disposition: form-data; name=\"key\"" + lineEnd + lineEnd);
			outputStream.writeBytes(API_KEY + lineEnd);

			outputStream.writeBytes(twoHyphens + boundary + lineEnd);
			outputStream.writeBytes("Content-Disposition: form-data; name=\"fileupload\"; filename=\"file.jpg\"" + lineEnd);
			outputStream.writeBytes("Content-Type: image/jpeg" + lineEnd);
			outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd + lineEnd);
			int bytesAvailable;
			while ((bytesAvailable = fileInputStream.available()) > 0) {
				int bufferSize = Math.min(bytesAvailable, 4096);
				byte[] buffer = new byte[bufferSize];
				int bytesRead = fileInputStream.read(buffer, 0, bufferSize);
				outputStream.write(buffer, 0, bytesRead);
			}
			outputStream.writeBytes(lineEnd + twoHyphens + boundary + twoHyphens + lineEnd);
			fileInputStream.close();
			outputStream.flush();
			outputStream.close();
			int respcode = connection.getResponseCode();
			Logger.verbose(String.format("ImageShack response code: %s", respcode));

			InputStream inputStream = connection.getInputStream();

			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document dom = builder.parse(inputStream);
			inputStream.close();

			Element root = dom.getDocumentElement();
			NodeList list = root.getElementsByTagName("image_link");
			if (list.getLength() > 0) {
				NodeList childs = list.item(0).getChildNodes();
				StringBuilder html = new StringBuilder();
				for (int pos = 0; pos < childs.getLength(); pos++) {
					html.append(childs.item(pos).getNodeValue());
				}
				Logger.verbose(html.toString());
				return String.format("<img src=\"%s\">", html);
			}

		} catch (ClientProtocolException e) {
			Logger.error("ImageShackHosting HTTP error", e);
		} catch (IOException e) {
			Logger.error("ImageShackHosting HTTP error", e);
		} catch (Throwable e) {
			Logger.error("ImageShackHosting upload fault ", e);
		}
		return null;
	}
}
