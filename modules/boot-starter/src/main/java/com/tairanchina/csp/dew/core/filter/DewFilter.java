package com.tairanchina.csp.dew.core.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * desription:
 * Created by ding on 2017/11/13.
 */

public class DewFilter implements Filter {


    private final Logger logger = LoggerFactory.getLogger(DewFilter.class);

    // url->(timestamp,resTime)
    public static final Map<String, LinkedHashMap<Long, Integer>> RECORD_MAP = new WeakHashMap<>();

    private static final String MATCHING_PATTERN_KEY = "org.springframework.web.servlet.HandlerMapping.bestMatchingPattern";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("dewFilter Started");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        long start = Instant.now().toEpochMilli();
        servletRequest.setAttribute("dew.metric.start",start);
        filterChain.doFilter(servletRequest, servletResponse);
        int resTime = (int) (Instant.now().toEpochMilli() - start);
        String method = httpServletRequest.getMethod();
        String matchingPattern = (String) httpServletRequest.getAttribute(MATCHING_PATTERN_KEY);
        if (matchingPattern != null && !matchingPattern.endsWith("/favicon.ico")) {
            String key = "{[" + method + "]:" + matchingPattern + "}";
            if (RECORD_MAP.containsKey(key)) {
                RECORD_MAP.get(key).put(start, resTime);
            } else {
                RECORD_MAP.put(key, new LinkedHashMap<Long, Integer>() {{
                    put(start, resTime);
                }});
            }
        }
    }

    @Override
    public void destroy() {
        logger.info("dewFilter destroyed");
    }
}
