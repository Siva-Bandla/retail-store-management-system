package com.retailstore.logging;


import jakarta.servlet.*;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.UUID;

public class RequestIdFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        try{
            MDC.put("requestId", UUID.randomUUID().toString());
            filterChain.doFilter(servletRequest, servletResponse);
        }finally {
            MDC.clear();
        }

    }
}
