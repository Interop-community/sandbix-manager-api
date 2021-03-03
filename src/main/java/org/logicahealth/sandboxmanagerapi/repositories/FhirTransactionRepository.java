package org.logicahealth.sandboxmanagerapi.repositories;

import org.logicahealth.sandboxmanagerapi.model.FhirTransaction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FhirTransactionRepository extends CrudRepository<FhirTransaction, Integer> {
    List<FhirTransaction> findByPayerUserId(@Param("payerUserId") Integer payerUserId);
    List<FhirTransaction> findBySandboxId(@Param("sandboxId") Integer sandboxId);
}
