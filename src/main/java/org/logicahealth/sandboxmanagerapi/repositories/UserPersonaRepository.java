package org.logicahealth.sandboxmanagerapi.repositories;

import org.logicahealth.sandboxmanagerapi.model.UserPersona;
import org.logicahealth.sandboxmanagerapi.model.Visibility;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPersonaRepository extends CrudRepository<UserPersona, Integer> {
    UserPersona findByPersonaUserId(@Param("personaUserId") String personaUserId);

    UserPersona findByPersonaUserIdAndSandboxId(@Param("personaUserId") String personaUserId,
                                                @Param("sandboxId") String sandboxId);

    List<UserPersona> findBySandboxId(@Param("sandboxId") String sandboxId);

    List<UserPersona> findBySandboxIdAndCreatedByOrVisibility(@Param("sandboxId") String sandboxId,
                                                                     @Param("createdBy") String createdBy,
                                                                     @Param("visibility") Visibility visibility);

    List<UserPersona> findDefaultBySandboxId(@Param("sandboxId") String sandboxId,
                                                             @Param("createdBy") String createdBy,
                                                             @Param("visibility") Visibility visibility);

    List<UserPersona> findBySandboxIdAndCreatedBy(@Param("sandboxId") String sandboxId,
                                                                     @Param("createdBy") String createdBy);
}
