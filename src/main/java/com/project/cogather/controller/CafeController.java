package com.project.cogather.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/studycafe")
public class CafeController {
	
	@RequestMapping("/main")
	public String studymain(Model model) {
		return "cafe/main";
	}

}