package com.sha5.ticketpigeon.common.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Simple filter that rejects any request that does not contain the expected
 * X-Gateway-Secret header. The secret is configured via the "gateway.secret"
 * property in each service's application.yml. This provides a lightweight
 * protection against direct calls bypassing the API‑Gateway.
 */
public class GatewaySecretFilter implements Filter {
    private final String expectedSecret;

    public GatewaySecretFilter(String expectedSecret) {
        this.expectedSecret = expectedSecret;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No init required
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }
        HttpServletRequest httpReq = (HttpServletRequest) request;
        String header = httpReq.getHeader("X-Gateway-Secret");
        if (expectedSecret == null || !expectedSecret.equals(header)) {
            HttpServletResponse httpResp = (HttpServletResponse) response;
            httpResp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResp.getWriter().write("Forbidden: missing or invalid gateway secret");
            return;
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // No resources to clean up
    }
}
