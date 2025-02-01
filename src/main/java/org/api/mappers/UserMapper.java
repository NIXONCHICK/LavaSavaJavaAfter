package org.api.mappers;

import org.api.dto.user.ProfileResponse;
import org.api.dto.user.UserResponse;
import org.api.entities.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public UserResponse toUserResponse(User user, String token) {
    return UserResponse.builder()
        .username(user.getUsername())
        .email(user.getEmail())
        .token(token)
        .bio(user.getBio())
        .image(user.getImage())
        .build();
  }

  public ProfileResponse toProfileResponse(User user, boolean following) {
    return ProfileResponse.builder()
        .username(user.getUsername())
        .bio(user.getBio())
        .image(user.getImage())
        .following(following)
        .build();
  }

  public ProfileResponse toProfileResponse(User user) {
    return toProfileResponse(user, false);
  }
}
