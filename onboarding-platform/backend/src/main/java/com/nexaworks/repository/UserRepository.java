package com.nexaworks.repository;

import com.nexaworks.entity.User;
import com.nexaworks.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(Role role);
    List<User> findByDepartment(String department);
    List<User> findByManagerName(String managerName);
    List<User> findByRoleAndDepartment(Role role, String department);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.riskScore >= :minRisk ORDER BY u.riskScore DESC")
    List<User> findHighRiskByRole(Role role, int minRisk);

    @Query("SELECT u FROM User u WHERE u.riskScore >= :minRisk ORDER BY u.riskScore DESC")
    List<User> findAllHighRisk(int minRisk);

    @Query("SELECT u FROM User u WHERE u.alertSent = false AND u.riskScore >= 50")
    List<User> findUnalertedHighRisk();

    long countByRole(Role role);
    long countByOnboardingComplete(boolean complete);
    long countByRoleAndOnboardingComplete(Role role, boolean complete);
}
