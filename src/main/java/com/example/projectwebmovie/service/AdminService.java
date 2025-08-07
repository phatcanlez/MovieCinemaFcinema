package com.example.projectwebmovie.service;

import com.example.projectwebmovie.dto.EmployeeDTO;
import com.example.projectwebmovie.dto.UpdateAccountDTO;
import com.example.projectwebmovie.dto.UpdateEmployeeDTO;
import com.example.projectwebmovie.model.Account;
import com.example.projectwebmovie.model.Employee;
import com.example.projectwebmovie.repository.AccountRepository;
import com.example.projectwebmovie.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ImageService imageService;

    @Cacheable(value = "accountsCache", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #search")
    public Page<Account> getAllMembers(Pageable pageable, String search) {
        Page<Account> members;
        if (search == null || search.trim().isEmpty()) {
            members = accountRepository.findByRoleId(3, pageable);
        } else {
            members = accountRepository.findByRoleIdAndSearch(3, search, pageable);
        }
        logger.info("Fetched {} members with roleId = 3 from the database", members.getTotalElements());
        return members;
    }

    @Cacheable(value = "employeesCache", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #search")
    public Page<Employee> getAllEmployees(Pageable pageable, String search) {
        Page<Employee> employees;
        if (search == null || search.trim().isEmpty()) {
            employees = employeeRepository.findAll(pageable).map(employee -> {
                if (employee.getAccount() == null)
                    employee.setAccount(new Account());
                return employee;
            });
        } else {
            employees = employeeRepository.findBySearch(search, pageable);
        }
        logger.info("Fetched {} employees from the database", employees.getTotalElements());
        return employees;
    }

    @Cacheable(value = "accountCountsCache", key = "#roleId")
    public int countByRoleId(Integer roleId) {
        return accountRepository.countByRoleId(roleId);
    }

    @Transactional
    @CacheEvict(value = { "accountsCache", "accountCountsCache" }, allEntries = true)
    public void toggleAccountStatus(String accountId) {
        logger.info("Toggling status for account with ID: {}", accountId);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with ID: " + accountId));
        int newStatus = (account.getStatus() == 1) ? 0 : 1;
        account.setStatus(newStatus);
        accountRepository.save(account);
        logger.info("Account status updated to: {}", account.getStatus());
    }

    @Transactional
    @CacheEvict(value = { "accountsCache", "accountCountsCache" }, allEntries = true)
    public void updateAccount(UpdateAccountDTO updateAccountDTO) {
        logger.info("Updating account with ID: {}", updateAccountDTO.getAccountId());
        Account account = accountRepository.findById(updateAccountDTO.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Account not found with ID: " + updateAccountDTO.getAccountId()));

        account.setFullName(updateAccountDTO.getFullName());
        account.setDateOfBirth(updateAccountDTO.getDateOfBirth());
        account.setGender(switch (updateAccountDTO.getGender()) {
            case "1" -> "Nam";
            case "2" -> "Nữ";
            default -> account.getGender();
        });
        account.setEmail(updateAccountDTO.getEmail());
        account.setIdentityCard(updateAccountDTO.getIdentityCard());
        account.setPhoneNumber(updateAccountDTO.getPhoneNumber());
        account.setAddress(updateAccountDTO.getAddress());
//        if (updateAccountDTO.getPoints() != null) {
//            account.setPoints(updateAccountDTO.getPoints());
//        }

        MultipartFile image = updateAccountDTO.getImage();
        if (image != null && !image.isEmpty()) {
            try {
                String filePath = imageService.saveAvatarImage(image);
                account.setImage(filePath);
            } catch (Exception e) {
                logger.error("Error uploading image: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to upload image: " + e.getMessage());
            }
        }

        accountRepository.save(account);
        logger.info("Account updated successfully with ID: {}", updateAccountDTO.getAccountId());
    }

    @Transactional
    @CacheEvict(value = { "accountsCache", "employeesCache", "accountCountsCache" }, allEntries = true)
    public void addEmployee(EmployeeDTO employeeDTO) {
        logger.info("Adding new employee with username: {}", employeeDTO.getUsername());
        if (accountRepository.existsByUsername(employeeDTO.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (accountRepository.existsByIdentityCard(employeeDTO.getIdentityCard())) {
            throw new IllegalArgumentException("Identity card already exists");
        }
        if (accountRepository.existsByPhoneNumber(employeeDTO.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number already exists");
        }
        if (accountRepository.existsByEmail(employeeDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        String accountId = UUID.randomUUID().toString().substring(0, 10);
        Account account = new Account();
        account.setAccountId(accountId);
        account.setUsername(employeeDTO.getUsername());
        account.setPassword(passwordEncoder.encode(employeeDTO.getPassword()));
        account.setFullName(employeeDTO.getFullName());
        account.setDateOfBirth(employeeDTO.getDateOfBirth());
        account.setGender(switch (employeeDTO.getGender()) {
            case "1" -> "Nam";
            case "2" -> "Nữ";
            default -> "";
        });
        account.setEmail(employeeDTO.getEmail());
        account.setIdentityCard(employeeDTO.getIdentityCard());
        account.setPhoneNumber(employeeDTO.getPhoneNumber());
        account.setAddress(employeeDTO.getAddress());
        account.setRegisterDate(LocalDate.now());
        account.setRoleId(2); // Vai trò Employee
        account.setStatus(1);
        account.setPoints(0); // Default points for new employee

        MultipartFile image = employeeDTO.getImage();
        if (image != null && !image.isEmpty()) {
            try {
                String filePath = imageService.saveAvatarImage(image);
                account.setImage(filePath);
            } catch (Exception e) {
                logger.error("Error uploading image: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to upload image: " + e.getMessage());
            }
        }

        accountRepository.save(account);
        Employee employee = new Employee();
        employee.setEmployeeId(UUID.randomUUID().toString().substring(0, 10));
        employee.setAccountId(account.getAccountId());
        employee.setAccount(account);
        employeeRepository.save(employee);
    }

    @Transactional
    @CacheEvict(value = { "accountsCache", "employeesCache", "accountCountsCache" }, allEntries = true)
    public void updateEmployee(UpdateEmployeeDTO updateEmployeeDTO) {
        logger.info("Updating employee with accountId: {}", updateEmployeeDTO.getAccountId());
        Account account = accountRepository.findById(updateEmployeeDTO.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Account not found with ID: " + updateEmployeeDTO.getAccountId()));

        account.setUsername(updateEmployeeDTO.getUsername());
        account.setFullName(updateEmployeeDTO.getFullName());
        account.setDateOfBirth(updateEmployeeDTO.getDateOfBirth());
        account.setGender(switch (updateEmployeeDTO.getGender()) {
            case "1" -> "Nam";
            case "2" -> "Nữ";
            default -> account.getGender();
        });
        account.setEmail(updateEmployeeDTO.getEmail());
        account.setIdentityCard(updateEmployeeDTO.getIdentityCard());
        account.setPhoneNumber(updateEmployeeDTO.getPhoneNumber());
        account.setAddress(updateEmployeeDTO.getAddress());
        account.setRegisterDate(updateEmployeeDTO.getRegisterDate());

        MultipartFile image = updateEmployeeDTO.getImage();
        if (image != null && !image.isEmpty()) {
            try {
                String filePath = imageService.saveAvatarImage(image);
                account.setImage(filePath);
            } catch (Exception e) {
                logger.error("Error uploading image: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to upload image: " + e.getMessage());
            }
        }

        accountRepository.save(account);
        logger.info("Employee updated successfully with accountId: {}", updateEmployeeDTO.getAccountId());
    }

    @Transactional
    @CacheEvict(value = { "accountsCache", "employeesCache", "accountCountsCache" }, allEntries = true)
    public void toggleEmployeeStatus(String accountId) {
        logger.info("Toggling status for employee with ID: {}", accountId);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with ID: " + accountId));
        int newStatus = (account.getStatus() == 1) ? 0 : 1;
        account.setStatus(newStatus);
        accountRepository.save(account);
        logger.info("Employee status updated to: {}", account.getStatus());
    }
}