package com.notesSharingApp.notesSharingApp.repository;

import com.notesSharingApp.notesSharingApp.model.Note;
import com.notesSharingApp.notesSharingApp.model.Status;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotesRepo extends JpaRepository<Note, String> {
    //public List<Note> findByisApproved(boolean isApproved);
    @Query("select n from Note n where n.subject.code=:code")
    public List<Note> findBySubjectID(@Param("code") String code);
    @Query("select n from Note n where n.createdBy.id=:userId")
    public List<Note> findBycreatedBy(String userId,Pageable pageable);
    List<Note> findNoteBystatus(Status status, Pageable pageable);
    @Query("select n from Note n where n.createdBy.id=:userId and n.status=:status")
    List<Note> findNotesBycreatedBy(String userId,Status status,Pageable pageable);
    @Query(value = "select status, count(status) as notes from Note where user_id = ?1 group by status",nativeQuery = true)
    List<Object[]> getCountsOfNotes(@Param("userId") String userId);
    @Query("SELECT n FROM Note n JOIN FETCH n.createdBy")
    List<Note> findNotes(String Id);
}