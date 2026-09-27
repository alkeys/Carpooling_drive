package com.carpooling.sv.v1.shared.security;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class CookieService {

    @Value("${jwt.cookie-name:access_token}")
    private String cookieName;

    @Value("${jwt.cookie-secure:false}")
    private boolean cookieSecure;

    @Value("${jwt.cookie-same-site:Lax}")
    private String sameSite;

    @Value("${jwt.expiration-ms:86400000}")
    private long expirationMs;

    public ResponseCookie createJwtCookie(String token) {
        return ResponseCookie.from(cookieName, token)
            .httpOnly(true)
            .secure(cookieSecure)
            .path("/")
            .maxAge(Duration.ofMillis(expirationMs))
            .sameSite(sameSite)
            .build();
    }

    public ResponseCookie createCleanJwtCookie() {
        return ResponseCookie.from(cookieName, "")
            .httpOnly(true)
            .secure(cookieSecure)
            .path("/")
            .maxAge(0)
            .sameSite(sameSite)
            .build();
    }

    public String extractJwtFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public String getCookieName() {
        return cookieName;
    }
}
