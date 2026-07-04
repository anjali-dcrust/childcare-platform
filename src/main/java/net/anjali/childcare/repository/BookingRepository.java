package net.anjali.childcare.repository;


import net.anjali.childcare.enums.BookingStatus;
import net.anjali.childcare.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByParentId(Long parentId);

    List<Booking> findByCaregiverId(Long caregiverId);

    List<Booking> findByParentIdAndStatus(Long parentId, BookingStatus status);

    List<Booking> findByCaregiverIdAndStatus(Long caregiverId, BookingStatus status);

    Boolean existsBySlotIdAndStatusNot(Long slotId, BookingStatus status);
}