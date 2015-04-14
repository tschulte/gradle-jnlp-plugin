package de.gliderpilot.jnlp.servlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by tobias on 3/29/15.
 */
public class JnlpJarRequestHandler extends JnlpRequestHandler {


    private final String currentVersion;

    public JnlpJarRequestHandler(HttpServletRequest req) {
        super(req);
        currentVersion = req.getParameter("current-version-id");
    }

    protected void findResource() {
        if (file != null)
            return;
        try {
            if (requestedVersion != null && currentVersion != null) {
                findResource(filePath.replaceAll("\\.jar$", "__V" + currentVersion + "__V" + requestedVersion + ".diff.jar"));
                if (file != null) {
                    contentType = "application/x-java-archive-diff";
                    return;
                }
            }
            if (requestedVersion != null) {
                findResource(filePath.replaceAll("\\.jar$", "__V" + requestedVersion + ".jar"));
            } else {
                findResource(filePath);
            }
            if (file != null) {
                contentType = "application/x-java-archive";
            }
        } catch (MalformedURLException e) {

        }
    }

    private void findResource(String filePath) throws MalformedURLException {
        file = context.getResource(filePath + ".pack.gz");
        if (file != null) {
            contentEncoding = "pack200-gzip";
            return;
        }
        file = context.getResource(filePath);
    }

}
