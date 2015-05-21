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

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tobias on 5/14/15.
 */
@WebFilter(filterName = "jnlp-file-filter", urlPatterns = {"*.jnlp"})
public class JnlpFileFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        JnlpFileResponseWrapper responseWrapper = new JnlpFileResponseWrapper((HttpServletRequest)request, (HttpServletResponse)response);
        chain.doFilter(request, responseWrapper);
        responseWrapper.getOutputStream().writeLine();
    }

    @Override
    public void destroy() {

    }

    private static class JnlpFileResponseWrapper extends HttpServletResponseWrapper {

        private JnlpFileOutputStream outputStream;

        public JnlpFileResponseWrapper(HttpServletRequest request, HttpServletResponse response) throws IOException {
            super(response);
            outputStream = new JnlpFileOutputStream(request, (HttpServletResponse) getResponse());
        }

        @Override
        public JnlpFileOutputStream getOutputStream() throws IOException {
            return outputStream;
        }

    }

    private static class JnlpFileOutputStream extends ServletOutputStream {

        private static final Pattern XML_DECLARATION_PATTERN = Pattern.compile("<\\?xml.*encoding=[\"'](.*?)[\"'].*\\?>", Pattern.CASE_INSENSITIVE);

        private HttpServletRequest request;
        private ServletOutputStream delegate;
        private ByteArrayOutputStream lineStream = new ByteArrayOutputStream(1024);
        private String encoding = "UTF-8";

        public JnlpFileOutputStream(HttpServletRequest request, HttpServletResponse response) throws IOException {
            this.request = request;
            delegate = response.getOutputStream();
        }

        @Override
        public boolean isReady() {
            return delegate.isReady();
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            delegate.setWriteListener(writeListener);
        }

        @Override
        public void write(int b) throws IOException {
            if (b == '\n' || b == '\r') {
                writeLine();
            } else {
                lineStream.write(b);
            }
        }

        @Override
        public void flush() throws IOException {
            delegate.flush();
        }

        @Override
        public void close() throws IOException {
            writeLine();
            flush();
            delegate.close();
        }

        public void writeLine() throws IOException {
            String line = lineStream.toString(encoding);
            if ("".equals(line))
                return;
            checkEncoding(line);
            String site = request.getRequestURL().toString().replace(request.getRequestURI(), "");
            String requestURI = request.getRequestURI();
            int idx = requestURI.lastIndexOf('/');
            String name = requestURI.substring(idx + 1);
            String codebase = requestURI.substring(0, idx + 1);
            line = line.replace("$$codebase", site + codebase);
            line = line.replace("$$name", name);
            line = line.replace("$$context", site + request.getContextPath());
            line = line.replace("$$site", site);
            line = line.replace("$$hostname", request.getServerName());
            delegate.println(line);
            lineStream.reset();
        }

        private void checkEncoding(String line) {
            Matcher encodingMatcher = XML_DECLARATION_PATTERN.matcher(line);
            if (encodingMatcher.matches()) {
                encoding = encodingMatcher.group(1);
            }
        }
    }
}
