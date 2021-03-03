package org.logicahealth.sandboxmanagerapi.repositories;

import org.logicahealth.sandboxmanagerapi.model.TermsOfUseAcceptance;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TermsOfUseAcceptanceRepository extends CrudRepository<TermsOfUseAcceptance, Integer> {
}
