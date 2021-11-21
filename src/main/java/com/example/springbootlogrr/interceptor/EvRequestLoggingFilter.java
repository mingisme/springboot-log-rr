package com.example.springbootlogrr.interceptor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

public class EvRequestLoggingFilter extends CommonsRequestLoggingFilter {

    private boolean includeResponseStatus;
    private boolean includeResponseBody;
    private boolean includeResponseHeaders;
    private String beforeMessagePrefix = DEFAULT_BEFORE_MESSAGE_PREFIX;
    private String beforeMessageSuffix = DEFAULT_BEFORE_MESSAGE_SUFFIX;
    private String afterMessagePrefix = DEFAULT_AFTER_MESSAGE_PREFIX;
    private String afterMessageSuffix = DEFAULT_AFTER_MESSAGE_SUFFIX;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {


        boolean isFirstRequest = !isAsyncDispatch(request);
        HttpServletRequest requestToUse = request;
        HttpServletResponse responseToUse = response;

        if (isIncludePayload() && isFirstRequest && !(request instanceof ContentCachingRequestWrapper)) {
            requestToUse = new ContentCachingRequestWrapper(request, getMaxPayloadLength());
            responseToUse = new ContentCachingResponseWrapper(response);
        }

        boolean shouldLog = shouldLog(requestToUse);
        if (shouldLog && isFirstRequest) {
            beforeRequest(requestToUse, getBeforeMessage(requestToUse));
        }
        try {
            filterChain.doFilter(requestToUse, responseToUse);
        } finally {
            if (shouldLog && !isAsyncStarted(requestToUse)) {
                afterRequest(requestToUse, getAfterMessage(requestToUse, responseToUse));
            }
        }
    }

    private String getBeforeMessage(HttpServletRequest request) {
        return createMessage(request, null, this.beforeMessagePrefix, this.beforeMessageSuffix);
    }

    private String getAfterMessage(HttpServletRequest request, HttpServletResponse response) {
        return createMessage(request, response, this.afterMessagePrefix, this.afterMessageSuffix);
    }

    protected String createMessage(HttpServletRequest request, HttpServletResponse response, String prefix, String suffix) {
        StringBuilder msg = new StringBuilder();
        msg.append(prefix);
        msg.append(request.getMethod()).append(' ');
        msg.append(request.getRequestURL());

        if (isIncludeQueryString()) {
            String queryString = request.getQueryString();
            if (queryString != null) {
                msg.append('?').append(queryString);
            }
        }

        if (isIncludeClientInfo()) {
            String client = request.getRemoteAddr();
            if (StringUtils.hasLength(client)) {
                msg.append(", client=").append(client);
            }
            HttpSession session = request.getSession(false);
            if (session != null) {
                msg.append(", session=").append(session.getId());
            }
            String user = request.getRemoteUser();
            if (user != null) {
                msg.append(", user=").append(user);
            }
        }

        if (isIncludeHeaders()) {
            HttpHeaders headers = new ServletServerHttpRequest(request).getHeaders();
            if (getHeaderPredicate() != null) {
                Enumeration<String> names = request.getHeaderNames();
                while (names.hasMoreElements()) {
                    String header = names.nextElement();
                    if (!getHeaderPredicate().test(header)) {
                        headers.set(header, "masked");
                    }
                }
            }
            msg.append(", headers=").append(headers);
        }

        if (isIncludePayload()) {
            String payload = getMessagePayload(request);
            if (payload != null) {
                msg.append(", payload=").append(payload);
            }
        }

        if (response != null) {
            if (isIncludeResponseStatus()) {
                int status = response.getStatus();
                msg.append(", response status=").append(status);
            }

            if (isIncludeResponseHeaders()) {
                HttpHeaders headers = new ServletServerHttpResponse(response).getHeaders();
                msg.append(", response headers=").append(headers);
            }

            if (isIncludeResponseBody()) {
                String payload = getMessagePayload(response);
                if (payload != null) {
                    msg.append(",response body=").append(payload);
                }
            }
        }

        msg.append(suffix);
        return msg.toString();
    }

    protected String getMessagePayload(HttpServletResponse response) {
        ContentCachingResponseWrapper wrapper =
                WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                int length = Math.min(buf.length, getMaxPayloadLength());
                try {
                    return new String(buf, 0, length, wrapper.getCharacterEncoding());
                } catch (UnsupportedEncodingException ex) {
                    return "[unknown]";
                }
            }
        }
        return null;
    }


    public void setBeforeMessagePrefix(String beforeMessagePrefix) {
        this.beforeMessagePrefix = beforeMessagePrefix;
    }

    public void setBeforeMessageSuffix(String beforeMessageSuffix) {
        this.beforeMessageSuffix = beforeMessageSuffix;
    }

    public void setAfterMessagePrefix(String afterMessagePrefix) {
        this.afterMessagePrefix = afterMessagePrefix;
    }

    public void setAfterMessageSuffix(String afterMessageSuffix) {
        this.afterMessageSuffix = afterMessageSuffix;
    }

    public void setIncludeResponseStatus(boolean includeResponseStatus) {
        this.includeResponseStatus = includeResponseStatus;
    }

    public void setIncludeResponseBody(boolean includeResponseBody) {
        this.includeResponseBody = includeResponseBody;
    }

    public void setIncludeResponseHeaders(boolean includeResponseHeaders) {
        this.includeResponseHeaders = includeResponseHeaders;
    }

    public boolean isIncludeResponseStatus() {
        return includeResponseStatus;
    }

    public boolean isIncludeResponseBody() {
        return includeResponseBody;
    }

    public boolean isIncludeResponseHeaders() {
        return includeResponseHeaders;
    }
}
