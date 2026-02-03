package com.mapleraid.core.utility;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;

import java.util.Arrays;
import java.util.Optional;

public class CookieUtil {

    public static Optional<String> refineCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(name))
                .findFirst()
                .map(Cookie::getValue);
    }

    public static void addCookie(HttpServletResponse response, String cookieDomain, String name, String value) {
        boolean isSecure = !cookieDomain.equals("localhost");
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .domain(cookieDomain)
                .path("/")
                .httpOnly(true)
                .secure(isSecure)
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public static void addSecureCookie(HttpServletResponse response, String cookieDomain, String name, String value, Integer maxAge) {
        boolean isSecure = !cookieDomain.equals("localhost");
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .domain(cookieDomain)
                .path("/")
                .httpOnly(true)
                .secure(isSecure)
                .maxAge(maxAge)
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String cookieDomain, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return;
        }
        boolean isSecure = !cookieDomain.equals("localhost");
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                ResponseCookie removedCookie = ResponseCookie.from(name, "")
                        .domain(cookieDomain)
                        .path("/")
                        .maxAge(0)
                        .httpOnly(true)
                        .secure(isSecure)
                        .sameSite("Lax")
                        .build();
                response.addHeader("Set-Cookie", removedCookie.toString());
            }
        }
    }

    public static void deleteCookieByResponse(HttpServletResponse response, String cookieDomain, String name) {
        boolean isSecure = !cookieDomain.equals("localhost");
        ResponseCookie removedCookie = ResponseCookie.from(name, "")
                .domain(cookieDomain)
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .secure(isSecure)
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", removedCookie.toString());
    }
}
