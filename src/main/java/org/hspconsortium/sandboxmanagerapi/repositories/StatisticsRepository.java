package org.hspconsortium.sandboxmanagerapi.repositories;

import org.hspconsortium.sandboxmanagerapi.model.FhirTransaction;
import org.hspconsortium.sandboxmanagerapi.model.Statistics;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface StatisticsRepository extends CrudRepository<Statistics, Integer> {
    List<Statistics> get12MonthStatistics(@Param("yearAgoTimestamp") Timestamp yearAgoTimestamp,
                                          @Param("currentTimestamp") Timestamp currentTimestamp);
    Integer getFhirTransaction(@Param("fromTimestamp") Timestamp fromTimestamp,
                                          @Param("toTimestamp") Timestamp toTimestamp);

}
