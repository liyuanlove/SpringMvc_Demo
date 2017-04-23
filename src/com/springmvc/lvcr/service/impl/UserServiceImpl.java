package com.springmvc.lvcr.service.impl;

import com.springmvc.lvcr.annotion.Service;

@Service("userServiceImpl")
public class UserServiceImpl implements IUserService {

	public void insert() {
		System.out.println(" userServiceImpl insert() .....");

	}

}
