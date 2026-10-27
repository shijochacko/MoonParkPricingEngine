package no.moonpark.pricing.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import no.moonpark.pricing.dao.ParkingZoneRepository;
import no.moonpark.pricing.dao.TariffDefinitionRepository;
import no.moonpark.pricing.exceptionhandler.exception.UnknownZoneException;
import no.moonpark.pricing.mapper.TariffMapper;
import no.moonpark.pricing.validator.TariffRequestValidator;

@ExtendWith(MockitoExtension.class)
class TariffAdminServiceTest {

    @Mock
    private TariffDefinitionRepository repository;

    @Mock
    private ParkingZoneRepository parkingZoneRepository;

    @Mock
    private TariffRequestValidator tariffRequestValidator;

    @Mock
    private AuditService auditService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private TariffMapper tariffMapper;

    @Mock
    private Clock clock;

    @InjectMocks
    private TariffAdminService service;

    @Test
    void rejectsUnknownZoneInsteadOfReturningAnEmptyPage() {
        when(parkingZoneRepository.existsById("M8")).thenReturn(false);

        assertThatThrownBy(() -> service.listTariffs("M8", PageRequest.of(0, 20)))
                .isInstanceOf(UnknownZoneException.class)
                .hasMessage("Unknown rate zone: M8");

        verifyNoInteractions(repository);
    }

    @Test
    void validatesAndNormalizesZoneBeforeListingTariffs() {
        when(parkingZoneRepository.existsById("M1")).thenReturn(true);
        when(repository.findByZoneCode(eq("M1"), any(Pageable.class))).thenReturn(Page.empty());

        var result = service.listTariffs(" m1 ", PageRequest.of(0, 20));

        assertThat(result.content()).isEmpty();
        verify(repository).findByZoneCode(eq("M1"), any(Pageable.class));
    }

    @Test
    void rejectsMalformedZoneCode() {
        assertThatThrownBy(() -> service.listTariffs("M-1", PageRequest.of(0, 20)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("zone must be 1-10 letters or digits");

        verifyNoInteractions(parkingZoneRepository, repository);
    }
}
