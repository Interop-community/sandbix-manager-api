package org.hspconsortium.sandboxmanagerapi.repositories;

import org.hspconsortium.sandboxmanagerapi.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
    User findBySbmUserId(@Param("sbmUserId") String sbmUserId);
    User findByUserEmail(@Param("email") String email);
    String fullCount();
    String fullCountForSpecificTimePeriod(@Param("endDate") Timestamp endDate);
    String intervalCount(@Param("intervalTime") Timestamp intervalTime);
    String intervalCountForSpecificTimePeriod(@Param("beginDate") Timestamp beginDate,
                                              @Param("endDate") Timestamp endDate);
    @Query(value="DELETE FROM user WHERE sbm_user_id IS NULL AND created_timestamp < CURDATE() - INTERVAL 1 MONTH", nativeQuery = true)
    void deleteSandboxUsersWhoDidNotAcceptInvitationWithinOneMonth();
}
