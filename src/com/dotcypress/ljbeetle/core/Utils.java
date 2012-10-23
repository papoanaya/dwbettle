package com.dotcypress.ljbeetle.core;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {

	public static String md5(String phase) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
			digest.update(phase.getBytes());
			byte messageDigest[] = digest.digest();
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String symbol = Integer.toHexString(0xFF & messageDigest[i]);
				while (symbol.length() < 2) {
					symbol = "0" + symbol;
				}
				hexString.append(symbol);
			}
			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			Logger.error("Can't find MD5 algorithm", e);
		}
		return "";
	}

	public static String convertStreamToString(InputStream stream) {
		StringBuilder stringBuilder = new StringBuilder();

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
			String line;
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line).append("\n");
			}
		} catch (IOException e) {
			Logger.error("Error converting HTTP response", e);
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				Logger.error("Error closing HTTP connection", e);
			}
		}
		return stringBuilder.toString();
	}

	public static void copy(File source, File destination) throws IOException {
		if (destination.exists()) {
			destination.delete();
		}
		InputStream in = new FileInputStream(source);
		OutputStream out = new FileOutputStream(destination);

		byte[] buffer = new byte[1024];
		int len;
		while ((len = in.read(buffer)) > 0) {
			out.write(buffer, 0, len);
		}
		in.close();
		out.close();
	}
}