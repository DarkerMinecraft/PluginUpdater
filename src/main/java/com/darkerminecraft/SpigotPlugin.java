package com.darkerminecraft;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.FileSystems;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SpigotPlugin {
	
	private final int resourceID;
	private final String version;
	private String updatedVersion;
	
	private String pluginName;
	private boolean isExternal, isPremium;
	
	public SpigotPlugin(int resourceID, String version) {
		this.resourceID = resourceID;
		this.version = version;
		gatheredInformation();
	}
	
	public boolean checkForUpdate() {
		try {
			JsonElement updatedVersion = parseJSON("https://api.spiget.org/v2/resources/" + resourceID + "/versions/latest");
			this.updatedVersion = updatedVersion.getAsJsonObject().get("name").getAsString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return version.equals(updatedVersion);
    }
	
	public DownloadMessage downloadPlugin() {
		if (!checkForUpdate()) {
			try {
				if (!isExternal) {
					if(!isPremium) {
						URL downloadURL = new URL("https://api.spiget.org/v2/resources/" + resourceID + "/download");
						ReadableByteChannel rbc = Channels.newChannel(downloadURL.openStream());
						FileOutputStream fos = new FileOutputStream(FileSystems.getDefault().getPath("").toAbsolutePath() + "/plugins/" + this.pluginName + " [" + this.updatedVersion + "].jar");
						fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
						return DownloadMessage.SUCCESS;
					} else return DownloadMessage.PREMIUMRESOURCE;
				} else return DownloadMessage.EXTERNALDOWNLOAD;
			} catch (Exception e) {
				e.printStackTrace();
				return DownloadMessage.FAILED;
			}
		} else return DownloadMessage.ALREADYUPDATED;
	}
	
	private void gatheredInformation() {
		try {
			JsonElement info = parseJSON("https://api.spiget.org/v2/resources/" + resourceID);
			JsonObject jsonObj = info.getAsJsonObject();
			this.isExternal = jsonObj.get("external").getAsBoolean();
			this.pluginName = jsonObj.get("name").getAsString();
			this.isPremium = jsonObj.get("premium").getAsBoolean();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	private JsonElement parseJSON(String link) throws Exception {
		URL url = new URL(link);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.addRequestProperty("User-Agent", "PLUGINS"); 
		
		InputStream inputStream = connection.getInputStream();
		InputStreamReader reader = new InputStreamReader(inputStream);
		
		JsonElement element = JsonParser.parseReader(reader);

		return element;
	}
	
}

enum DownloadMessage {
		SUCCESS, FAILED, EXTERNALDOWNLOAD, ALREADYUPDATED, PREMIUMRESOURCE
}
