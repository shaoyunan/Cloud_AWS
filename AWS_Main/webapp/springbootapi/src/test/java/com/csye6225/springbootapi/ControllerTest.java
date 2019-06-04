package com.csye6225.springbootapi;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import com.csye6225.springbootapi.controller.UserController;
//import com.csye6225.springbootapi.pojo.User;
import com.csye6225.springbootapi.repository.UserRepository;
//import com.google.gson.Gson;

@RunWith(MockitoJUnitRunner.class)
public class ControllerTest {

	private MockMvc mvc;

	@Mock
	private UserRepository ur;

	@InjectMocks
	private UserController uc;

	@Before
	public void setup() {
		mvc = MockMvcBuilders.standaloneSetup(uc).build();
	}

	@Test
	public void unAuthTest() throws Exception {

		MockHttpServletResponse response = mvc.perform(get("/").accept(MediaType.APPLICATION_JSON)).andReturn()
				.getResponse();

		assertEquals(response.getStatus(), HttpStatus.UNAUTHORIZED.value());

	}
	
	/*
	@Test
	public void pwdValidationTest() throws Exception {
		User u = new User();
		u.setEmail("a@a.com");
		u.setPassword("123");
		Gson gson = new Gson();
		MockHttpServletResponse response = mvc.perform(post("/user/register").contentType(MediaType.APPLICATION_JSON).content(gson.toJson(u))).andReturn().getResponse();
		
		//String msg = response.getContentAsString();
		//System.out.println(msg);
		
		//assertEquals(response.getStatus(), HttpStatus.UNPROCESSABLE_ENTITY.value());
		assertEquals(response.getContentAsString().contains("Weak password"), true);
	}
	
	@Test
	public void emailValidationTest() throws Exception {
		User u = new User();
		u.setEmail("username");
		u.setPassword("Abc.123456");
		Gson gson = new Gson();
		MockHttpServletResponse response = mvc.perform(post("/user/register").contentType(MediaType.APPLICATION_JSON).content(gson.toJson(u))).andReturn().getResponse();
		
		//String msg = response.getContentAsString();
		//System.out.println(msg);
		//assertEquals(response.getStatus(), HttpStatus.UNPROCESSABLE_ENTITY.value());
		assertEquals(response.getContentAsString().contains("User name should be email"), true);
	}
	*/
}
