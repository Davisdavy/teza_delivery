package com.wafula.teza.rider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wafula.teza.shared.event.UserRoleChangedEvent;
import com.wafula.teza.rider.application.RiderEventListener;

import com.wafula.teza.rider.api.dto.RiderProfileCreateRequest;
import com.wafula.teza.rider.api.dto.RiderProfileResponse;
import com.wafula.teza.rider.api.dto.RiderProfileUpdateRequest;
import com.wafula.teza.rider.application.RiderService;
import com.wafula.teza.rider.application.RiderServiceImpl;
import com.wafula.teza.rider.domain.OnboardingStatus;
import com.wafula.teza.rider.domain.RiderLocationRepository;
import com.wafula.teza.rider.domain.RiderProfile;
import com.wafula.teza.rider.domain.RiderProfileRepository;
import com.wafula.teza.shared.domain.Role;
import com.wafula.teza.shared.exception.ApiException;
import com.wafula.teza.user.application.UserAccount;
import com.wafula.teza.user.application.UserAccountService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class RiderServiceTest {

    @Mock
    private RiderProfileRepository riderProfileRepository;

    @Mock
    private RiderLocationRepository riderLocationRepository;

    @Mock
    private UserAccountService userAccountService;

    private RiderService riderService;

    @BeforeEach
    void setUp() {
        riderService = new RiderServiceImpl(riderProfileRepository, riderLocationRepository, userAccountService);
    }

    @Test
    void testCreateProfileSuccess() {
        UUID userId = UUID.randomUUID();
        RiderProfileCreateRequest request = new RiderProfileCreateRequest(com.wafula.teza.rider.domain.VehicleType.MOTORCYCLE, "DL-12345");
        UserAccount user = new UserAccount(userId, "rider@teza.local", "hash", Role.RIDER, true);

        when(riderProfileRepository.existsByUserId(userId)).thenReturn(false);
        when(userAccountService.findById(userId)).thenReturn(Optional.of(user));
        when(riderProfileRepository.save(any(RiderProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        RiderProfileResponse response = riderService.createProfile(userId, request);

        assertEquals(OnboardingStatus.PENDING, response.onboardingStatus());
        assertEquals(userId, response.userId());
        verify(riderProfileRepository).save(any(RiderProfile.class));
    }

    @Test
    void testUpdateProfileRestrictsOnboardingStatus() {
        UUID userId = UUID.randomUUID();
        RiderProfile profile = RiderProfile.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .onboardingStatus(OnboardingStatus.PENDING)
                .build();

        RiderProfileUpdateRequest request = new RiderProfileUpdateRequest(null, null, OnboardingStatus.APPROVED, null);

        when(riderProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        ApiException exception = assertThrows(ApiException.class, () ->
                riderService.updateProfile(userId, request)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("Only admins can change onboarding status", exception.getMessage());
    }

    @Test
    void testUpdateProfileSucceedsForRiderFields() {
        UUID userId = UUID.randomUUID();
        RiderProfile profile = RiderProfile.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .onboardingStatus(OnboardingStatus.PENDING)
                .build();

        RiderProfileUpdateRequest request = new RiderProfileUpdateRequest(com.wafula.teza.rider.domain.VehicleType.BICYCLE, "KBZ 123", null, true);

        when(riderProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(riderProfileRepository.save(any(RiderProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        RiderProfileResponse response = riderService.updateProfile(userId, request);

        assertEquals(OnboardingStatus.PENDING, response.onboardingStatus());
        verify(riderProfileRepository).save(profile);
    }

    @Test
    void testAdminUpdateOnboardingStatus() {
        UUID profileId = UUID.randomUUID();
        RiderProfile profile = RiderProfile.builder()
                .id(profileId)
                .userId(UUID.randomUUID())
                .onboardingStatus(OnboardingStatus.PENDING)
                .build();

        when(riderProfileRepository.findById(profileId)).thenReturn(Optional.of(profile));
        when(riderProfileRepository.save(any(RiderProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        RiderProfileResponse response = riderService.updateOnboardingStatus(profileId, OnboardingStatus.APPROVED, UUID.randomUUID());

        assertEquals(OnboardingStatus.APPROVED, response.onboardingStatus());
        verify(riderProfileRepository).save(profile);
    }

    @Test
    void testUserRoleChangedEventListenerCreatesProfile() {
        UUID userId = UUID.randomUUID();
        UserRoleChangedEvent event = new UserRoleChangedEvent(userId, Role.CUSTOMER, Role.RIDER);

        when(riderProfileRepository.existsByUserId(userId)).thenReturn(false);
        when(riderProfileRepository.save(any(RiderProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        RiderEventListener listener = new RiderEventListener(riderProfileRepository);
        listener.onUserRoleChanged(event);

        verify(riderProfileRepository).save(any(RiderProfile.class));
    }

    @Test
    void testGetAllProfiles() {
        RiderProfile profile1 = RiderProfile.builder().id(UUID.randomUUID()).userId(UUID.randomUUID()).build();
        RiderProfile profile2 = RiderProfile.builder().id(UUID.randomUUID()).userId(UUID.randomUUID()).build();
        when(riderProfileRepository.findAll()).thenReturn(List.of(profile1, profile2));

        List<RiderProfileResponse> result = riderService.getAllProfiles();

        assertEquals(2, result.size());
        verify(riderProfileRepository).findAll();
    }
}
