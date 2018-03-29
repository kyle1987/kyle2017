package com.example.feign;

import org.springframework.stereotype.Component;

/**
 * Created by zhangjie on 2017/12/15.
 */
@Component
public class SchedualServiceHiHystric implements SchedualServiceHi {
    @Override
    public String sayHiFromClientOne(String name) {
        return "sorry "+name;
    }
}