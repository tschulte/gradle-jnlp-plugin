package de.gliderpilot.jnlp.servlet;

import java.net.URL;

/**
 * Created by tobias on 3/29/15.
 */
public class JnlpResource {

    private final URL file;
    private final String version;
    private final String contentType;
    private final String contentEncoding;

    public JnlpResource(URL file, String version, String contentType, String contentEncoding) {
        if (file == null)
            throw new IllegalArgumentException("file must not be null");
        this.file = file;
        this.version = version;
        this.contentType = contentType;
        this.contentEncoding = contentEncoding;
    }

    public URL getFile() {
        return file;
    }

    public String getVersion() {
        return version;
    }

    public String getContentType() {
        return contentType;
    }

    public String getContentEncoding() {
        return contentEncoding;
    }
}
