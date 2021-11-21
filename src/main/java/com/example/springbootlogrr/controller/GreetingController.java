package com.example.springbootlogrr.controller;

import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class GreetingController {

    private final AtomicLong count = new AtomicLong();

    @GetMapping("greetings/{id}")
    public GreetingResponse getGreeting(@PathVariable("id") Long id) {
        return GreetingResponse.builder().id(id).message("Hello world!").build();
    }

    @PostMapping("greetings")
    public GreetingResponse createGreeting(@RequestBody GreetingRequest greetingRequest) {
        return GreetingResponse.builder().id(count.incrementAndGet()).message(greetingRequest.getMessage()).build();
    }

    @PostMapping("greetings2")
    public void createGreeting2(@RequestBody GreetingRequest greetingRequest) {

    }

    @SneakyThrows
    @PostMapping("greetings3")
    public void createGreeting3(@RequestBody GreetingRequest greetingRequest, HttpServletResponse response) {
        response.sendRedirect("https://www.baidu.com");
    }

    @SneakyThrows
    @GetMapping("greetings4")
    public void createGreeting4( HttpServletResponse response) {
        response.sendRedirect("https://www.baidu.com");
    }
}