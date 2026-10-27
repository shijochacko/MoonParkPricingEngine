package no.moonpark.pricing.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import no.moonpark.pricing.service.CachedTariffRegistry;

/**
 * Reloads tariffs from the database at a fixed interval ({@code pricing.tariff-refresh}, default 60s).
 * This is how tariff changes made on one pod reach all other pods.
 */
@Component
@RequiredArgsConstructor
public class TariffRefreshScheduler {

    private final CachedTariffRegistry tariffRegistry;

    @Scheduled(fixedDelayString = "${pricing.tariff-refresh:PT60S}", initialDelayString = "${pricing.tariff-refresh:PT60S}")
    public void refreshTariffs() {
        tariffRegistry.refresh();
    }
}
