package com.csye6225.springbootapi.storageservice.local;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.csye6225.springbootapi.pojo.Attachment;

public interface StorageService {

    void init();

    void store(MultipartFile file);
    
   	String store(MultipartFile file, String username, String nId, String aId);

    Stream<Path> loadAll();

    Path load(String filename);

    Resource loadAsResource(String filename);

    void deleteAll();
    
    void delete(String filename);

    String store(MultipartFile file, String aId);

	void clear(List<Attachment> attachments);
}
