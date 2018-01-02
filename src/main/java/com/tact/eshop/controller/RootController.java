package com.tact.eshop.controller;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/")
public class RootController {
	//at login launches the home page
	@RequestMapping({"", "index", "home"})
	public String index() {
		return "root/home";
	}

}
