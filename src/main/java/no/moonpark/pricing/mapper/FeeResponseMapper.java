package no.moonpark.pricing.mapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import no.moonpark.pricing.model.domain.FeeQuote;
import no.moonpark.pricing.model.dto.fee.FeeResponse;


/**
 * Maps a domain {@link FeeQuote} to the JSON response. Times are shown in the garage time zone.
 */
@Mapper(componentModel = "spring")
public interface FeeResponseMapper {

    @Mapping(target = "zone", expression = "java(quote.zone().value())")
    @Mapping(target = "start", expression = "java(toLocalTime(quote.period().start(), timeZone))")
    @Mapping(target = "end", expression = "java(toLocalTime(quote.period().end(), timeZone))")
    @Mapping(target = "durationMinutes", expression = "java(quote.period().startedMinutes())")
    @Mapping(target = "amount", expression = "java(quote.fee().amount())")
    @Mapping(target = "currency", expression = "java(quote.fee().currency().getCurrencyCode())")
    @Mapping(target = "tariffVersion", source = "tariffVersion")
    FeeResponse toResponse(FeeQuote quote, @Context ZoneId timeZone);

    default OffsetDateTime toLocalTime(Instant instant, ZoneId timeZone) {
        return instant.atZone(timeZone).toOffsetDateTime();
    }
}
