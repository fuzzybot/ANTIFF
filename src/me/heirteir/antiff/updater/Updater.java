package me.heirteir.antiff.updater;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import me.heirteir.antiff.config.Configurations;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public final class Updater {

    private static final Pattern FILE_VERSION_SPLITTER = Pattern.compile("^v|[\\s_-]v");
    public static final String CURSE_URL = "https://api.curseforge.com/servermods/", CURSE_PROJECT_URL = (CURSE_URL + "projects?search="), CURSE_FILES_URL = (CURSE_URL + "files?projectIds="),
	    ID_KEY = "id", NAME_KEY = "name", DOWNLOAD_URL_KEY = "downloadUrl", ERROR_GETTING_ID = "There was an error checking for the project ID of this plugin",
	    ERROR_GETTING_LATEST_FILE = "There was an error checking for the latest version of this plugin",
	    ERROR_INVALID_FILE_NAME = "The file name listed on dev.bukkit.org is incorrect. The format should be 'PluginName v1.0' (got '%s' instead)",
	    INFO_UPDATE_AVAILABLE = "There is a new update for Anti-ForceField come check it out it's probably AWESOME!", INFO_UPDATE_AVAILABLE_DL = "Download the latest version at %s",
	    INFO_DOWNLOADING_FILE = "Downloading the latest version...", INFO_DOWNLOADED_FILE = "Finished downloading";

    private final PluginDescriptionFile description;
    private final Logger logger;
    private final boolean shouldDownload;

    public Updater(Plugin plugin, boolean shouldDownload) {
	description = plugin.getDescription();
	logger = plugin.getLogger();
	this.shouldDownload = shouldDownload;
    }

    /**
     * Performs an update check.
     */
    public void performUpdateCheck() {
	if (!shouldUpdate())
	    return;

	// NEW THREAD
	new Thread(new Runnable() {
	    @Override
	    public void run() {
		final int projectId = getProjectId(description.getName());

		if (projectId == -1)
		    return;

		final JSONObject latestFile = getLatestFileOfProject(projectId);

		if (latestFile.size() == 0)
		    return;

		final String fileName = latestFile.get(NAME_KEY).toString();
		final String fileUrl = latestFile.get(DOWNLOAD_URL_KEY).toString();
		String fileVersion;

		final String[] fileVersionSplit = FILE_VERSION_SPLITTER.split(fileName);
		if (fileVersionSplit.length == 2)
		    fileVersion = fileVersionSplit[1].split(Pattern.quote(" "))[0];
		else {
		    logger.warning(String.format(ERROR_INVALID_FILE_NAME, fileName));
		    return;
		}

		if (description.getVersion().equalsIgnoreCase(fileVersion))
		    return;

		logger.info(INFO_UPDATE_AVAILABLE);

		final String pluginWebsite = description.getWebsite();

		if (shouldDownload) {
		    logger.info(INFO_DOWNLOADING_FILE);

		    final File downloadedFile = downloadFile(fileUrl, Bukkit.getUpdateFolderFile());

		    if (downloadedFile.getName().endsWith(".zip"))
			unzipFile(downloadedFile);

		    logger.info(INFO_DOWNLOADED_FILE);
		} else if (pluginWebsite != null) {
		    logger.info(String.format(INFO_UPDATE_AVAILABLE_DL, pluginWebsite));
		}
	    }
	}).start();
    }

    private boolean shouldUpdate() {
	return Configurations.informUpdate();
    }

    private int getProjectId(String project) {
	final JSONArray idResponse = readUrl(CURSE_PROJECT_URL + project);

	if (idResponse.size() == 0)
	    logger.warning(ERROR_GETTING_ID);
	for (Object entry : idResponse) {
	    final JSONObject entryObject = (JSONObject) entry;
	    final String entryName = entryObject.get(NAME_KEY).toString();

	    if (entryName.equalsIgnoreCase(project))
		return Integer.parseInt(entryObject.get(ID_KEY).toString());
	}
	return -1;
    }

    private JSONObject getLatestFileOfProject(int projectId) {
	final JSONArray filesResponse = readUrl(CURSE_FILES_URL + projectId);

	if (filesResponse.size() == 0) {
	    logger.warning(ERROR_GETTING_LATEST_FILE);
	    return new JSONObject();
	}
	return (JSONObject) filesResponse.get(filesResponse.size() - 1);
    }

    private void createFile(File file) {
	file.getParentFile().mkdirs();
	try {
	    file.createNewFile();
	} catch (IOException ex) {
	    logError(ex);
	}
    }

    private JSONArray readUrl(String url) {
	URLConnection urlConnection;
	BufferedReader in;
	try {
	    urlConnection = new URL(url.toLowerCase()).openConnection();
	    in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
	    return (JSONArray) JSONValue.parse(in);
	} catch (IOException ex) {
	    logError(ex);
	}
	return new JSONArray();
    }

    private File downloadFile(String urlPath, File destFolder) {
	URL url;
	try {
	    url = new URL(urlPath);
	} catch (MalformedURLException ex) {
	    logError(ex);
	    return destFolder;
	}

	final String fileName = StringUtils.substringAfterLast(url.getFile(), "/");
	final File localFile = new File(destFolder, fileName);
	final byte[] data = new byte[1024];
	BufferedInputStream in = null;
	FileOutputStream out = null;

	createFile(localFile);
	try {
	    in = new BufferedInputStream(url.openStream());
	    out = new FileOutputStream(localFile);

	    int count;
	    while ((count = in.read(data, 0, data.length)) != -1)
		out.write(data, 0, count);
	} catch (IOException ex) {
	    logError(ex);
	} finally {
	    closeQuietly(in);
	    closeQuietly(out);
	}
	return localFile;
    }

    private void unzipFile(File toUnzip) {
	ZipFile zipFile = null;
	InputStream in = null;
	FileOutputStream out = null;

	try {
	    zipFile = new ZipFile(toUnzip);

	    final Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();

	    while (zipEntries.hasMoreElements()) {
		final ZipEntry zipEntry = zipEntries.nextElement();
		String zipEntryName = zipEntry.getName();
		if (zipEntryName.contains("/"))
		    zipEntryName = StringUtils.substringAfterLast(zipEntryName, "/");

		if (zipEntryName.endsWith(".jar")) {
		    final File zipEntryDest = new File(Bukkit.getUpdateFolderFile(), zipEntryName);
		    final byte[] data = new byte[1024];

		    createFile(zipEntryDest);
		    in = zipFile.getInputStream(zipEntry);
		    out = new FileOutputStream(zipEntryDest);

		    int count;
		    while ((count = in.read(data)) != -1)
			out.write(data, 0, count);
		}
	    }
	} catch (IOException ex) {
	    logError(ex);
	} finally {
	    closeQuietly(in);
	    closeQuietly(out);
	    if (zipFile != null) {
		try {
		    zipFile.close();
		} catch (IOException ignored) {
		}
	    }
	    toUnzip.delete();
	}
    }

    private void closeQuietly(Closeable closeable) {
	if (closeable != null) {
	    try {
		closeable.close();
	    } catch (IOException ignored) {
	    }
	}
    }

    private void logError(Exception ex) {
	logger.log(Level.SEVERE, ex.getMessage(), ex);
    }

}
