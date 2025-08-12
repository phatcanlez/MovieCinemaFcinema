package com.example.projectwebmovie.repository;

import com.example.projectwebmovie.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {
    @EntityGraph(attributePaths = {"account"})
    Page<Employee> findAll(Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE e.account IS NOT NULL AND " +
            "(LOWER(e.account.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.account.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.account.phoneNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.account.identityCard) LIKE LOWER(CONCAT('%', :search, '%')))")
    @EntityGraph(attributePaths = {"account"})
    Page<Employee> findBySearch(@Param("search") String search, Pageable pageable);
}