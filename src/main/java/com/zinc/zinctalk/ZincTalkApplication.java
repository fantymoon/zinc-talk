package com.zinc.zinctalk;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.zinc.zinctalk.mapper")
public class ZincTalkApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZincTalkApplication.class, args);
    }

}
