package com.csye6225.springbootapi.dao;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.csye6225.springbootapi.pojo.Note;
import com.csye6225.springbootapi.repository.NoteRepository;

@Service
public class NoteDAO {
    //The bean will be injected automatically
	@Autowired
	private NoteRepository nr;
    //Save the note
	public Note save(Note note) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		String curr = df.format(new Date());
		//Set the created time if necessary
		//Change the updated time
		if (note.getCreated_on() == null) {
			note.setCreated_on(curr);
		}
		note.setLast_updated_on(curr);
		//Use the save and flush method because we dont have transaction
		//It will flush the data instantly
		return nr.saveAndFlush(note);
	}

}
