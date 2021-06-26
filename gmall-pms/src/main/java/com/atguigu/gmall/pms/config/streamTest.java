package com.atguigu.gmall.pms.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Date:2021/6/25
 * Author:away
 * Description:
 */
public class streamTest {
    public static void main(String[] args) {
        // Arrays.asList() 该方法是将对象型的数组转化成List集合的方法。
        List<User> users = Arrays.asList(
                new User("liuyan", 20, false),
                new User("marong", 21, false),
                new User("xiaolu", 22, false),
                new User("laowang", 23, true),
                new User("xiaoliang", 24, true),
                new User("zhengshuang", 25, false),
                new User("pig", 26, true)
        );
        //map:把一个集合转化为另一个集合
        users.stream().map(user -> user.getUsername()).collect(Collectors.toList()).forEach(System.out::println);
        users.stream().map(user -> {
            Person person = new Person();//把user对象转为person对象到新的集合
            person.setName(user.getUsername());
            person.setAge(user.getAge());
            return person;
        }).collect(Collectors.toList()).forEach(System.out::println);

        System.out.println("=============================================");

        //filter：过滤出需要的数据，组装为新的集合
        users.stream().filter(user -> {
            return user.getAge() > 22;
        }).collect(Collectors.toList()).forEach(System.out::println);
        //也可以简写把大括号去掉
        users.stream().filter(
                user -> user.getSex() == false
        ).collect(Collectors.toList()).forEach(System.out::println);
        //判断所有Age为true的
        users.stream().filter(User::getSex).collect(Collectors.toList()).forEach(System.out::println);
        System.out.println("==============================================");
        //reduce：求和
        List<Integer> asList = Arrays.asList(21, 22, 23, 24, 25);
        System.out.println(asList.stream().reduce((a, b) -> a + b).get());
        System.out.println(users.stream().map(User::getAge).reduce((a, b) -> a + b).get());
    }
}
@Data
@AllArgsConstructor
@NoArgsConstructor
class User {
    private String username;
    private Integer age;
    private Boolean sex;
}

@Data
class Person {
    private String name;
    private Integer age;
}