package net.anjali.childcare.service;

import net.anjali.childcare.dto.request.AvailabilityRequest;
import net.anjali.childcare.dto.request.CaregiverProfileRequest;
import net.anjali.childcare.dto.response.AvailabilitySlotResponse;
import net.anjali.childcare.dto.response.CaregiverResponse;
import net.anjali.childcare.exception.ResourceNotfoundException;
import net.anjali.childcare.model.AvailabilitySlot;
import net.anjali.childcare.model.CaregiverProfile;
import net.anjali.childcare.model.User;
import net.anjali.childcare.repository.AvailabilitySlotRepository;
import net.anjali.childcare.repository.CaregiverRepository;
import net.anjali.childcare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CaregiverService {

    @Autowired
    private CaregiverRepository caregiverRepository;

    @Autowired
    private AvailabilitySlotRepository availabilitySlotRepository;

    @Autowired
    private UserRepository userRepository;

    // Get all verified caregivers
    public List<CaregiverResponse> getAllVerifiedCaregivers() {
        return caregiverRepository.findByIsVerifiedTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get single caregiver by id
    public CaregiverResponse getCaregiverById(Long id) {
        CaregiverProfile profile = caregiverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotfoundException("Caregiver not found"));
        return mapToResponse(profile);
    }

    // Caregiver updates own profile
    public CaregiverResponse updateProfile(String email, CaregiverProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotfoundException("User not found"));

        // Get existing profile or create new one
        CaregiverProfile profile = caregiverRepository.findByUserId(user.getId())
                .orElse(CaregiverProfile.builder().user(user).build());

        profile.setBio(request.getBio());
        profile.setHourlyRate(request.getHourlyRate());
        profile.setExperienceYears(request.getExperienceYears());
        profile.setSpecializations(request.getSpecializations());
        profile.setCity(request.getCity());

        caregiverRepository.save(profile);
        return mapToResponse(profile);
    }

    // Caregiver adds availability slot
    public AvailabilitySlotResponse addSlot(String email, AvailabilityRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotfoundException("User not found"));

        CaregiverProfile profile = caregiverRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotfoundException("Caregiver profile not found"));

        // Validate times
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new RuntimeException("Start time cannot be after end time");
        }

        if (request.getSlotDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Slot date cannot be in the past");
        }

        AvailabilitySlot slot = AvailabilitySlot.builder()
                .caregiver(profile)
                .slotDate(request.getSlotDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .isBooked(false)
                .build();

        availabilitySlotRepository.save(slot);
        return mapToSlotResponse(slot);
    }

    // Get caregiver's own slots
    public List<AvailabilitySlotResponse> getMySlots(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotfoundException("User not found"));

        CaregiverProfile profile = caregiverRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotfoundException("Caregiver profile not found"));

        return availabilitySlotRepository.findByCaregiverId(profile.getId())
                .stream()
                .map(this::mapToSlotResponse)
                .collect(Collectors.toList());
    }

    // Delete a slot
    public void deleteSlot(String email, Long slotId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotfoundException("User not found"));

        CaregiverProfile profile = caregiverRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotfoundException("Caregiver profile not found"));

        AvailabilitySlot slot = availabilitySlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotfoundException("Slot not found"));

        // Make sure caregiver can only delete their own slots
        if (!slot.getCaregiver().getId().equals(profile.getId())) {
            throw new RuntimeException("Unauthorized to delete this slot");
        }

        if (slot.getIsBooked()) {
            throw new RuntimeException("Cannot delete a booked slot");
        }

        availabilitySlotRepository.delete(slot);
    }

    // Search available caregivers by city and date
    public List<CaregiverResponse> searchCaregivers(String city, LocalDate date) {
        List<CaregiverProfile> caregivers = caregiverRepository
                .findByCityAndIsVerifiedTrue(city);

        return caregivers.stream()
                .filter(c -> !availabilitySlotRepository
                        .findByCaregiverIdAndSlotDate(c.getId(), date).isEmpty())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Map entity to response DTO
    private CaregiverResponse mapToResponse(CaregiverProfile profile) {
        return CaregiverResponse.builder()
                .id(profile.getId())
                .name(profile.getUser().getName())
                .email(profile.getUser().getEmail())
                .bio(profile.getBio())
                .hourlyRate(profile.getHourlyRate())
                .experienceYears(profile.getExperienceYears())
                .specializations(profile.getSpecializations())
                .city(profile.getCity())
                .isVerified(profile.getIsVerified())
                .averageRating(profile.getAverageRating())
                .build();
    }

    // Map slot entity to response DTO
    private AvailabilitySlotResponse mapToSlotResponse(AvailabilitySlot slot) {
        return AvailabilitySlotResponse.builder()
                .id(slot.getId())
                .slotDate(slot.getSlotDate())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .isBooked(slot.getIsBooked())
                .build();
    }
}