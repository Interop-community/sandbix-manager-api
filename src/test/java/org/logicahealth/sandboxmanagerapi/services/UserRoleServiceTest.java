package org.logicahealth.sandboxmanagerapi.services;

import org.logicahealth.sandboxmanagerapi.model.UserRole;
import org.logicahealth.sandboxmanagerapi.repositories.UserRoleRepository;
import org.logicahealth.sandboxmanagerapi.services.impl.UserRoleServiceImpl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserRoleServiceTest {

    private UserRoleRepository repository = mock(UserRoleRepository.class);

    private UserRoleServiceImpl userRoleService = new UserRoleServiceImpl(repository);

    private UserRole userRole;

    @Before
    public void setup() {
        userRole = new UserRole();
        userRole.setId(1);
    }

    @Test
    public void deleteTest() {
        userRoleService.delete(userRole.getId());
        verify(repository).deleteById(userRole.getId());
    }

    @Test
    public void deleteTestByObject() {
        userRoleService.delete(userRole);
        verify(repository).deleteById(userRole.getId());
    }

    @Test
    public void saveTest() {
        when(repository.save(userRole)).thenReturn(userRole);
        UserRole returnedUserRole = userRoleService.save(userRole);
        assertEquals(userRole, returnedUserRole);
    }
}
