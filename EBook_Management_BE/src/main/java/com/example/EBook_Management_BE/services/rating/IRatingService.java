package com.example.EBook_Management_BE.services.rating;

import java.util.Set;

import com.example.EBook_Management_BE.entity.Book;
import com.example.EBook_Management_BE.entity.Rating;
import com.example.EBook_Management_BE.exceptions.DataNotFoundException;

public interface IRatingService {
	Rating createRating(Rating rating);
	
	Rating getRatingById(Long ratingId) throws DataNotFoundException;
	
	Rating updateRating(Long ratingId, Rating rating);

	void deleteRating(Rating rating);
	
	Set<Rating> getAllRatingByBook(Book book) throws DataNotFoundException;
}
