package com.ChineseChess;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.*;

public class ServletTest extends HttpServlet {

	public ServletTest() {
		
	}
	
	public void init(ServletConfig config)
			throws ServletException {
		super.init(config);
	}
	
	public void service(HttpServletRequest request, HttpServletResponse response) 
			throws IOException, ServletException {
		String name = request.getParameter("type");
		System.out.println(name);
	}
}
