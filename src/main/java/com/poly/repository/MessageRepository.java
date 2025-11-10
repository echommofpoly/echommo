package com.poly.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.poly.model.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Lấy lịch sử chat giữa 2 người dùng dựa trên username
    @Query("SELECT m FROM Message m " +
           "WHERE m.sender.username = :usernameA AND m.receiver.username = :usernameB " +
           "OR m.sender.username = :usernameB AND m.receiver.username = :usernameA " +
           "ORDER BY m.sentAt ASC")
    List<Message> findConversationByUsernames(@Param("usernameA") String usernameA,
                                              @Param("usernameB") String usernameB);
}
