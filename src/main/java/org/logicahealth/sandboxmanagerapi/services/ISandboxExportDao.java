package org.logicahealth.sandboxmanagerapi.services;

import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import org.logicahealth.sandboxmanagerapi.model.SandboxExport;
import org.logicahealth.sandboxmanagerapi.model.SandboxExportEnum;
import org.springframework.data.domain.Pageable;
import java.util.Date;
import java.util.List;



public interface ISandboxExportDao extends JpaRepository<SandboxExport, Long> {

    	@Query("SELECT j FROM SandboxExport j WHERE j.status = :status")
	    Slice<SandboxExport> findByStatus(Pageable thePage, @Param("status") SandboxExportEnum status);

		@Query("SELECT j FROM SandboxExport j WHERE j.status = :status")
	    Optional<List<SandboxExport>> findByStatus(@Param("status") SandboxExportEnum status);

		@Query("SELECT j FROM SandboxExport j WHERE j.createdTime < :createdBefore AND (j.status = :status1 OR j.status = :status2) ")
		List<SandboxExport> findExistingJob(@Param("createdBefore") Date thecreatedBefore, @Param("status1") SandboxExportEnum theStatus1,  @Param("status2") SandboxExportEnum theStatus2);

    
}
