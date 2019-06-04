package com.csye6225.springbootapi.controller;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.csye6225.springbootapi.dao.NoteDAO;
import com.csye6225.springbootapi.dao.UserDAO;
import com.csye6225.springbootapi.pojo.Attachment;
import com.csye6225.springbootapi.pojo.Note;
import com.csye6225.springbootapi.pojo.User;
import com.csye6225.springbootapi.storageservice.s3.AmazonClient;
import com.timgroup.statsd.NonBlockingStatsDClient;

@ConditionalOnExpression("'${controller.type}'=='cloud'")
@RestController
public class CloudNoteController {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	private NonBlockingStatsDClient client = new NonBlockingStatsDClient("csye6225.webapp", "localhost", 8125);

	@Autowired
	private AmazonClient amazonClient;

	// The bean will be injected automatically
	@Autowired
	private UserDAO uDao;
	// The bean will be injected automatically
	@Autowired
	private NoteDAO nDao;

	// When using get in the postman, returns all the notes related to the specific
	// authorized user
	@RequestMapping(value = "/note", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Object> getAllNotes(
			@RequestHeader(value = "Authorization", defaultValue = "NOTAUTH") String auth) {
		
		client.incrementCounter("/note | GET");
		
		if (!auth.equals("NOTAUTH")) {
			// Decode the username and password
			String base64Credentials = auth.substring("Basic".length()).trim();
			byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
			String credentials = new String(credDecoded, StandardCharsets.UTF_8);
			String[] values = credentials.split(":", 2);
			String username = values[0];
			String password = values[1];
			// If unauthorized, return unauthorized status
			if (!uDao.checkAuth(username, password)) {
				LOGGER.warn("End Point(GET): /note. Incorrect Credential Attempt. Username: "+username);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"Message\":\"Unauthorized\"}");
			}
			// get the user by username
			User u = uDao.findOneByEmail(username);
			// Get the notes
			List<Note> result = u.getNotes();
			// Show the notes in the view
			return ResponseEntity.status(HttpStatus.OK).body(result);
		}
		LOGGER.warn("End Pint(GET): /note. No Auth Access Attempt");
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"Message\":\"Unauthorized\"}");
	}


	// When posting the notes in the postman, check if the user is authorized
	// @valid will check the notes automatically
	@RequestMapping(value = "/note", method = RequestMethod.POST, produces = "application/json")
	public ResponseEntity<Object> createNote(@Valid @RequestBody Note note,
			@RequestHeader(value = "Authorization", defaultValue = "NOTAUTH") String auth) {

		client.incrementCounter("/note | POST");
		
		if (!auth.equals("NOTAUTH")) {
			// Decode the username and password
			String base64Credentials = auth.substring("Basic".length()).trim();
			byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
			String credentials = new String(credDecoded, StandardCharsets.UTF_8);
			String[] values = credentials.split(":", 2);
			String username = values[0];
			String password = values[1];
			// If unauthorized, return unauthorized status
			if (!uDao.checkAuth(username, password)) {
				LOGGER.warn("End Point(POST): /note. Incorrect Credential Attempt. Username: "+username);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"Message\":\"Unauthorized\"}");
			}
			// When title and content is null, it will return to bad request
			if (note.getTitle() == null || note.getContent() == null) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"Message\":\"Bad Request\"}");
			}
			// Relates the note to a user
			User u = uDao.findOneByEmail(username);
			note.setUser(u);
			u.getNotes().add(note);
			return ResponseEntity.status(HttpStatus.CREATED).body(nDao.save(note));
		}
		LOGGER.warn("End Pint(POST): /note. No Auth Access Attempt");
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"Message\":\"Unauthorized\"}");
	}


	// Get the specific note by id
	@RequestMapping(value = "/note/{id}", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Object> getNote(@PathVariable(value = "id") String id,
			@RequestHeader(value = "Authorization", defaultValue = "NOTAUTH") String auth) {

		client.incrementCounter("/note/id | GET");
		
		if (!auth.equals("NOTAUTH")) {
			String base64Credentials = auth.substring("Basic".length()).trim();
			byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
			String credentials = new String(credDecoded, StandardCharsets.UTF_8);
			String[] values = credentials.split(":", 2);
			// Decode the username and password
			String username = values[0];
			String password = values[1];
			// If unauthorized, return unauthorized status
			if (!uDao.checkAuth(username, password)) {
				LOGGER.warn("End Point(GET): /note/id. Incorrect Credential Attempt. Username: "+username);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"Message\":\"Unauthorized\"}");
			}
			// Find the user and search for note using id, then return OK status
			User u = uDao.findOneByEmail(username);
			for (Note n : u.getNotes()) {
				if (n.getId().equals(id)) {
					return ResponseEntity.status(HttpStatus.OK).body(n);
				}
			}
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"Message\":\"Not Found\"}");
		}
		LOGGER.warn("End Pint(GET): /note/id. No Auth Access Attempt");
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"Message\":\"Unauthorized\"}");
	}


	// Update the specific note related to id when doing get operation in the
	// postman
	@RequestMapping(value = "/note/{id}", method = RequestMethod.PUT, produces = "application/json")
	public ResponseEntity<Object> updateNote(@PathVariable(value = "id") String id, @Valid @RequestBody Note note,
			@RequestHeader(value = "Authorization", defaultValue = "NOTAUTH") String auth) {
		
		client.incrementCounter("/note/id | PUT");
		
		if (!auth.equals("NOTAUTH")) {
			String base64Credentials = auth.substring("Basic".length()).trim();
			byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
			String credentials = new String(credDecoded, StandardCharsets.UTF_8);
			String[] values = credentials.split(":", 2);
			// Decode the username and password
			String username = values[0];
			String password = values[1];
			// If unauthorized, return unauthorized status
			if (!uDao.checkAuth(username, password)) {
				LOGGER.warn("End Point(PUT): /note/id. Incorrect Credential Attempt. Username: "+username);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"Message\":\"Unauthorized\"}");
			}
			// When title and content is null, it will return to bad request
			if (note.getTitle() == null || note.getContent() == null) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"Message\":\"Bad Request\"}");
			}
			// Get the user and then get the note
			// Update the note in DB matched model
			User u = uDao.findOneByEmail(username);
			for (Note n : u.getNotes()) {
				if (n.getId().equals(id)) {
					n.setTitle(note.getTitle());
					n.setContent(note.getContent());
					nDao.save(n);
					return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No Content");
				}
			}
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"Message\":\"Unauthorized\"}");
		}
		LOGGER.warn("End Pint(PUT): /note/id. No Auth Access Attempt");
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"Message\":\"Unauthorized\"}");
	}


	// Delete a specific note using id
	@RequestMapping(value = "/note/{id}", method = RequestMethod.DELETE, produces = "application/json")
	public ResponseEntity<Object> deleteNote(@PathVariable(value = "id") String id,
			@RequestHeader(value = "Authorization", defaultValue = "NOTAUTH") String auth) {
		
		client.incrementCounter("/note/id | DELETE");
		
		if (!auth.equals("NOTAUTH")) {
			String base64Credentials = auth.substring("Basic".length()).trim();
			byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
			String credentials = new String(credDecoded, StandardCharsets.UTF_8);
			String[] values = credentials.split(":", 2);
			// Decode the username and password
			String username = values[0];
			String password = values[1];
			// If unauthorized, return unauthorized status
			if (!uDao.checkAuth(username, password)) {
				LOGGER.warn("End Point(DELETE): /note/id. Incorrect Credential Attempt. Username: "+username);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"Message\":\"Unauthorized\"}");
			}
			// Get the note and delete it
			// Jpa repository do it for us in the DB
			User u = uDao.findOneByEmail(username);
			Iterator<Note> it = u.getNotes().iterator();
			while (it.hasNext()) {
				Note n = it.next();
				if (n.getId().equals(id)) {
					// deleteLocalAttachments(n.getAttachments());
					deleteS3Attachments(n.getAttachments());
					it.remove();
					uDao.update(u);
					return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No Content");
				}
			}
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"Message\":\"Bad Request\"}");
		}
		LOGGER.warn("End Pint(DELETE): /note/id. No Auth Access Attempt");
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"Message\":\"Unauthorized\"}");
	}

	private void deleteS3Attachments(List<Attachment> attachments) {
		amazonClient.clear(attachments);

	}

}
