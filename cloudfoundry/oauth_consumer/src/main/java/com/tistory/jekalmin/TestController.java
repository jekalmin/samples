package com.tistory.jekalmin;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

	@RequestMapping("/")
	public String index(){
		return "Hello " + SecurityContextHolder.getContext().getAuthentication().getName();
	}
	
}
