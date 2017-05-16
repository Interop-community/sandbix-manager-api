package org.hspconsortium.sandboxmanager.repositories;

import org.hspconsortium.sandboxmanager.model.UserPersona;
import org.hspconsortium.sandboxmanager.model.Visibility;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserPersonaRepository extends CrudRepository<UserPersona, Integer> {
    public UserPersona findByPersonaUserId(@Param("personaUserId") String personaUserId);

    public UserPersona findByPersonaUserIdAndSandboxId(@Param("personaUserId") String personaUserId,
                                                @Param("sandboxId") String sandboxId);

    public List<UserPersona> findBySandboxId(@Param("sandboxId") String sandboxId);

    public List<UserPersona> findBySandboxIdAndCreatedByOrVisibility(@Param("sandboxId") String sandboxId,
                                                             @Param("createdBy") String createdBy,
                                                             @Param("visibility") Visibility visibility);

    public List<UserPersona> findBySandboxIdAndCreatedBy(@Param("sandboxId") String sandboxId,
                                                                     @Param("createdBy") String createdBy);
}
