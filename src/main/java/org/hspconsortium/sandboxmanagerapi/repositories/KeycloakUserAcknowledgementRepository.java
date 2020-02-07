package org.hspconsortium.sandboxmanagerapi.repositories;

import org.hspconsortium.sandboxmanagerapi.model.KeycloakUserAcknowledgment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface KeycloakUserAcknowledgementRepository extends CrudRepository<KeycloakUserAcknowledgment, Integer> {
    String findBySbmUserId(@Param("sbmUserId") String sbmUserId);
}
