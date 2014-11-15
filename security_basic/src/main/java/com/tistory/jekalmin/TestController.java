package com.tistory.jekalmin;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

	@RequestMapping("/")
	public String index(){
		System.out.println(SecurityContextHolder.getContext());
		return "Hello World";
	}
	
	@RequestMapping("/secured")
	public String secured(){
		return "secured";
	}
	
}
