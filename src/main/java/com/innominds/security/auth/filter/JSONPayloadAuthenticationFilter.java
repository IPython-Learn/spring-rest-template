package com.innominds.security.auth.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.SecureRandom;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.HazelcastInstance;
import com.innominds.persistence.vo.LoginRequest;
import com.innominds.persistence.vo.LoginResponse;
import com.innominds.persistence.vo.User;

public class JSONPayloadAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    /** Reference to logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(JSONPayloadAuthenticationFilter.class);

    private HazelcastInstance hazelcastInstance;

    private ObjectMapper jacksonObjectMapper;

    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    public void setJacksonObjectMapper(ObjectMapper jacksonObjectMapper) {
        this.jacksonObjectMapper = jacksonObjectMapper;
    }

    public JSONPayloadAuthenticationFilter() {
        super(new AntPathRequestMatcher("/api/login", "POST")); // if the URI and HTTP method matches the it is considers as login request
    }

    static boolean isContentTypeValid(final HttpServletRequest request) {
        return request.getContentType() != null && request.getContentType().contains(MediaType.APPLICATION_JSON_VALUE);

    }

    LoginRequest getLoginRequest(final HttpServletRequest request) throws BadCredentialsException {

        try {
            final StringBuffer payload = new StringBuffer();
            String line = null;
            while ((line = request.getReader().readLine()) != null) {
                payload.append(line);
            }
            return jacksonObjectMapper.readValue(payload.toString(), LoginRequest.class);
        } catch (final Exception e) {
            LOGGER.error("Invalid attributes in the payload. Required username and password", e);
            throw new BadCredentialsException("Invalid attributes in the payload. Required username and password");
        }
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException,
            ServletException {

        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            LOGGER.debug("Invalid HTTP Method. it accepts only POST ");
            throw new InsufficientAuthenticationException("Invalid HTTP Method. it accepts only POST ");
        }

        if (!isContentTypeValid(request)) {
            throw new InsufficientAuthenticationException("Invalid content type. It accepts JSON only.");
        }

        final LoginRequest loginRequest = getLoginRequest(request);

        final UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());

        // Allow subclasses to set the "details" property
        // setDetails(request, authRequest);

        return getAuthenticationManager().authenticate(authRequest);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult)
            throws IOException, ServletException {
        final SecurityContextImpl sCtx = new SecurityContextImpl();
        sCtx.setAuthentication(authResult);

        // SecurityContextHolder.getContext().setAuthentication(authResult);
        final String sessionToken = sessionToken();

        hazelcastInstance.getMap("userTokenMap").put(sessionToken, sCtx);

        try (PrintWriter out = response.getWriter()) {

            final LoginResponse loginResponse = new LoginResponse();
            loginResponse.setAccessToken(sessionToken);

            if (authResult.getPrincipal() instanceof User) {
                final User user = (User) authResult.getPrincipal();
                loginResponse.setName(user.getUsername());
                loginResponse.setName(user.getUsername());
            }
            out.write(jacksonObjectMapper.writeValueAsString(loginResponse));
        }

    }

    static String sessionToken() {
        final String token = new BigInteger(130, new SecureRandom()).toString(32);
        return new String(Base64.encode(token.getBytes()));
    }

    @Override
    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        return "/api/login".equalsIgnoreCase(request.getRequestURI());
    }

}
