package com.hj.jwt;

import com.alibaba.fastjson.JSON;
import com.hj.core.AuthOperation;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * jwt impl
 */
public class JwtAuthOperation implements AuthOperation {

    Logger logger = LoggerFactory.getLogger(JwtAuthOperation.class);

    private String key = "APP_SECRET";

    private Long expire = 30L;

    private Long extendTime = 1 * 60L;

    RedisTemplate<String, Object> redisTemplate;

    public JwtAuthOperation(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public JwtAuthOperation() {
    }


    @Override
    public void setKey(String key) {
        this.key = key;
    }

    public void setExpire(Long expire) {
        this.expire = expire;
    }

    public void setExtendTime(Long extendTime) {
        this.extendTime = extendTime;
    }

    @Override
    public String generateToken(Object obj) {
        String json = "";
        if (!(obj instanceof String)) {
            json = JSON.toJSONString(obj);
        } else {
            json = (String) obj;
        }
        String JwtToken = Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS256")
                .setSubject("jwt")
                .setIssuedAt(new Date())
                .claim("info", json)
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
        //set into redis
        UUID uuid = UUID.randomUUID();
        String str = uuid.toString();
        logger.info("存入redis,key{} ，value{}", str, JwtToken);
        redisTemplate.opsForValue().set(str, JwtToken);
        redisTemplate.expire(str, expire, TimeUnit.MINUTES);
        return str;
    }

    @Override
    public Boolean authToken(String tokenId) {
        if (null == tokenId || "".equals(tokenId)) return false;
        String jwt = (String) redisTemplate.opsForValue().get(tokenId);
        if (jwt == null || jwt == "")
            return false;
        extendTokenTime(tokenId);
        try {
            Jwts.parser().setSigningKey(key).parseClaimsJws(jwt);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public Object getInfo(String tokenId, Class<?> objClass) {
        String jwt = (String) redisTemplate.opsForValue().get(tokenId);
        if (jwt == null || jwt == "")
            return null;
        authToken(tokenId);
        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(key).parseClaimsJws(jwt);
        Claims body = claimsJws.getBody();
        String info = (String) body.get("info");
        Object obj = null;
        if (objClass == String.class) {
            obj = JSON.parseObject(info, objClass);
        }else {
            obj = info;
        }
        return obj;
    }


    /**
     * redis add times
     */
    public void extendTokenTime(String tokenId) {
        Long expire = redisTemplate.getExpire(tokenId);
        if (expire == -1)
            return;
        // where redis expire is less than extendTime update it to the new value
        if (expire < extendTime) {
            redisTemplate.expire(tokenId, expire, TimeUnit.MINUTES);
        }
    }
}
