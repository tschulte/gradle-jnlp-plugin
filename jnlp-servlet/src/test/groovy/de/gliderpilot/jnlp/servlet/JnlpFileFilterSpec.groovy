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
class JnlpFileFilterSpec extends AbstractJnlpServletSpec {

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
}
