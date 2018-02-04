package com.bookcatalog.registration;

import com.bookcatalog.registration.UserRegistrationService;
import com.bookcatalog.registration.model.Role;
import com.bookcatalog.registration.model.User;
import com.bookcatalog.registration.model.UserRegistrationDto;
import com.bookcatalog.registration.repositories.UserRepository;
import com.bookcatalog.registration.validation.EmailExistsException;
import com.bookcatalog.registration.validation.UsernameExistsException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class UserRegistrationServiceTests {
    @Mock
    private UserRepository userRepository;
    private UserRegistrationService userRegistrationService;
    private UserRegistrationDto userRegistrationDto;
    private User newlyCreatedUser;

    @Before
    public void setUp() throws EmailExistsException, UsernameExistsException {
        userRegistrationService = new UserRegistrationService(userRepository);

        userRegistrationDto = new UserRegistrationDto();
        userRegistrationDto.setUsername("user1");
        userRegistrationDto.setPassword("password1");
        userRegistrationDto.setEmail("test@test.com");

        newlyCreatedUser = userRegistrationService.registerNewUserAccount(userRegistrationDto);
    }

    @Test
    public void registerNewUserAccountSetsAllFieldsFromDto() {
        User expectedUser = new User();
        expectedUser.setUsername("user1");
        expectedUser.setPassword("password1");
        expectedUser.setEmail("test@test.com");

        assertEquals(expectedUser.getUsername(), newlyCreatedUser.getUsername());
        assertEquals(expectedUser.getEmail(), newlyCreatedUser.getEmail());
        assertEquals(expectedUser.getPassword(), newlyCreatedUser.getPassword());
    }

    @Test
    public void registerNewUserAccountSavesUserToDatabase() {
        Mockito.verify(userRepository, Mockito.times(1)).save(Matchers.any(User.class));
    }

    @Test
    public void userAccountIsDisabledWhenCreated() {
        assertFalse(newlyCreatedUser.isEnabled());
    }

    @Test
    public void userHasOnlyUserRoleWhenCreated() {
        Collection<Role> roles = newlyCreatedUser.getRoles();
        assertThat(roles, hasItems(new Role("ROLE_USER")));
        assertThat(roles.size(), is(1));
    }

    @Test(expected = EmailExistsException.class)
    public void throwsEmailExistsExceptionWhenUserWithGivenEmailAlreadyExists() throws EmailExistsException, UsernameExistsException {
        Mockito.when(userRepository.findByEmail("test@test.com")).thenReturn(new User());
        userRegistrationService.registerNewUserAccount(userRegistrationDto);
    }

    @Test(expected = Test.None.class)
    public void doesNotThrowsEmailExistsExceptionIfUserDoesNotExistsYet() throws EmailExistsException, UsernameExistsException {
        Mockito.when(userRepository.findByEmail("test@test.com")).thenReturn(null);
        userRegistrationService.registerNewUserAccount(userRegistrationDto);
    }

    @Test(expected = UsernameExistsException.class)
    public void throwsUsernameExistsExceptionWhenUserAlreadyExists() throws EmailExistsException, UsernameExistsException {
        Mockito.when(userRepository.findByUsername("user1")).thenReturn(new User());
        userRegistrationService.registerNewUserAccount(userRegistrationDto);
    }

    @Test(expected = Test.None.class)
    public void doesNotThrowUsernameExistsExceptionWhenUserDoesNotExistYet() throws EmailExistsException, UsernameExistsException {
        Mockito.when(userRepository.findByUsername("user1")).thenReturn(null);
        userRegistrationService.registerNewUserAccount(userRegistrationDto);
    }
}