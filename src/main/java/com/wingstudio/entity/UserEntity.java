package com.wingstudio.entity;

/**
 * @Author ITcz
 * @Data 2021-04-19 - 18:09
 */

public class UserEntity {

    String name;

    String age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "name='" + name + '\'' +
                ", age='" + age + '\'' +
                '}';
    }
}
