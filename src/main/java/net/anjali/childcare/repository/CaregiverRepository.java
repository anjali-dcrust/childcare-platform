package net.anjali.childcare.repository;


import net.anjali.childcare.model.CaregiverProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaregiverRepository extends JpaRepository<CaregiverProfile, Long> {

    Optional<CaregiverProfile> findByUserId(Long userId);

    List<CaregiverProfile> findByIsVerifiedTrue();

    List<CaregiverProfile> findByIsVerifiedFalse();

    List<CaregiverProfile> findByCityAndIsVerifiedTrue(String city);
}