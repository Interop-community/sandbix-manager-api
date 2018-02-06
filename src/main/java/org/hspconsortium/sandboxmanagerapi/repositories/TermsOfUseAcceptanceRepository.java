package org.hspconsortium.sandboxmanagerapi.repositories;

import org.hspconsortium.sandboxmanagerapi.model.TermsOfUseAcceptance;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TermsOfUseAcceptanceRepository extends CrudRepository<TermsOfUseAcceptance, Integer> {
}
