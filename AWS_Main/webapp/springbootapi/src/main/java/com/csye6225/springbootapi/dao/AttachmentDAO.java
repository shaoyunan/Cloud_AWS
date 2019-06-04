package com.csye6225.springbootapi.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.csye6225.springbootapi.pojo.Attachment;
import com.csye6225.springbootapi.repository.AttachmentRepository;

@Service
public class AttachmentDAO {

	@Autowired
	private AttachmentRepository ar;
	
	public Attachment save(Attachment a) {
		
		return ar.saveAndFlush(a);
	}
}
