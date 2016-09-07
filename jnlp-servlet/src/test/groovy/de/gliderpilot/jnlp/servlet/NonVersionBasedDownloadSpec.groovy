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

class NonVersionBasedDownloadSpec extends AbstractJnlpServletSpec {

    def "download does work for #file"() {
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

    def "download supports If-Modified-Since Header"() {
        setup:
        def connection = download("application__V0.1.1.jar")
        def lastModified = connection.lastModified

        when:
        connection = download("application__V0.1.1.jar")
        connection.setIfModifiedSince(lastModified)

        then:
        connection.responseCode == HttpURLConnection.HTTP_NOT_MODIFIED

        when:
        connection = download("application__V0.1.1.jar")
        connection.setIfModifiedSince(lastModified + 1000)

        then:
        connection.responseCode == HttpURLConnection.HTTP_NOT_MODIFIED

        when:
        connection = download("application__V0.1.1.jar")
        connection.setIfModifiedSince(lastModified - 1000)

        then:
        connection.responseCode == HttpURLConnection.HTTP_OK
    }
}
