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
class JnlpServletSpec extends Specification {

    def "non version based download does work for #file"() {
        when:
        def connection = download(file)

        then:
        connection.responseCode == HttpURLConnection.HTTP_OK

        and:
        connection.contentType == contentType

        and:
        connection.contentEncoding == contentEncoding

        where:
        file                       | contentType                    | contentEncoding
        "application__V0.1.1.jar"  | "application/x-java-archive"   | 'pack200-gzip'
        "jnlp/launch__V0.1.1.jnlp" | "application/x-java-jnlp-file" | null
        "griffon__V1.0.0.png"      | "image/png"                    | null
    }

    def "non version based download supports If-Modified-Since Header"() {
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

    def "special values are substituted in jnlp files"() {
        when:
        def connection = download("jnlp/launch__V0.1.1.jnlp")

        then:
        connection.responseCode == HttpURLConnection.HTTP_OK

        when:
        def jnlp = new XmlSlurper().parse(connection.inputStream)

        then:
        jnlp.@href == 'launch__V0.1.1.jnlp'
        jnlp.@codebase == 'http://localhost:8080/jnlp-servlet/jnlp/'
        jnlp.resources.property.find { it.@name == 'jnlp.context' }.@value == 'http://localhost:8080/jnlp-servlet'
        jnlp.resources.property.find { it.@name == 'jnlp.site' }.@value == 'http://localhost:8080'
        jnlp.resources.property.find { it.@name == 'jnlp.hostname' }.@value == 'localhost'
    }

    private HttpURLConnection download(String file) {
        "http://localhost:8080/jnlp-servlet/$file".toURL().openConnection()
    }
}
