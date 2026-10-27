package no.moonpark.pricing.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import no.moonpark.pricing.model.dto.client.ApiClientResponse;
import no.moonpark.pricing.model.entity.ApiClientEntity;


@Mapper(componentModel = "spring")
public interface ApiClientMapper {

    @Mapping(target = "roles", expression = "java(client.roleNames())")
    ApiClientResponse toResponse(ApiClientEntity client);
}
