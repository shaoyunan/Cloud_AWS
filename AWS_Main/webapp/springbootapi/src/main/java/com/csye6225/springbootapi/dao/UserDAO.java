package com.csye6225.springbootapi.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.csye6225.springbootapi.pojo.User;
import com.csye6225.springbootapi.repository.UserRepository;

@Service
public class UserDAO {
	//The bean will be injected automatically
	@Autowired
	private UserRepository ur;
	
	//save
	public User save(User user) {
		//Generate the salt and use it to hash the password we pass
		String salt = BCrypt.gensalt(10);
		String pw_hash = BCrypt.hashpw(user.getPassword(), salt);
		user.setSalt(salt);
		user.setPassword(pw_hash);
		//Use the save and flush method because we dont have transaction
		//It will flush the data instantly
		return ur.saveAndFlush(user);
	}
	
	//Query Criteria
	//See whether if the user exists by searching email
	public boolean isExist(String email) {
		
		User result = ur.findOneByEmail(email);
		if(result!=null){
			return true;
		}
		
		return false;
	}
	//See whether if the user exists by searching email
	public User findOneByEmail(String email) {
		return ur.findOneByEmail(email);
	}
	
	//When the email exists, check whether if the password corresponds to 
	//the password we hashed and stored in the database before
	public boolean checkAuth(String email, String password) {
		if(isExist(email)) {
			User stored = findOneByEmail(email);
			String salt = stored.getSalt();
			if (BCrypt.hashpw(password, salt).equals(stored.getPassword())) {
				return true;
			}
		}
		
		
		return false;
	}
	
	//Code for testing
	//search
	public List<User> findAll(){
		return ur.findAll();
	}
	
	
	//delete
	public void delete(User user) {
		ur.delete(user);
	}
    //Update the user
	public void update(User u) {
		
		ur.saveAndFlush(u);
		
	}
	
}
