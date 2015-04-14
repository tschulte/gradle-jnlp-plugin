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
