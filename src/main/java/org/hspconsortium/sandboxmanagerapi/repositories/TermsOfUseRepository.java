package org.hspconsortium.sandboxmanagerapi.repositories;

import org.hspconsortium.sandboxmanagerapi.model.TermsOfUse;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TermsOfUseRepository extends CrudRepository<TermsOfUse, Integer> {
    List<TermsOfUse> orderByCreatedTimestamp();
}
