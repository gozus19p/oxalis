package no.difi.oxalis.inbound.ws;

import javax.annotation.Resource;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Manuel Gozzi
 *
 */
@Slf4j
public class InboundClientWS {
	
	@Resource
	HttpServlet as2Servlet;

	@RequestMapping(value = "v1.0/inbound/receive")
	public void receiveInbound(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		//	forward request to AS2Servlet
	}
	
}
