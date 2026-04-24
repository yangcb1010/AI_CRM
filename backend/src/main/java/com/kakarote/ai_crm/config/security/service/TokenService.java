package com.kakarote.ai_crm.config.security.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import com.alibaba.fastjson.JSON;
import com.kakarote.ai_crm.common.Const;
import com.kakarote.ai_crm.entity.BO.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Handles JWT and Redis-backed login sessions.
 */
@Component
@Slf4j
public class TokenService {
    private static final DateTimeFormatter KICKOUT_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final long MILLIS_SECOND = 1000;
    private static final long MILLIS_MINUTE = 60 * MILLIS_SECOND;
    private static final long MILLIS_MINUTE_TEN = 1400 * 60 * 1000L;

    @Value("${token.header}")
    private String header;

    @Value("${token.secret}")
    private String secret;

    @Value("${token.expireTime}")
    private int expireTime;

    @Value("${token.multi-login-enabled:false}")
    private boolean multiLoginEnabled;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public LoginUser getLoginUser(HttpServletRequest request) {
        String token = getToken(request);
        if (StrUtil.isEmpty(token)) {
            return null;
        }

        try {
            Claims claims = parseToken(token);
            String uuid = (String) claims.get(Const.LOGIN_USER_KEY);
            String loginUserJson = redisTemplate.opsForValue().get(getTokenKey(uuid));
            if (StrUtil.isBlank(loginUserJson)) {
                attachKickoutMessage(request, uuid);
                return null;
            }

            LoginUser loginUser = JSON.parseObject(loginUserJson, LoginUser.class);
            if (loginUser == null) {
                attachKickoutMessage(request, uuid);
                return null;
            }

            if (isTokenReplaced(loginUser, uuid)) {
                attachKickoutMessage(request, uuid);
                return null;
            }
            return loginUser;
        } catch (Exception e) {
            log.debug("Failed to resolve login user from token: {}", e.getMessage());
            return null;
        }
    }

    public void setLoginUser(LoginUser loginUser) {
        if (ObjectUtil.isNotNull(loginUser) && StrUtil.isNotEmpty(loginUser.getToken())) {
            refreshToken(loginUser);
        }
    }

    public void delLoginUser(String token) {
        if (StrUtil.isEmpty(token)) {
            return;
        }

        String tokenKey = getTokenKey(token);
        String loginUserJson = redisTemplate.opsForValue().get(tokenKey);
        redisTemplate.delete(tokenKey);
        redisTemplate.delete(getKickoutKey(token));

        if (StrUtil.isBlank(loginUserJson)) {
            return;
        }

        LoginUser loginUser = JSON.parseObject(loginUserJson, LoginUser.class);
        clearUserTokenMapping(loginUser, token);
    }

    public String createToken(LoginUser loginUser) {
        return createToken(loginUser, null);
    }

    public String createToken(LoginUser loginUser, String loginIp) {
        String token = IdUtil.fastUUID();
        loginUser.setToken(token);
        handleSingleLogin(loginUser, token, loginIp);
        refreshToken(loginUser);

        Map<String, Object> claims = new HashMap<>();
        claims.put(Const.LOGIN_USER_KEY, token);
        claims.put("username", loginUser.getUsername());
        return createToken(claims);
    }

    public void verifyToken(LoginUser loginUser) {
        long expireAt = loginUser.getExpireTime();
        long currentTime = System.currentTimeMillis();
        if (expireAt - currentTime <= MILLIS_MINUTE_TEN) {
            refreshToken(loginUser);
        }
    }

