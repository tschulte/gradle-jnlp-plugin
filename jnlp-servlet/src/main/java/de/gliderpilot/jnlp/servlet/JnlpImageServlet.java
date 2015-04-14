package de.gliderpilot.jnlp.servlet;

import javax.servlet.annotation.WebServlet;

@WebServlet(name = "jnlp-image-servlet", urlPatterns = {"*.png", "*.gif", "*.jpg"})
public class JnlpImageServlet extends AbstractJnlpServlet {

}
