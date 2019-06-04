package com.csye6225.springbootapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.csye6225.springbootapi.pojo.Attachment;

public interface AttachmentRepository extends JpaRepository<Attachment, String>{

}
