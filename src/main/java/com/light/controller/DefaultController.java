package com.light.controller;

// TODO: 2019-03-27  增加启动默认页面

@Path("/")
@Controller
public class DefaultController {
	public void index() {
		return 'index';
	}
}
