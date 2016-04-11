package net.codepixl.GLCraft.launcher;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Observable;

import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;

// This class downloads a file from a URL.
class Download extends Observable implements Runnable {

// Max size of download buffer.
private static final int MAX_BUFFER_SIZE = 1024;

// These are the status names.
public static final String STATUSES[] = {"Downloading",
"Paused", "Complete", "Cancelled", "Error"};

// These are the status codes.
public static final int DOWNLOADING = 0;
public static final int PAUSED = 1;
public static final int COMPLETE = 2;
public static final int CANCELLED = 3;
public static final int ERROR = 4;

private String dest;
private ProgressBar pb;
private ProgressIndicator pi;
private URL url; // download URL
private int size; // size of download in bytes
private int downloaded; // number of bytes downloaded
private int status; // current status of download

// Constructor for Download.
public Download(String url, String dest, ProgressBar pb, ProgressIndicator pi) {
    try {
		this.url = new URL(url);
	} catch (MalformedURLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    this.dest = dest.replace('\\', '/');
    this.pb = pb;
    this.pi = pi;
    this.pb.setVisible(true);
    this.pi.setVisible(true);
    size = -1;
    downloaded = 0;
    status = DOWNLOADING;

    // Begin the download.
    download();
}

// Get this download's URL.
public String getUrl() {
    return url.toString();
}

// Get this download's size.
public int getSize() {
    return size;
}

// Get this download's progress.
public float getProgress() {
    return ((float) downloaded / size) * 100;
}

// Get this download's status.
public int getStatus() {
    return status;
}

// Pause this download.
public void pause() {
    status = PAUSED;
    stateChanged();
}

// Resume this download.
public void resume() {
    status = DOWNLOADING;
    stateChanged();
    download();
}

// Cancel this download.
public void cancel() {
    status = CANCELLED;
    stateChanged();
}

// Mark this download as having an error.
private void error() {
    status = ERROR;
    stateChanged();
}

// Start or resume downloading.
private void download() {
    Thread thread = new Thread(this);
    thread.start();
}

// Download file.
public void run() {
    RandomAccessFile file = null;
    InputStream stream = null;

    try {
        // Open connection to URL.
        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Range",
                "bytes=" + downloaded + "-");
        connection.addRequestProperty("User-Agent", "CrapExplorer/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");

        // Connect to server.
        connection.connect();

        // Make sure response code is in the 200 range.
        if (connection.getResponseCode() / 100 != 2) {
            error();
        }

        // Check for valid content length.
        int contentLength = connection.getContentLength();
        if (contentLength < 1) {
            error();
        }

  /* Set the size for this download if it
     hasn't been already set. */
        if (size == -1) {
            size = contentLength;
            stateChanged();
        }

        // Open file and seek to the end of it.
        new File(this.dest).getParentFile().mkdirs();
        file = new RandomAccessFile(this.dest, "rw");
        file.seek(downloaded);

        stream = connection.getInputStream();
        while (status == DOWNLOADING) {
    /* Size buffer according to how much of the
       file is left to download. */
            byte buffer[];
            if (size - downloaded > MAX_BUFFER_SIZE) {
                buffer = new byte[MAX_BUFFER_SIZE];
            } else {
                buffer = new byte[size - downloaded];
            }

            // Read from server into buffer.
            int read = stream.read(buffer);
            if (read == -1)
                break;

            // Write buffer to file.
            file.write(buffer, 0, read);
            downloaded += read;
            stateChanged();
            this.pb.setProgress(this.getProgress()/100);
        }

  /* Change status to complete if this point was
     reached because downloading has finished. */
        if (status == DOWNLOADING) {
        	this.pi.setVisible(false);
        	this.pb.setVisible(false);
            status = COMPLETE;
            stateChanged();
        }
    } catch (Exception e) {
        error();
    } finally {
        // Close file.
        if (file != null) {
            try {
                file.close();
            } catch (Exception e) {}
        }

        // Close connection to server.
        if (stream != null) {
            try {
                stream.close();
            } catch (Exception e) {}
        }
    }
}

// Notify observers that this download's status has changed.
private void stateChanged() {
    setChanged();
    notifyObservers();
}
}