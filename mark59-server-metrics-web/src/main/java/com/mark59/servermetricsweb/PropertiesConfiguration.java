package com.mark59.servermetricsweb;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * reference: 
 * https://www.theserverside.com/video/How-applicationproperties-simplifies-Spring-config 
 * 
 * @author Philip Webb
 * Written: Australian Autumn 2020    
 */
@ConfigurationProperties(prefix="web.logon")
public class PropertiesConfiguration {
	
	private String userid;
	private String pwd;
	
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	
}
