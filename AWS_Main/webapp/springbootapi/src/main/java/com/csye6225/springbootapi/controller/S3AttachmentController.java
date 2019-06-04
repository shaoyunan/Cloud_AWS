package com.csye6225.springbootapi.controller;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.csye6225.springbootapi.dao.AttachmentDAO;
import com.csye6225.springbootapi.dao.NoteDAO;
import com.csye6225.springbootapi.dao.UserDAO;
import com.csye6225.springbootapi.pojo.Attachment;
import com.csye6225.springbootapi.pojo.Note;
import com.csye6225.springbootapi.pojo.User;
import com.csye6225.springbootapi.storageservice.s3.AmazonClient;
import com.timgroup.statsd.NonBlockingStatsDClient;

@RestController
@ConditionalOnExpression("'${controller.type}'=='cloud'")
public class S3AttachmentController {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	private NonBlockingStatsDClient client = new NonBlockingStatsDClient("csye6225.webapp", "localhost", 8125);

	@Autowired
	private AmazonClient amazonClient;
	@Autowired
	private UserDAO uDao;
	@Autowired
	private NoteDAO nDao;
	@Autowired
	private AttachmentDAO aDao;

	// Get the note's attachment
	@RequestMapping(value = "/note/{noteId}/attachments", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Object> getAttachments(@PathVariable(value = "noteId") String noteId,
			@RequestHeader(value = "Authorization", defaultValue = "NOTAUTH") String auth) {
		
		client.incrementCounter("/note/noteid/attachments | GET");
		
		if (!auth.equals("NOTAUTH")) {
			String base64Credentials = auth.substring("Basic".length()).trim();
			byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
			String credentials = new String(credDecoded, StandardCharsets.UTF_8);
			String[] values = credentials.split(":", 2);
			String username = values[0];
			String password = values[1];
			// check if authorized
			if (!uDao.checkAuth(username, password)) {
				LOGGER.warn("End Point(GET): /note/noteid/attachments. Incorrect Credential Attempt. Username: " + username);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"Message\":\"Unauthorized\"}");
			}

			User u = uDao.findOneByEmail(username);
			for (Note n : u.getNotes()) {
				if (n.getId().equals(noteId)) {
					return ResponseEntity.status(HttpStatus.OK).body(n.getAttachments());
				}
			}
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"Message\":\"Unauthorized\"}");
		}
		LOGGER.warn("End Pint(GET): /note/noteid/attachments. No Auth Access Attempt");
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"Message\":\"Unauthorized\"}");
	}

	// Post an attachment to a note
	@RequestMapping(value = "/note/{noteId}/attachments", method = RequestMethod.POST, produces = "application/json")
	public ResponseEntity<Object> addAttachment(@PathVariable(value = "noteId") String noteId,
			@RequestParam("file") MultipartFile file,
			@RequestHeader(value = "Authorization", defaultValue = "NOTAUTH") String auth) {
		
		client.incrementCounter("/note/noteid/attachments | POST");
		
		if (!auth.equals("NOTAUTH")) {
			// Decode the username and password
			String base64Credentials = auth.substring("Basic".length()).trim();
			byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
			String credentials = new String(credDecoded, StandardCharsets.UTF_8);
			String[] values = credentials.split(":", 2);
			String username = values[0];
			String password = values[1];
			// check if authorized;

			if (!uDao.checkAuth(username, password)) {
				LOGGER.warn("End Point(POST): /note/noteid/attachments. Incorrect Credential Attempt. Username: " + username);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"Message\":\"Unauthorized\"}");
			}

			User u = uDao.findOneByEmail(username);
			for (Note n : u.getNotes()) {
				if (n.getId().equals(noteId)) {
					try {
						Attachment a = new Attachment();
						n.getAttachments().add(a);
						a.setNote(n);
						a = aDao.save(a);
						String path = amazonClient.uploadFile(a.getId(), getExtension(file), file);
						a.setUrl(path);
						a = aDao.save(a);
						return ResponseEntity.status(HttpStatus.OK).body(a);
					} catch (Exception e) {

					}
				}
			}
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"Message\":\"Unauthorized\"}");
		}
		LOGGER.warn("End Pint(POST): /note/noteid/attachments. No Auth Access Attempt");
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"Message\":\"Unauthorized\"}");
	}

	// Delete an attachment by given id
	@RequestMapping(value = "/note/{noteId}/attachments/{attachmentId}", method = RequestMethod.DELETE, produces = "application/json")
	public ResponseEntity<Object> removeAttachment(@PathVariable(value = "noteId") String noteId,
			@PathVariable(value = "attachmentId") String attachmentId,
			@RequestHeader(value = "Authorization", defaultValue = "NOTAUTH") String auth) {
		
		client.incrementCounter("/note/noteid/attachments/attachid | DELETE");
		
		if (!auth.equals("NOTAUTH")) {
			// Decode the username and password
			String base64Credentials = auth.substring("Basic".length()).trim();
			byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
			String credentials = new String(credDecoded, StandardCharsets.UTF_8);
			String[] values = credentials.split(":", 2);
			String username = values[0];
			String password = values[1];
			// check if authorized;

			if (!uDao.checkAuth(username, password)) {
				LOGGER.warn("End Point(DELETE): /note/noteid/attachments/attchid. Incorrect Credential Attempt. Username: " + username);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"Message\":\"Unauthorized\"}");
			}

			User u = uDao.findOneByEmail(username);
			for (Note n : u.getNotes()) {
				if (n.getId().equals(noteId)) {
					Iterator<Attachment> it = n.getAttachments().iterator();
					while (it.hasNext()) {
						Attachment a = it.next();
						if (a.getId().equals(attachmentId)) {
							it.remove();
							amazonClient.deleteFileFromS3Bucket(a.getUrl());
							nDao.save(n);
							return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No Content");
						}
					}
				}
			}
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"Message\":\"Unauthorized\"}");
		}
		LOGGER.warn("End Pint(DELETE): /note/noteid/attachments/attachid. No Auth Access Attempt");
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"Message\":\"Unauthorized\"}");
	}

	// Update the attachment by given id
	@RequestMapping(value = "/note/{noteId}/attachments/{attachmentId}", method = RequestMethod.PUT, produces = "application/json")
	public ResponseEntity<Object> updateAttachment(@PathVariable(value = "noteId") String noteId,
			@PathVariable(value = "attachmentId") String attachmentId, @RequestParam("file") MultipartFile file,
			@RequestHeader(value = "Authorization", defaultValue = "NOTAUTH") String auth) {
		
		client.incrementCounter("/note/noteid/attachments/attachid | PUT");
		
		if (!auth.equals("NOTAUTH")) {
			// Decode the username and password
			String base64Credentials = auth.substring("Basic".length()).trim();
			byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
			String credentials = new String(credDecoded, StandardCharsets.UTF_8);
			String[] values = credentials.split(":", 2);
			String username = values[0];
			String password = values[1];
			// check if authorized;

			if (!uDao.checkAuth(username, password)) {
				LOGGER.warn("End Point(PUT): /note/noteid/attachments/attachid. Incorrect Credential Attempt. Username: " + username);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"Message\":\"Unauthorized\"}");
			}

			User u = uDao.findOneByEmail(username);
			for (Note n : u.getNotes()) {
				if (n.getId().equals(noteId)) {
					for (Attachment a : n.getAttachments()) {
						if (a.getId().equals(attachmentId)) {
							String url = amazonClient.updateFile(a.getUrl(), a.getId(), getExtension(file), file);
							a.setUrl(url);
							aDao.save(a);
							return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No Content");
						}
					}
				}
			}
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"Message\":\"Unauthorized\"}");
		}
		LOGGER.warn("End Pint(PUT): /note/noteid/attachments/attachid. No Auth Access Attempt");
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"Message\":\"Unauthorized\"}");
	}

	// getOriginalFilename returns the original filename in the client's filesystem.
	// This may contain path information depending on the browser used.
	// So we can substring it to an expected and normal file name
	private String getExtension(MultipartFile file) {
		String name = file.getOriginalFilename();
		int lastIndex = name.lastIndexOf(".");
		if (lastIndex == -1) {
			return "";
		}
		return name.substring(lastIndex);
	}
}
