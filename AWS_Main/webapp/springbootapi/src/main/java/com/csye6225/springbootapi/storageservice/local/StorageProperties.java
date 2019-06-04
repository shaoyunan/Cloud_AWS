package com.csye6225.springbootapi.storageservice.local;

import java.io.File;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("storage")
public class StorageProperties {

	/**
	 * Folder location for storing files
	 */
	private String location = System.getenv("CATALINA_HOME") + "/temp";

	public String getLocation() {
		File dir = new File(location);
		if (!dir.exists())
			dir.mkdirs();
		// return dir.getAbsolutePath();
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

}
