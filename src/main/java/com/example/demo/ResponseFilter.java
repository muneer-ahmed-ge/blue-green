package com.example.demo;

import java.io.IOException;
import java.util.Random;
import javax.annotation.PostConstruct;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * @author Muneer Ahmed
 * @version 1.0
 * @since 2019-03-13
 */
@Slf4j
@Order(1)
@Component
/**
 *
 * Add a Host header with value as the Application Version configured by the property gateway.version
 *
 * It is expected for Client App to send this header in every request
 *
 * This is useful to route the requests during Blue/Green deployment
 *
 */
public class ResponseFilter extends OncePerRequestFilter {

    private static final String HOST = "Host";
    private static final String SYNC_ID = "SYNC_ID";

    @Value("${bg.version}")
    private String applicationVersion;

    @PostConstruct
    private void init() {
        applicationVersion += ".com";
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        addSyncId(httpServletRequest);
        httpServletResponse.addHeader(HOST, applicationVersion);
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    private void addSyncId(HttpServletRequest httpServletRequest) {

        HttpSession session = httpServletRequest.getSession(true);
        String syncId = (String) session.getAttribute(SYNC_ID);
        if (null == syncId) {
            syncId = String.valueOf(new Random().nextInt(900) + 100);
            session.setAttribute(SYNC_ID, syncId);
        }
        MDC.put(SYNC_ID, syncId);
    }
}
