/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
public class JnlpRequestHandler {

    protected final ServletContext context;
    protected final String filePath;
    protected final String requestedVersion;

    protected String contentType;
    protected String contentEncoding;
    protected URL file;

    public JnlpRequestHandler(HttpServletRequest req) {
        context = req.getServletContext();
        filePath = req.getServletPath();
        requestedVersion = req.getParameter("version-id");
    }

    public long getLastModified() {
        findResource();
        if (file == null) {
            return -1;
        }
        try {
            return file.openConnection().getLastModified();
        } catch (IOException e) {
            return -1;
        }
    }

    public void doGet(HttpServletResponse resp) throws IOException {
        findResource();
        if (file == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (contentType != null) {
            resp.setContentType(contentType);
        }

        if (contentEncoding != null) {
            resp.setHeader("Content-Encoding", contentEncoding);
        }

        if (requestedVersion != null) {
            resp.setHeader("x-java-jnlp-version-id", requestedVersion);
        }

        try (InputStream is = file.openConnection().getInputStream()) {
            copy(is, resp.getOutputStream());
        }
    }


    protected void findResource() {
        if (file != null)
            return;
        try {
            if (requestedVersion != null) {
                file = context.getResource(filePath.replaceAll("(\\..*)$", "__V" + requestedVersion + "$1"));
            } else {
                file = context.getResource(filePath);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void copy(InputStream source, OutputStream target) throws IOException {
        byte[] buf = new byte[8192];
        int n;
        while ((n = source.read(buf)) > 0) {
            target.write(buf, 0, n);
        }
    }


}
