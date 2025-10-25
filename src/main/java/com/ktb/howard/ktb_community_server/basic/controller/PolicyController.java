package com.ktb.howard.ktb_community_server.basic.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/policy")
public class PolicyController {

    @GetMapping("/terms")
    public String getTermsOfService() {
        return "policy/termsOfService";
    }

}
