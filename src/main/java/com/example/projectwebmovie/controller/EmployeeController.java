package com.example.projectwebmovie.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/employee")
public class EmployeeController {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    @GetMapping("/ticket-management")
    public String showTickets(Model model) {
        return "admin/ticket-management";
    }

    @GetMapping("/booking")
    public String showBookingPage(Model model) {
        logger.info("Accessing booking page");
        return "employee/employee-booking";
    }
}
