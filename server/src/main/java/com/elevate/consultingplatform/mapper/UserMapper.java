package com.elevate.consultingplatform.mapper;

import com.elevate.consultingplatform.dto.user.UserResponse;
import com.elevate.consultingplatform.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "emailVerified", target = "isEmailVerified")
    @Mapping(source = "active", target = "isActive")
    UserResponse toUserResponse(User user);

    default String map(boolean value) {
        return value ? "ACTIVE" : "INACTIVE";
    }
}
