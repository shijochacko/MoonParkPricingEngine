package no.moonpark.pricing.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import no.moonpark.pricing.model.dto.user.ProfileResponse;
import no.moonpark.pricing.model.dto.user.RoleResponse;
import no.moonpark.pricing.model.dto.user.UserResponse;
import no.moonpark.pricing.model.entity.RoleEntity;
import no.moonpark.pricing.model.entity.UserEntity;


@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", expression = "java(user.roleNames())")
    UserResponse toUserResponse(UserEntity user);

    @Mapping(target = "roles", expression = "java(user.roleNames())")
    @Mapping(target = "permissions", expression = "java(user.permissionNames())")
    ProfileResponse toProfileResponse(UserEntity user);

    @Mapping(target = "permissions", expression = "java(role.permissionNames())")
    RoleResponse toRoleResponse(RoleEntity role);
}
