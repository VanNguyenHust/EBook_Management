package com.example.EBook_Management_BE.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@Table(name = "ratings")
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Rating {
	@Id
	@GeneratedValue
	@Column(name = "id")
	Long id;

	@Column(name = "score", columnDefinition = "TINYINT(2)", nullable = false)
	short score;
	
	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "book_id")
	Book book;
	
	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "user_id")
	User user;
}
