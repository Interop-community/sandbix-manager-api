package org.hspconsortium.sandboxmanagerapi.repositories;

import org.hspconsortium.sandboxmanagerapi.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
    User findBySbmUserId(@Param("sbmUserId") String sbmUserId);
    User findByUserEmail(@Param("email") String email);
    User findById(@Param("id") Integer id);
    String fullCount();
    String fullCountForSpecificTimePeriod(@Param("endDate") Timestamp endDate);
    String intervalCount(@Param("intervalTime") Timestamp intervalTime);
    String intervalCountForSpecificTimePeriod(@Param("beginDate") Timestamp beginDate,
                                              @Param("endDate") Timestamp endDate);
}
