package com.csye6225.springbootapi.pojo;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "attachments")
@EntityListeners(AuditingEntityListener.class)
//Enable EntityListener to persist and update the entity
public class Attachment {
	//Generate a UUID
	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	private String id;
	//Relate the attachment to a specific note
	@ManyToOne
	@JoinColumn(name = "note_id", nullable = false)
	@JsonIgnore
	private Note note;
	//url
	private String url;
    //Get the attachment's id
	public String getId() {
		return id;
	}
    //Set the attachment's id
	public void setId(String id) {
		this.id = id;
	}
    //Get the related note
	public Note getNote() {
		return note;
	}
    //Set the relater note
	public void setNote(Note note) {
		this.note = note;
	}
    //Get the url
	public String getUrl() {
		return url;
	}
    //Set the url
	public void setUrl(String url) {
		this.url = url;
	}
	
}

