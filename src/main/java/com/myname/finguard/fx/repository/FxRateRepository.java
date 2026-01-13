package com.myname.finguard.fx.repository;

import com.myname.finguard.fx.model.FxRate;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FxRateRepository extends JpaRepository<FxRate, Long> {

    @Query("select max(r.asOf) from FxRate r where r.baseCurrency = :baseCurrency")
    Instant findLatestAsOf(@Param("baseCurrency") String baseCurrency);

    List<FxRate> findByBaseCurrencyAndAsOf(String baseCurrency, Instant asOf);

    Optional<FxRate> findTopByBaseCurrencyAndQuoteCurrencyOrderByAsOfDesc(String baseCurrency, String quoteCurrency);

    void deleteByBaseCurrencyAndAsOf(String baseCurrency, Instant asOf);
}

