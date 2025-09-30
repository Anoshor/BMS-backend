package com.bms.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/debug")
public class DebugController {

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @GetMapping("/mappings")
    public Map<String, String> getMappings() {
        return requestMappingHandlerMapping.getHandlerMethods()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().toString(),
                        entry -> entry.getValue().toString()
                ));
    }

    @GetMapping("/health")
    public String health() {
        return "Debug controller is working!";
    }
}