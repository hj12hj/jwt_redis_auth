package com.hj.test;

import com.hj.dto.User;
import com.hj.jwt.JwtAuthOperation;

public class App {
    public static void main(String[] args) {

        JwtAuthOperation jwtAuthOperation = new JwtAuthOperation();
//        User user = new User();
//        user.setName("11");
//        String token = jwtAuthOperation.generateToken(user);

//        System.out.println(jwtAuthOperation.authToken(token));
        User info = (User) jwtAuthOperation.getInfo("8c97c63c-b46b-4e03-8aad-e4bb44b4dbf9", User.class);
    }
}
