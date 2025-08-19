package com.notesSharingApp.notesSharingApp.repository;

import com.notesSharingApp.notesSharingApp.DTO.NotesWithoutImagesDTO;
import com.notesSharingApp.notesSharingApp.model.Note;
import com.notesSharingApp.notesSharingApp.model.Status;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.notesSharingApp.notesSharingApp.model.Status;

import java.util.List;

@Repository
public interface NotesRepo extends JpaRepository<Note, String> {

     @Query("select n from Note n where n.status=\"Pending\"")
     List<Note> findApprovalPendingNotes(Pageable pageable);

     @Query("select n from Note n where n.subject.code=:code and n.status=\"Approved\"")
     List<Note> findBySubjectCode(@Param("code") String code,Pageable pageable);

     @Query("select n from Note n where n.subject.code=:code and n.status=\"Approved\" and lower(n.title) like lower(concat('%', :query ,'%'))")
     List<Note> findBySubjectCodeAndQuery(@Param("code") String code,Pageable pageable,@Param("query") String query);

     @Query("select n from Note n where n.createdBy.id=:userId")
     List<Note> findBycreatedBy(String userId,Pageable pageable);

    @Query("select n from Note n where n.createdBy.id=:userId or (lower(n.title) like lower(concat('%', :query ,'%')) or lower(n.description) like lower(concat('%', :query ,'%'))) ")
    List<Note> findBycreatedByAndQuery(String userId,Pageable pageable,@Param("query") String query);

     @Query("SELECT n FROM Note n WHERE n.createdBy.id =:userId AND (n.status =:status OR lower(n.title) LIKE lower(concat('%', :query ,'%'))) ")
     List<Note> findNotesBycreatedByAndStatusAndQuery(@Param("userId") String userId,@Param("status") Status status,Pageable pageable,@Param("query") String query);

    @Query("select n from Note n where n.createdBy.id=:userId and n.status=:status")
    List<Note> findNotesBycreatedBy(String userId,Status status,Pageable pageable);

     @Query(value = "select status, count(status) as notes from Note where user_id = ?1 group by status",nativeQuery = true)
     List<Object[]> getCountsOfNotes(@Param("userId") String userId);

     @Query("SELECT n FROM Note n JOIN FETCH n.createdBy")
     List<Note> findNotes(String Id);

     @Query(value = "delete from Note n where n.id=:id")
     @Modifying
     @Transactional
     void deleteByid(@Param("id") String id);

     long countBystatus(Status status);

}