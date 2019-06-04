package com.csye6225.springbootapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.csye6225.springbootapi.pojo.Note;

public interface NoteRepository extends JpaRepository<Note, String> {

	// Note findById(String id);

	// List<Note> findNoteByUsername(String username);
	// Note findOneByEmail(String username);

	// @Query(value="SELECT * FROM notes WHERE user_id = ?1", nativeQuery=true)
	// List<Note> findAllByUser(Long id);

}
