package io.papermc.hangar.modelold;

@Deprecated(forRemoval = true)
public enum DownloadType {

    /**
     * The download was for the file that was originally uploaded.
     */
    UPLOADED_FILE,

    /**
     * The download was for just the JAR file of the upload.
     */
    JAR_FILE,

    /**
     * The download is on an external site.
     */
    EXTERNAL_DOWNLOAD
}
