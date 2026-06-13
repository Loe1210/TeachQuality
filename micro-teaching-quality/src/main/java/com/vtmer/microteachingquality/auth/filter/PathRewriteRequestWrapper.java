package com.vtmer.microteachingquality.auth.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class PathRewriteRequestWrapper extends HttpServletRequestWrapper {
    private final String contextPath;
    private final String requestURI;
    private final String servletPath;
    private final String pathInfo;

    public PathRewriteRequestWrapper(HttpServletRequest request, String newURI) {
        super(request);
        this.contextPath = request.getContextPath();

        // 计算新的servletPath和pathInfo
        if (newURI.startsWith(contextPath)) {
            this.requestURI = newURI;
            this.servletPath = contextPath + newURI.substring(contextPath.length()).split("/")[1];
            this.pathInfo = newURI.substring(contextPath.length() + this.servletPath.length());
        } else {
            this.requestURI = contextPath + newURI;
            this.servletPath = newURI.split("/")[1];
            this.pathInfo = newURI.substring(this.servletPath.length() + 1);
        }
    }

    @Override
    public String getRequestURI() {
        return requestURI;
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

    @Override
    public String getServletPath() {
        return servletPath;
    }

    @Override
    public String getPathInfo() {
        return pathInfo != null && !pathInfo.isEmpty() ? pathInfo : null;
    }
}
