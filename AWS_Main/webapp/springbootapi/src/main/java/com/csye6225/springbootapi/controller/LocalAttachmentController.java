package com.csye6225.springbootapi.controller;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Iterator;

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
import com.csye6225.springbootapi.storageservice.local.StorageService;

@RestController
@ConditionalOnExpression("'${controller.type}'=='local'")
public class LocalAttachmentController {

	//The bean will be injected automatically
	@Autowired
	private StorageService storageService;
	//The bean will be injected automatically
	@Autowired
	private UserDAO uDao;
	//The bean will be injected automatically
	@Autowired
	private NoteDAO nDao;
	//The bean will be injected automatically
	@Autowired
	private AttachmentDAO aDao;

	//Get the note's attachment
	@RequestMapping(value = "/note/{noteId}/attachments", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Object> getAttachments(@PathVariable(value = "noteId") String noteId,
			@RequestHeader(value = "Authorization", defaultValue = "NOTAUTH") String auth) {
		if (!auth.equals("NOTAUTH")) {

			String base64Credentials = auth.substring("Basic".length()).trim();
			byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
			String credentials = new String(credDecoded, StandardCharsets.UTF_8);
			String[] values = credentials.split(":", 2);
			String username = values[0];
			String password = values[1];

			if (!uDao.checkAuth(username, password)) {
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
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"Message\":\"Unauthorized\"}");
	}

    //Post an attachment to a note
	@RequestMapping(value = "/note/{noteId}/attachments", method = RequestMethod.POST, produces = "application/json")
	public ResponseEntity<Object> addAttachment(@PathVariable(value = "noteId") String noteId,
			@RequestParam("file") MultipartFile file,
			@RequestHeader(value = "Authorization", defaultValue = "NOTAUTH") String auth) {
		if (!auth.equals("NOTAUTH")) {

			String base64Credentials = auth.substring("Basic".length()).trim();
			byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
			String credentials = new String(credDecoded, StandardCharsets.UTF_8);
			String[] values = credentials.split(":", 2);
			String username = values[0];
			String password = values[1];

			if (!uDao.checkAuth(username, password)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"Message\":\"Unauthorized\"}");
			}

			User u = uDao.findOneByEmail(username);
			for (Note n : u.getNotes()) {
				if (n.getId().equals(noteId)) {
					Attachment a = new Attachment();
					n.getAttachments().add(a);
					a.setNote(n);
                    a = aDao.save(a);
                    //Store the data in the local storage
					String path = storageService.store(file, a.getId()+getExtension(file));
					a.setUrl(path);
					a = aDao.save(a);
					return ResponseEntity.status(HttpStatus.OK).body(a);
				}
			}
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"Message\":\"Unauthorized\"}");
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"Message\":\"Unauthorized\"}");
	}

  //Delete an attachment by given id
	@RequestMapping(value = "/note/{noteId}/attachments/{attachmentId}", method = RequestMethod.DELETE, produces = "application/json")
	public ResponseEntity<Object> removeAttachment(@PathVariable(value = "noteId") String noteId,
			@PathVariable(value = "attachmentId") String attachmentId,
			@RequestHeader(value = "Authorization", defaultValue = "NOTAUTH") String auth) {
		if (!auth.equals("NOTAUTH")) {

			String base64Credentials = auth.substring("Basic".length()).trim();
			byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
			String credentials = new String(credDecoded, StandardCharsets.UTF_8);
			String[] values = credentials.split(":", 2);
			String username = values[0];
			String password = values[1];

			if (!uDao.checkAuth(username, password)) {
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
                            //Delete the data by given url
							storageService.delete(a.getUrl());
							nDao.save(n);
							return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No Content");
						}
					}
				}
			}
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"Message\":\"Unauthorized\"}");
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"Message\":\"Unauthorized\"}");
	}

    //Update the attachment by given id
	@RequestMapping(value = "/note/{noteId}/attachments/{attachmentId}", method = RequestMethod.PUT, produces = "application/json")
	public ResponseEntity<Object> updateAttachment(@PathVariable(value = "noteId") String noteId,
			@PathVariable(value = "attachmentId") String attachmentId, @RequestParam("file") MultipartFile file,
			@RequestHeader(value = "Authorization", defaultValue = "NOTAUTH") String auth) {
		if (!auth.equals("NOTAUTH")) {

			String base64Credentials = auth.substring("Basic".length()).trim();
			byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
			String credentials = new String(credDecoded, StandardCharsets.UTF_8);
			String[] values = credentials.split(":", 2);
			String username = values[0];
			String password = values[1];

			if (!uDao.checkAuth(username, password)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"Message\":\"Unauthorized\"}");
			}

			User u = uDao.findOneByEmail(username);
			for (Note n : u.getNotes()) {
				if (n.getId().equals(noteId)) {
					for (Attachment a : n.getAttachments()) {
						if (a.getId().equals(attachmentId)) {
                            //Delete the attachment and replace it by a new one
							storageService.delete(a.getUrl());
							String path = storageService.store(file, attachmentId+getExtension(file));
							a.setUrl(path);
							aDao.save(a);
							return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No Content");
						}
					}
				}
			}
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"Message\":\"Unauthorized\"}");
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"Message\":\"Unauthorized\"}");
	}
    //Delete some useless information in the file name
	private String getExtension(MultipartFile file) {
		String name = file.getOriginalFilename();
		int lastIndex = name.lastIndexOf(".");
		if(lastIndex==-1) {
			return "";
		}
		return name.substring(lastIndex);
	}
}
