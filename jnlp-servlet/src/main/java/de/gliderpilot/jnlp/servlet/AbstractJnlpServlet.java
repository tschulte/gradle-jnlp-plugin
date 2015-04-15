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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class AbstractJnlpServlet extends HttpServlet {

    @Override
    protected long getLastModified(HttpServletRequest req) {
        JnlpRequestHandler requestHandler = getRequestHandler(req);
        return requestHandler.getLastModified();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JnlpRequestHandler requestHandler = getRequestHandler(req);
        requestHandler.doGet(resp);
    }

    private JnlpRequestHandler getRequestHandler(HttpServletRequest req) {
        JnlpRequestHandler requestHandler = (JnlpRequestHandler) req.getAttribute("jnlp-request-handler");
        if (requestHandler == null) {
            requestHandler = createRequestHandler(req);
            req.setAttribute("jnlp-request-handler", requestHandler);
        }
        return requestHandler;
    }

    protected JnlpRequestHandler createRequestHandler(HttpServletRequest req) {
        return new JnlpRequestHandler(req);
    }

}
