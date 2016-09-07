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

class VersionBasedDownloadSpec extends AbstractJnlpServletSpec {

    def "download does work for #file in version #version"() {
        when:
        def connection = download("${file}?version-id=${version}")

        then:
        connection.responseCode == HttpURLConnection.HTTP_OK

        and:
        connection.contentType == contentType

        and:
        connection.contentEncoding == contentEncoding

        where:
        file               | version | contentType                    | contentEncoding
        "application.jar"  | "0.1.1" | "application/x-java-archive"   | "pack200-gzip"
        "jnlp/launch.jnlp" | "0.1.1" | "application/x-java-jnlp-file" | null
        "griffon.png"      | "1.0.0" | "image/png"                    | null
    }

    def "diff-download does work"() {
        when:
        def connection = download("application.jar?version-id=0.1.1&current-version-id=0.1.0")

        then:
        connection.responseCode == HttpURLConnection.HTTP_OK

        and:
        connection.contentType == "application/x-java-archive-diff"

        and:
        connection.contentEncoding == 'pack200-gzip'
    }

    def "download with non-existing current-version-id does work"() {
        when:
        def connection = download("application.jar?version-id=0.1.1&current-version-id=0.0.9")

        then:
        connection.responseCode == HttpURLConnection.HTTP_OK

        and:
        connection.contentType == "application/x-java-archive"

        and:
        connection.contentEncoding == 'pack200-gzip'
    }

    def "download for non-existing version-id does return 404"() {
        when:
        def connection = download("application.jar?version-id=0.1.2")

        then:
        connection.responseCode == HttpURLConnection.HTTP_NOT_FOUND
    }

}
