package com.example.EBook_Management_BE.controllers;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.EBook_Management_BE.components.LocalizationUtils;
import com.example.EBook_Management_BE.dtos.FollowDTO;
import com.example.EBook_Management_BE.entity.Follow;
import com.example.EBook_Management_BE.entity.User;
import com.example.EBook_Management_BE.constants.Uri;
import com.example.EBook_Management_BE.mappers.FollowMapper;
import com.example.EBook_Management_BE.responses.FollowResponse;
import com.example.EBook_Management_BE.services.follow.IFollowRedisService;
import com.example.EBook_Management_BE.services.follow.IFollowService;
import com.example.EBook_Management_BE.services.user.IUserService;
import com.example.EBook_Management_BE.utils.MessageKeys;
import com.example.EBook_Management_BE.utils.ResponseObject;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = Uri.FOLLOW)
@Validated
@RequiredArgsConstructor
public class FollowController {
    private final IFollowService followService;
    private final IFollowRedisService followRedisService;
    private final IUserService userService;

    private final LocalizationUtils localizationUtils;

    @Autowired
    private FollowMapper followMapper;

    @PostMapping()
//    @PreAuthorize("hasRole('ROLE_USER')")
    @ResponseStatus(code = HttpStatus.CREATED)
    public ResponseEntity<ResponseObject> createFollow(@Valid @RequestBody FollowDTO followDTO)
            throws Exception {
        userService.getUserById(followDTO.getFollowing());

        User follower = userService.getUserById(followDTO.getUserId());

        Follow newFollow = Follow.builder()
                .following(followDTO.getFollowing())
                .user(follower)
                .build();

        newFollow = followService.createFollow(newFollow);

        FollowResponse followResponse = followMapper.mapToFollowResponse(newFollow);

        return ResponseEntity.ok(ResponseObject.builder()
                .status(HttpStatus.CREATED)
                .message(localizationUtils.getLocalizedMessage(MessageKeys.FOLLOW_CREATE_SUCCESSFULLY))
                .data(followResponse)
                .build());
    }

    @GetMapping("/{id}")
//    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject> getFollowById(@PathVariable Long id) throws Exception {
        Follow existingFollow = followService.getFollowById(id);

        FollowResponse followResponse = followMapper.mapToFollowResponse(existingFollow);

        return ResponseEntity.ok(ResponseObject.builder()
                .status(HttpStatus.OK)
                .message(localizationUtils.getLocalizedMessage(MessageKeys.FOLLOW_GET_BY_ID_SUCCESSFULLY))
                .data(followResponse)
                .build());
    }

    @GetMapping("/all/{typeGet}/{id}")
//    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject> getAllFollow(@PathVariable Long id, @PathVariable String typeGet) throws Exception {
        String messageKey = typeGet.equals("following")
                ? MessageKeys.FOLLOW_GET_ALL_BY_FOLLOWING_SUCCESSFULLY
                : MessageKeys.FOLLOW_GET_ALL_BY_USER_ID_SUCCESSFULLY;

        Set<Follow> follows = followRedisService.getAllFollow(typeGet, id);
        if (follows == null) {
            if (typeGet.equals("following")) {
                follows = followService.getAllFollowByFollowing(id);
            } else {
                follows = followService.getAllFollowByUserId(id);
            }

            followRedisService.saveAllFollow(typeGet, id, follows);
        }

        Set<FollowResponse> followResponses = new HashSet<FollowResponse>();
        for (Follow follow : follows) {
            followResponses.add(followMapper.mapToFollowResponse(follow));
        }

        return ResponseEntity.ok(ResponseObject.builder()
                .message(localizationUtils.getLocalizedMessage(messageKey))
                .status(HttpStatus.OK)
                .data(followResponses)
                .build());
    }

    @DeleteMapping("/{id}")
//    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ResponseObject> deleteFollow(@PathVariable Long id) throws Exception {
        followService.deleteFollow(id);

        return ResponseEntity.ok(ResponseObject.builder()
                .status(HttpStatus.OK)
                .message(localizationUtils.getLocalizedMessage(MessageKeys.FOLLOW_DELETE_SUCCESSFULLY))
                .build());
    }

    @GetMapping("/{userId1}/{userId2}")
    public ResponseEntity<ResponseObject> getFollowByTwoUserId(@PathVariable Long userId1, @PathVariable Long userId2) throws Exception {
        User user = userService.getUserById(userId2);

        Follow follow = followService.getFollowByTwoUser(userId1, user);

        FollowResponse followResponse;
        if (follow == null) {
            followResponse = FollowResponse.builder().id(0L).build();
        } else {
            followResponse = followMapper.mapToFollowResponse(follow);
        }

        return ResponseEntity.ok(ResponseObject.builder()
                .message(localizationUtils.getLocalizedMessage(MessageKeys.FOLLOW_GET_BY_ID_SUCCESSFULLY))
                .status(HttpStatus.OK)
                .data(followResponse)
                .build());
    }
}
