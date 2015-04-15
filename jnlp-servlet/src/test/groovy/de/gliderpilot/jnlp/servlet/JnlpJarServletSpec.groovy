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
package de.gliderpilot.jnlp.servlet

import spock.lang.Specification

/**
 * Created by tobias on 4/12/15.
 */
class JnlpJarServletSpec extends Specification {

    def "non version based download does work"() {
        when:
        def connection = download("application__V0.1.1.jar")

        then:
        connection.responseCode == HttpURLConnection.HTTP_OK

        and:
        connection.contentType == "application/x-java-archive"

        and:
        connection.contentEncoding == 'pack200-gzip'
    }

    def "non version based download returns 'NOT-CHANGED'"() {
        setup:
        def lastModified = download("application__V0.1.1.jar").lastModified

        when:
        def connection = download("application__V0.1.1.jar")

        and:
        connection.setIfModifiedSince(lastModified)

        then:
        connection.responseCode == HttpURLConnection.HTTP_NOT_MODIFIED
    }

    def "version based download does work"() {
        when:
        def connection = download("application.jar?version-id=0.1.1")

        then:
        connection.responseCode == HttpURLConnection.HTTP_OK

        and:
        connection.contentType == "application/x-java-archive"

        and:
        connection.contentEncoding == 'pack200-gzip'
    }

    def "version based diff-download does work"() {
        when:
        def connection = download("application.jar?version-id=0.1.1&current-version-id=0.1.0")

        then:
        connection.responseCode == HttpURLConnection.HTTP_OK

        and:
        connection.contentType == "application/x-java-archive-diff"

        and:
        connection.contentEncoding == 'pack200-gzip'
    }

    def "version based download with non-existing current-version-id does work"() {
        when:
        def connection = download("application.jar?version-id=0.1.1&current-version-id=0.0.9")

        then:
        connection.responseCode == HttpURLConnection.HTTP_OK

        and:
        connection.contentType == "application/x-java-archive"

        and:
        connection.contentEncoding == 'pack200-gzip'
    }

    def "version based download for non-existing version-id does return 404"() {
        when:
        def connection = download("application.jar?version-id=0.1.2")

        then:
        connection.responseCode == HttpURLConnection.HTTP_NOT_FOUND
    }

    private HttpURLConnection download(String file) {
        "http://localhost:8080/jnlp-servlet/$file".toURL().openConnection()
    }
}
