package de.gliderpilot.jnlp.servlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

@WebServlet(name = "jnlp-jar-servlet", urlPatterns = {"*.jar"})
public class JnlpJarServlet extends AbstractJnlpServlet {

    @Override
    protected JnlpRequestHandler createRequestHandler(HttpServletRequest req) {
        return new JnlpJarRequestHandler(req);
    }

}
