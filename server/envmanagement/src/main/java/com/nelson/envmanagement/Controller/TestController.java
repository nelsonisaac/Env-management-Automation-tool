package com.nelson.envmanagement.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/testing")
public class TestController {
    @GetMapping()
    public String tester(){
        System.out.println("this is testing");
        return "Testing endpoint";
    }
}
