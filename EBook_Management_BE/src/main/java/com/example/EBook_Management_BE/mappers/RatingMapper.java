package com.example.EBook_Management_BE.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.example.EBook_Management_BE.dtos.RatingDTO;
import com.example.EBook_Management_BE.entity.Rating;
import com.example.EBook_Management_BE.responses.RatingResponse;

@Mapper(componentModel = "spring")
public interface RatingMapper {
	RatingMapper INSTANCE = Mappers.getMapper(RatingMapper.class);
	
	Rating mapToRatingEntity(RatingDTO ratingDTO);
		
	RatingResponse mapToRatingResponse(Rating rating);
}
