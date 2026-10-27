package no.moonpark.pricing.mapper;

import org.mapstruct.Mapper;
import no.moonpark.pricing.model.dto.tariff.TariffResponse;
import no.moonpark.pricing.model.entity.TariffDefinitionEntity;


@Mapper(componentModel = "spring")
public interface TariffMapper {

    TariffResponse toResponse(TariffDefinitionEntity tariff);
}
