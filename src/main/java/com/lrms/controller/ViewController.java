package com.lrms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ViewController {

    @GetMapping("/login")
    public String login() {
        System.out.println("DEBUG: Reached /login endpoint");
        return "login";
    }

    @GetMapping("/test")
    @ResponseBody
    public String test() {
        return "Controller is working!";
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/rooms")
    public String rooms() {
        return "rooms";
    }

    @GetMapping("/bookings")
    public String bookings() {
        return "bookings";
    }

    @GetMapping("/guests")
    public String guests() {
        return "guests";
    }

    @GetMapping("/restaurant")
    public String restaurant() {
        return "restaurant";
    }

    @GetMapping("/billing")
    public String billing() {
        return "billing";
    }

    @GetMapping("/housekeeping")
    public String housekeeping() {
        return "housekeeping";
    }

    @GetMapping("/staff")
    public String staff() {
        return "staff";
    }
}
