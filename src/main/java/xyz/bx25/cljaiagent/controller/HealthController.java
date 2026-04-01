package xyz.bx25.cljaiagent.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author bx25 小陈
 * @Date 2026/3/29 12:30
 */
@RestController
@RequestMapping("health")
public class HealthController {
    @GetMapping
    public String healthCheck(){
        return "ok";
    }
}
