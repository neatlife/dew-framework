package com.tairanchina.csp.dew.example.bone;

import com.tairanchina.csp.dew.core.DewBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * 工程启动类
 */
public class BoneExampleApplication extends DewBootApplication {


    public static void main(String[] args) {
        new SpringApplicationBuilder(BoneExampleApplication.class).run(args);
    }

}
