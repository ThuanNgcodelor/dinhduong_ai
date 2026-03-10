package com.david.NUTRITION_TRACNKER.security;

import com.david.NUTRITION_TRACNKER.service.CustomUserDetailsService;
import com.david.NUTRITION_TRACNKER.util.CookieUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = getJwtFromCookies(request, "accessToken");

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                authenticateUser(request, jwt);
            } else {
                // Access token expired or missing, try refresh token
                String refreshToken = getJwtFromCookies(request, "refreshToken");
                if (StringUtils.hasText(refreshToken) && tokenProvider.validateToken(refreshToken)) {
                    // Valid refresh token, generate new tokens
                    String username = tokenProvider.getUsernameFromToken(refreshToken);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    
                    String newAccessToken = tokenProvider.generateAccessToken(authentication);
                    String newRefreshToken = tokenProvider.generateRefreshToken(authentication);
                    
                    CookieUtils.addCookie(response, "accessToken", newAccessToken, 3600);
                    CookieUtils.addCookie(response, "refreshToken", newRefreshToken, 2592000);
                    
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateUser(HttpServletRequest request, String jwt) {
        String username = tokenProvider.getUsernameFromToken(jwt);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String getJwtFromCookies(HttpServletRequest request, String name) {
        Optional<Cookie> cookie = CookieUtils.getCookie(request, name);
        return cookie.map(Cookie::getValue).orElse(null);
    }
}
