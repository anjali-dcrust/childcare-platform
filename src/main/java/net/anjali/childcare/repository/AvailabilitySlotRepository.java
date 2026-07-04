package net.anjali.childcare.repository;

import net.anjali.childcare.model.AvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {

    List<AvailabilitySlot> findByCaregiverId(Long caregiverId);

    List<AvailabilitySlot> findByCaregiverIdAndIsBookedFalse(Long caregiverId);

    List<AvailabilitySlot> findBySlotDateAndIsBookedFalse(LocalDate slotDate);

    List<AvailabilitySlot> findByCaregiverIdAndSlotDate(Long caregiverId, LocalDate slotDate);
}