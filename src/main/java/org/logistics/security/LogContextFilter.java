package org.logistics.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import jakarta.servlet.*;
import org.slf4j.MDC;

@Component
public class LogContextFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            MDC.put("userEmail", auth.getName());
            MDC.put("userRole", auth.getAuthorities().toString());
        }

        HttpServletRequest req = (HttpServletRequest) request;
        MDC.put("endpoint", req.getRequestURI());
        MDC.put("method", req.getMethod());

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}