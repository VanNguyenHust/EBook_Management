package com.example.EBook_Management_BE.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.EBook_Management_BE.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long>{
	List<Comment> findByReplyIdAndReplyType(Long replyId,String replyType);
}