    public void refreshToken(LoginUser loginUser) {
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setExpireTime(loginUser.getLoginTime() + expireTime * MILLIS_MINUTE);
        redisTemplate.opsForValue().set(
                getTokenKey(loginUser.getToken()),
                JSON.toJSONString(loginUser),
                expireTime,
                TimeUnit.MINUTES
        );

        if (!multiLoginEnabled && loginUser.getUser() != null && loginUser.getUser().getUserId() != null) {
            redisTemplate.opsForValue().set(
                    getUserTokenKey(loginUser.getUser().getUserId()),
                    loginUser.getToken(),
                    expireTime,
                    TimeUnit.MINUTES
            );
        }
    }

    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    public String resolveLoginIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return StrUtil.nullToEmpty(JakartaServletUtil.getClientIP(request)).trim();
    }

    private String createToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    private String getToken(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String token = request.getHeader(header);
        if (StrUtil.isNotEmpty(token) && token.startsWith(Const.TOKEN_PREFIX)) {
            token = token.replace(Const.TOKEN_PREFIX, "");
        }
        return token;
    }

    private String getTokenKey(String uuid) {
        return Const.LOGIN_TOKEN_KEY + uuid;
    }

    private String getUserTokenKey(Long userId) {
        return Const.LOGIN_USER_TOKEN_KEY + userId;
    }

    private String getKickoutKey(String token) {
        return Const.LOGIN_KICKOUT_KEY + token;
    }

    private boolean isTokenReplaced(LoginUser loginUser, String token) {
        if (multiLoginEnabled || loginUser == null || loginUser.getUser() == null || loginUser.getUser().getUserId() == null) {
            return false;
        }

        String latestToken = redisTemplate.opsForValue().get(getUserTokenKey(loginUser.getUser().getUserId()));
        return StrUtil.isNotBlank(latestToken) && !StrUtil.equals(latestToken, token);
    }

    private void handleSingleLogin(LoginUser loginUser, String newToken, String loginIp) {
        if (multiLoginEnabled || loginUser == null || loginUser.getUser() == null || loginUser.getUser().getUserId() == null) {
            return;
        }

        String oldToken = redisTemplate.opsForValue().get(getUserTokenKey(loginUser.getUser().getUserId()));
        if (StrUtil.isBlank(oldToken) || StrUtil.equals(oldToken, newToken)) {
            return;
        }

        redisTemplate.opsForValue().set(
                getKickoutKey(oldToken),
                buildKickoutMessage(loginIp),
                expireTime,
                TimeUnit.MINUTES
        );
        redisTemplate.delete(getTokenKey(oldToken));
    }

    private void clearUserTokenMapping(LoginUser loginUser, String token) {
        if (loginUser == null || loginUser.getUser() == null || loginUser.getUser().getUserId() == null) {
            return;
        }

        String userTokenKey = getUserTokenKey(loginUser.getUser().getUserId());
        String currentToken = redisTemplate.opsForValue().get(userTokenKey);
        if (StrUtil.equals(token, currentToken)) {
            redisTemplate.delete(userTokenKey);
        }
    }

    private void attachKickoutMessage(HttpServletRequest request, String token) {
        if (request == null || StrUtil.isBlank(token)) {
            return;
        }

        String kickoutMessage = redisTemplate.opsForValue().get(getKickoutKey(token));
        if (StrUtil.isNotBlank(kickoutMessage)) {
            request.setAttribute(Const.LOGIN_KICKOUT_MESSAGE_ATTR, kickoutMessage);
        }
    }

    private String buildKickoutMessage(String loginIp) {
        String time = LocalTime.now().format(KICKOUT_TIME_FORMATTER);
        if (StrUtil.isBlank(loginIp)) {
            return "\u5f53\u524d\u7528\u6237\u4e8e" + time + "\u5728\u5176\u4ed6IP\u767b\u5f55\uff0c\u5f53\u524d\u767b\u5f55\u5df2\u88ab\u9000\u51fa";
        }
        return "\u5f53\u524d\u7528\u6237\u4e8e" + time + "\u5728\u5176\u4ed6IP(" + loginIp
                + ")\u767b\u5f55\uff0c\u5f53\u524d\u767b\u5f55\u5df2\u88ab\u9000\u51fa";
    }
}
