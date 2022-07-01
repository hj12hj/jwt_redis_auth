package com.hj.core;

/**
 * generate token and auth etc...
 */
public interface AuthOperation {
    /**
     * info use json to handle
     * return token id
     */
    String generateToken(Object obj);

    /**
     * check jwt token
     */
    Boolean authToken(String tokenId);

    /**
     * get user info
     */
    Object getInfo(String tokenId, Class<?> obj);

    /**
     * add times
     */
    void extendTokenTime(String tokenId);

    void setKey(String key);

    void setExpire(Long expire);

    void setExtendTime(Long extendTime);
}
