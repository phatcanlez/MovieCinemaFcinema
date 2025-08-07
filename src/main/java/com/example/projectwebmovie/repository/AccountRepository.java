package com.example.projectwebmovie.repository;

import com.example.projectwebmovie.model.Account;
import com.example.projectwebmovie.model.Booking;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    Optional<Account> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByIdentityCard(String identityCard);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    Account findByEmail(String email);

    Account findByAccountId(String accountId);

    @EntityGraph(attributePaths = { "role" })
    @Cacheable("accountsCache") // Vẫn giữ nguyên, cân nhắc xóa nếu chỉ muốn cache ở Service
    List<Account> findByRoleId(Integer roleId);

    @EntityGraph(attributePaths = { "role" })
    @Cacheable("accountsCache") // Vẫn giữ nguyên, cân nhắc xóa nếu chỉ muốn cache ở Service
    Page<Account> findByRoleId(Integer roleId, Pageable pageable);

    @EntityGraph(attributePaths = { "role" })
    @Cacheable("accountsCache") // Vẫn giữ nguyên, cân nhắc xóa nếu chỉ muốn cache ở Service
    @Query("SELECT a FROM Account a WHERE a.roleId = :roleId AND " +
            "(LOWER(a.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.phoneNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.identityCard) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Account> findByRoleIdAndSearch(@Param("roleId") Integer roleId,
            @Param("search") String search,
            Pageable pageable);

    int countByRoleId(Integer roleId);

    // Thêm phương thức để lấy điểm của tài khoản dựa trên accountId
    @Query("SELECT a.points FROM Account a WHERE a.accountId = :accountId")
    Optional<Integer> findPointsByAccountId(@Param("accountId") String accountId);

    // (Tùy chọn) Lấy điểm dựa trên username nếu cần
    @Query("SELECT a.points FROM Account a WHERE a.username = :username")
    Optional<Integer> findPointsByUsername(@Param("username") String username);

    long countByRegisterDateBetweenAndRoleId(LocalDate startDate, LocalDate endDate, Integer roleId);
    
}