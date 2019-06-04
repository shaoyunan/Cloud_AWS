package com.csye6225.springbootapi.pojo;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "notes")
@EntityListeners(AuditingEntityListener.class)
//Enable EntityListener to persist and update the entity
public class Note {
    //Generate a UUID
	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	private String id;
    //Make the user_id as the foreign key
	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	@JsonIgnore
	private User user;
    //The content of a note
	private String content;
    //The title of a note
	private String title;
    //Hide attributes from the Jackson parser by instructing it to ignore these fields
	//The time created the note
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private String created_on;
    //Hide attributes from the Jackson parser by instructing it to ignore these fields.
	//The latest time updated the note
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private String last_updated_on;
	
	@SuppressWarnings("deprecation")
	@OneToMany(mappedBy = "note", fetch = FetchType.LAZY)
	@Cascade({ CascadeType.ALL, CascadeType.DELETE_ORPHAN })
	private List<Attachment> attachments;
	
	public Note() {
		this.attachments = new ArrayList<Attachment>();
	}
	
    //Get the note's id
	public String getId() {
		return id;
	}
    //Set the note's id
	public void setId(String id) {
		this.id = id;
	}
   //Get the note's content
	public String getContent() {
		return content;
	}
    //Set the note's content
	public void setContent(String content) {
		this.content = content;
	}
	//Get the note's title
	public String getTitle() {
		return title;
	}
	//Get the note's title
	public void setTitle(String title) {
		this.title = title;
	}
	//Get the note's created time
	public String getCreated_on() {
		return created_on;
	}
	//Set the note's created time
	public void setCreated_on(String created_on) {
		this.created_on = created_on;
	}
	//Get the note's updated time
	public String getLast_updated_on() {
		return last_updated_on;
	}
	//Set the note's updated time
	public void setLast_updated_on(String last_updated_on) {
		this.last_updated_on = last_updated_on;
	}
	//Get the note's user
	public User getUser() {
		return user;
	}
	//Set the note's user
	public void setUser(User user) {
		this.user = user;
	}
	public List<Attachment> getAttachments() {
		return attachments;
	}
	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}

}
