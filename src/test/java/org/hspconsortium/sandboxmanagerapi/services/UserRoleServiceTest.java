package org.hspconsortium.sandboxmanagerapi.services;

import org.hspconsortium.sandboxmanagerapi.model.UserRole;
import org.hspconsortium.sandboxmanagerapi.repositories.UserRoleRepository;
import org.hspconsortium.sandboxmanagerapi.services.impl.UserRoleServiceImpl;
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
        verify(repository).delete(userRole.getId());
    }

    @Test
    public void deleteTestByObject() {
        userRoleService.delete(userRole);
        verify(repository).delete(userRole.getId());
    }

    @Test
    public void saveTest() {
        when(repository.save(userRole)).thenReturn(userRole);
        UserRole returnedUserRole = userRoleService.save(userRole);
        assertEquals(userRole, returnedUserRole);
    }
}
