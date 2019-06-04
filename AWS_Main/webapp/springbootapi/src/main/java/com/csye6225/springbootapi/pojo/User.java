package com.csye6225.springbootapi.pojo;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
//Enable EntityListener to persist and update the entity
public class User {
	
    //Generate id automatically
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
    //Email cannot be blank
	@NotBlank
	private String email;
	
    //password cannot be blank
	@NotBlank
	private String password;
	
    //Generate a salt for use with the BCrypt.hashpw() method
	private String salt;
	
    //Make the notes related to a specific user
	@SuppressWarnings("deprecation")
	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
	//Persist the data in a more flexible way
	@Cascade({ CascadeType.ALL, CascadeType.DELETE_ORPHAN })
	private List<Note> notes;
	
    //Get user's id
	public Long getId() {
		return id;
	}
    //Set user's id
	public void setId(Long id) {
		this.id = id;
	}
    //Get user's email
	public String getEmail() {
		return email;
	}
    //Set user's email
	public void setEmail(String email) {
		this.email = email;
	}
    //Get password
	public String getPassword() {
		return password;
	}
    //Set password
	public void setPassword(String password) {
		this.password = password;
	}
    //Get the salt which is used to hash
	public String getSalt() {
		return salt;
	}
    //Set the salt 
	public void setSalt(String salt) {
		this.salt = salt;
	}
    //Get the notes
	public List<Note> getNotes() {
		return notes;
	}
    //Set the notes
	public void setNotes(List<Note> notes) {
		this.notes = notes;
	}

}
