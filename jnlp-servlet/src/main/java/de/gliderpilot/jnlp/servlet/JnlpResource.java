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
