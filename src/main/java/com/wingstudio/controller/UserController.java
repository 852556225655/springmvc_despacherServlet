package com.wingstudio.controller;

import com.wingstudio.annotation.Controller;
import com.wingstudio.annotation.RequestMapping;
import com.wingstudio.annotation.ResponseBody;
import com.wingstudio.entity.UserEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author ITcz
 * @Data 2021-04-19 - 18:05
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @RequestMapping("/getUser.do")
    @ResponseBody
    public Object getUser(HttpServletRequest request, HttpServletResponse responce, UserEntity userEntity) {
        System.out.println("request: " + request + "\n" + "response: " + responce + "\n" + "User: " + userEntity);
        return userEntity;
    }

    @RequestMapping("/index.do")
    public String index(){
        return "/index.html";
    }



}
