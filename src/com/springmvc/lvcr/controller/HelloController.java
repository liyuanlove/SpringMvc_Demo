package com.springmvc.lvcr.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.springmvc.lvcr.annotion.Autowired;
import com.springmvc.lvcr.annotion.Controller;
import com.springmvc.lvcr.annotion.RequestMapping;
import com.springmvc.lvcr.service.impl.IUserService;
import com.springmvc.lvcr.service.impl.UserServiceImpl;

@Controller("/hello")
public class HelloController {
	
	@Autowired("userServiceImpl")
	private IUserService userServiceImpl;
	
	
	@RequestMapping(value="/insert")	
	public void insert(HttpServletRequest request,HttpServletResponse response,String parm){
		userServiceImpl.insert();
	}
	
}
