package com.mark59.servermetricsweb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Basic Spring Security configuration.  Forcing login into the Web Application, 
 * allowing open access to the api service.
 * 
 * <p>References:<br>
 * https://www.baeldung.com/spring-boot-security-autoconfiguration<br>
 * https://www.baeldung.com/spring-security-login
 * 
 * @author Philip Webb
 * Written: Australian Autumn 2020  
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	 private Environment env;	
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
		
		String user = env.getProperty("user");
		String pass = env.getProperty("pass");
		
		System.out.println("user=" + user + ",pass=" + pass );
		
		auth.inMemoryAuthentication().withUser(user).password(encoder.encode(pass)).roles("USER");
	}
    

	@Override
	public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
        .antMatchers("/login").permitAll()
        .antMatchers("/api/**").permitAll()
        .antMatchers("/**").hasRole("USER")
        .and().formLogin().loginPage("/login").defaultSuccessUrl("/serverProfileList", true)
        .and().logout().logoutSuccessUrl("/login").permitAll()
        .and().csrf().disable();		
	}
	
   @Override
    public void configure(WebSecurity web) throws Exception {
        web
         .ignoring()
         .antMatchers("/h2-console/**");
    }

}
