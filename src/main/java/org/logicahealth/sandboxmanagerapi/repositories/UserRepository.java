package org.logicahealth.sandboxmanagerapi.repositories;

import org.logicahealth.sandboxmanagerapi.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
    User findBySbmUserId(@Param("sbmUserId") String sbmUserId);
    User findByUserEmail(@Param("email") String email);
    String fullCount();
    String fullCountForSpecificTimePeriod(@Param("endDate") Timestamp endDate);
    String intervalCount(@Param("intervalTime") Timestamp intervalTime);
    String intervalCountForSpecificTimePeriod(@Param("beginDate") Timestamp beginDate,
                                              @Param("endDate") Timestamp endDate);
    List<User> findAllBySbmUserIdIsNullAndCreatedTimestampLessThan(Timestamp staleInviteDate);
}
