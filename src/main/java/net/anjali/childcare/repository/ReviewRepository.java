package net.anjali.childcare.repository;

import net.anjali.childcare.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByCaregiverId(Long caregiverId);

    List<Review> findByParentId(Long parentId);

    Optional<Review> findByBookingId(Long bookingId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.caregiver.id = :caregiverId")
    Double findAverageRatingByCaregiverId(Long caregiverId);
}