package com.mapleraid.core.utility;

import com.mapleraid.core.constant.Constants;
import com.mapleraid.core.exception.definition.ErrorCode;
import com.mapleraid.core.exception.type.CommonException;
import com.mapleraid.security.application.dto.DefaultJsonWebTokenDto;
import com.mapleraid.security.application.dto.OauthJsonWebTokenDto;
import com.mapleraid.security.type.ESecurityRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
public class JsonWebTokenUtil implements InitializingBean {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.temporary-token-validity-ms:1800000}")
    private Long temporaryTokenExpirePeriod;

    @Value("${jwt.access-token-validity-ms}")
    private Long accessTokenExpirePeriod;

    @Getter
    @Value("${jwt.refresh-token-validity-ms}")
    private Long refreshTokenExpirePeriod;

    private SecretKey key;

    @Override
    public void afterPropertiesSet() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public DefaultJsonWebTokenDto generateDefaultJsonWebTokens(UUID id, ESecurityRole role) {
        return new DefaultJsonWebTokenDto(
                generateToken(id.toString(), role, accessTokenExpirePeriod),
                generateToken(id.toString(), null, refreshTokenExpirePeriod)
        );
    }

    public OauthJsonWebTokenDto generateOauthJsonWebTokens(String id) {
        return new OauthJsonWebTokenDto(
                generateToken(id, null, temporaryTokenExpirePeriod),
                null,
                null
        );
    }

    public OauthJsonWebTokenDto generateOauthJsonWebTokens(UUID id, ESecurityRole role) {
        return new OauthJsonWebTokenDto(
                null,
                generateToken(id.toString(), role, accessTokenExpirePeriod),
                generateToken(id.toString(), null, refreshTokenExpirePeriod)
        );
    }

    /**
     * Access token만 생성 (토큰 자동 갱신용)
     */
    public String generateAccessToken(UUID id, ESecurityRole role) {
        return generateToken(id.toString(), role, accessTokenExpirePeriod);
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (MalformedJwtException e) {
            throw new CommonException(ErrorCode.TOKEN_MALFORMED_ERROR);
        } catch (IllegalArgumentException e) {
            throw new CommonException(ErrorCode.TOKEN_TYPE_ERROR);
        } catch (ExpiredJwtException e) {
            throw new CommonException(ErrorCode.EXPIRED_TOKEN_ERROR);
        } catch (UnsupportedJwtException e) {
            throw new CommonException(ErrorCode.TOKEN_UNSUPPORTED_ERROR);
        } catch (JwtException e) {
            throw new CommonException(ErrorCode.TOKEN_UNKNOWN_ERROR);
        }
    }

    private String generateToken(String identifier, ESecurityRole role, Long expirePeriod) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirePeriod);

        JwtBuilder builder = Jwts.builder()
                .claim(Constants.ACCOUNT_ID_CLAIM_NAME, identifier)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key);

        if (role != null) {
            builder.claim(Constants.ACCOUNT_ROLE_CLAIM_NAME, role.name());
        }

        return builder.compact();
    }
}
