package com.csye6225.springbootapi.controller;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.csye6225.springbootapi.dao.UserDAO;
import com.csye6225.springbootapi.pojo.User;
import com.timgroup.statsd.NonBlockingStatsDClient;

@RestController
public class UserController {
	
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
	private NonBlockingStatsDClient client = new NonBlockingStatsDClient("csye6225.webapp", "localhost", 8125);
	
	@Autowired
	private UserDAO dao;

	// first endpoint (/)
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ResponseEntity<Map<String, String>> auth(
			@RequestHeader(value = "Authorization", defaultValue = "NOTAUTH") String auth) {
		Map<String, String> map = new HashMap<String, String>();
		
		//client.incrementCounter("/ | GET");
		
		if (!auth.equals("NOTAUTH")) {
            
			client.incrementCounter("/ | GET");
			
			String base64Credentials = auth.substring("Basic".length()).trim();
			byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
			String credentials = new String(credDecoded, StandardCharsets.UTF_8);
			String[] values = credentials.split(":", 2);
            //Get the password and email
			String email = values[0];
			String pwd = values[1];

			// find existence of email and judge validity of password
			if (dao.isExist(email)) {
				// User stored = dao.findOneByEmail(email);
				// String salt = stored.getSalt();
				//Check two stuffs
				//1 User exists or not
				//2 Password correct or not
				if (!dao.checkAuth(email, pwd)) {
					map.put("Message", "Incorrect Credentials");
					//Return unauthorized code
					LOGGER.warn("End Point(GET): /. Incorrect Credential Attempt. Username: "+email);
					return new ResponseEntity<Map<String, String>>(map, HttpStatus.UNAUTHORIZED);
				}
				//If the information is authorized, return the time as message
				map.put("Message", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(new Date()));
				return ResponseEntity.ok().body(map);
			}
			//If the user doesn't exist, return the authorized code
			map.put("Message", "Can't find the account");
			return new ResponseEntity<Map<String, String>>(map, HttpStatus.UNAUTHORIZED);

		}
		
		//LOGGER.warn("End Pint(GET): /. No Auth Access Attempt");
		map.put("Message", "Not logged in/No auth info provided");
		return new ResponseEntity<Map<String, String>>(map, HttpStatus.UNAUTHORIZED);
	}

	// register endpoint (/user/register)
	@RequestMapping(value = "/user/register", method = RequestMethod.POST)
	public ResponseEntity<Map<String, String>> createUser(@Valid @RequestBody User user) {
		
		client.incrementCounter("/user/register | POST");
		
		String email = user.getEmail();
		String password = user.getPassword();
		Map<String, String> map = new HashMap<String, String>();
        //If the user already exists, show the tip
		//Return the 409 code
		if (dao.isExist(email)) {
			map.put("Message", "Account exists");
			return new ResponseEntity<Map<String, String>>(map, HttpStatus.CONFLICT);
		}
        //Check whether if the username matches a email
		String pattern = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(email);
		if (!m.matches()) {
            //HttpStatus.UNPROCESSABLE_ENTITY: 422
			map.put("Message", "User name should be email");
			return new ResponseEntity<Map<String, String>>(map, HttpStatus.UNPROCESSABLE_ENTITY);
		}
        //Validate the password
		boolean checkpassword = password.matches("^.*[a-z]+.*$") && password.matches("^.*[0-9]+.*$")
				&& password.matches("^.*[A-Z]+.*$")
				&& password.matches("^.*[/^/$/.//,;:'!@#%&/*/|/?/+/(/)/[/]/{/}]+.*$") && password.matches("^.{9,}$");
		if (!checkpassword) {
            //If the password is weak, return corrsponding message
			map.put("Message", "Weak password");
			return new ResponseEntity<Map<String, String>>(map, HttpStatus.UNPROCESSABLE_ENTITY);
		}
        //Save the user
		dao.save(user);
		map.put("Result", "Account created");
		return ResponseEntity.ok().body(map);
	}

	// Code below this line is used for testing
	@GetMapping("/users")
	public List<User> getAllUsers() {
		return dao.findAll();
	}

}
