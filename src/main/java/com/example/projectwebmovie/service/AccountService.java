package com.example.projectwebmovie.service;

import com.example.projectwebmovie.dto.MyTicketDTO;
import com.example.projectwebmovie.dto.PagedTickets;
import com.example.projectwebmovie.dto.UpdateAccountDTO;
import com.example.projectwebmovie.model.Account;
import com.example.projectwebmovie.model.Booking;
import com.example.projectwebmovie.model.Member;
import com.example.projectwebmovie.model.MovieSchedule;
import com.example.projectwebmovie.repository.AccountRepository;
import com.example.projectwebmovie.repository.BookingRepository;
import com.example.projectwebmovie.repository.MemberRepository;

import com.example.projectwebmovie.repository.MovieScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ImageService imageService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private MovieScheduleRepository movieScheduleRepository;

    @Transactional
    public void updateAccount(UpdateAccountDTO updateAccountDTO) throws Exception {
        logger.info("Updating account for accountId: {}", updateAccountDTO.getAccountId());
        Account currentAccount = accountRepository.findById(updateAccountDTO.getAccountId()).orElse(null);
        if (currentAccount == null) {
            logger.error("No account found for accountId: {}", updateAccountDTO.getAccountId());
            throw new Exception("Không tìm thấy tài khoản");
        }

        currentAccount.setFullName(updateAccountDTO.getFullName());

        // Only update dateOfBirth if it's not null
        if (updateAccountDTO.getDateOfBirth() != null) {
            currentAccount.setDateOfBirth(updateAccountDTO.getDateOfBirth());
        }

        if (updateAccountDTO.getGender() != null) {
            currentAccount.setGender(updateAccountDTO.getGender().equals("1") ? "Nam" : "Nữ");
        }
        currentAccount.setEmail(updateAccountDTO.getEmail());
        currentAccount.setIdentityCard(updateAccountDTO.getIdentityCard());
        currentAccount.setPhoneNumber(updateAccountDTO.getPhoneNumber());
        currentAccount.setAddress(updateAccountDTO.getAddress());
//        if (updateAccountDTO.getPoints() != null) {
//            currentAccount.setPoints(updateAccountDTO.getPoints());
//        }

        MultipartFile image = updateAccountDTO.getImage();
        if (image != null && !image.isEmpty()) {
            try {
                String imagePath = imageService.saveAvatarImage(updateAccountDTO.getImage());
                currentAccount.setImage(imagePath);
            } catch (Exception e) {
                logger.error("Error saving avatar image: {}", e.getMessage());
                throw new Exception("Lỗi khi lưu ảnh đại diện: " + e.getMessage(), e);
            }
        }

        accountRepository.save(currentAccount);
        logger.info("Account updated successfully for accountId: {}, new data: {}", currentAccount.getAccountId(),
                currentAccount);
    }

    public List<Account> getAllAccounts() {
        logger.info("Fetching all accounts");
        return accountRepository.findAll();
    }

    public List<Account> getAllMembers() {
        logger.info("Fetching all members with roleId = 3 (USER)");
        return accountRepository.findByRoleId(3);
    }

    public List<Account> getAllEmployees() {
        logger.info("Fetching all members with roleId = 2 (EMPLOYEE)");
        return accountRepository.findByRoleId(2);
    }

    public Account getCurrentAccount() {
        logger.info("Getting current authenticated account");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.error("No authenticated user found");
            return null;
        }
        String username = authentication.getName();
        logger.info("Fetching account by username: {}", username);
        return accountRepository.findByUsername(username).orElse(null);
    }

    public Account getAccountById(String accountId) {
        logger.info("Fetching account by ID: {}", accountId);
        return accountRepository.findById(accountId).orElse(null);
    }

    public void deleteAccount(String accountEmail) throws Exception {
        logger.info("Deleting account with accountEmail: {}", accountEmail);
        Account account = accountRepository.findByEmail(accountEmail);
        Member member = memberRepository.findByAccount(account);
        if (account == null) {
            logger.error("Account not found for ID: {}", accountEmail);
            throw new Exception("Tài khoản không tồn tại");
        }
        memberRepository.delete(member);
        accountRepository.delete(account);
        logger.info("Account deleted successfully with ID: {}", accountEmail);
    }

    public Account findByEmail(String email) {
        logger.info("Finding account by email: {}", email);
        return accountRepository.findByEmail(email);
    }

    public PagedTickets getMyTicketsPaginated(String accountId, int page, int size) {
        logger.info("Getting paginated tickets for account: {}, page: {}, size: {}", accountId, page, size);

        int offset = page * size;

        // Get paginated bookings
        List<Booking> bookings = bookingRepository.findBookingsByAccountIdWithPagination(accountId, offset, size);

        // Get total count
        Integer totalCount = bookingRepository.countBookingsByAccountId(accountId);
        int totalPages = (int) Math.ceil((double) totalCount / size);

        // Convert to DTOs
        List<MyTicketDTO> tickets = bookings.stream()
                .map(this::mapToTicketDTO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        logger.info("Found {} tickets on page {} of {}", tickets.size(), page, totalPages);

        return new PagedTickets(tickets, totalPages, totalCount, page, size);
    }

    private MyTicketDTO mapToTicketDTO(Booking booking) {
        try {
            String movieTitle = booking.getMovie().getMovieNameVn();
            LocalDateTime bookingDateTime = booking.getBookingDate();
            String cinemaName = booking.getSchedule().getScheduleSeats().stream()
                    .findFirst()
                    .map(ss -> ss.getSeat().getCinemaRoom().getCinemaRoomName())
                    .orElse("Không xác định");

            // Convert seats list to comma-separated string
            String seats = booking.getBookingSeats().stream()
                    .map(bs -> bs.getScheduleSeat().getSeat().getSeatRow() +
                            bs.getScheduleSeat().getSeat().getSeatRow() + bs.getScheduleSeat().getSeatColumn())
                    .collect(Collectors.joining(", "));

            Double totalPrice = booking.getTotalPrice();
            String status = booking.getStatus().name();

            // Convert combos map to JSON string
            Map<String, Integer> combosMap = new HashMap<>();
            if (booking.getBookingCombos() != null) {
                booking.getBookingCombos().forEach(bc -> {
                    String comboName = bc.getCombo().getComboName();
                    Integer quantity = bc.getQuantity();
                    combosMap.put(comboName, quantity);
                });
            }

            // Convert map to JSON string using Jackson or simple string format
            String combos = combosMap.isEmpty() ? "{}" :
                    combosMap.entrySet().stream()
                            .map(entry -> "\"" + entry.getKey() + "\":" + entry.getValue())
                            .collect(Collectors.joining(",", "{", "}"));

            return new MyTicketDTO(
                    booking.getBookingId(),
                    movieTitle,
                    bookingDateTime,
                    cinemaName,
                    seats,        // String instead of List<String>
                    totalPrice,
                    status,
                    combos);      // String instead of Map<String, Integer>

        } catch (Exception e) {
            logger.error("Error processing booking {}: {}", booking.getBookingId(), e.getMessage());
            return null;
        }
    }

    public long countAccountsRegisteredInMonthByRole(int year, int month, int roleId) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1);
        return accountRepository.countByRegisterDateBetweenAndRoleId(startDate, endDate, roleId);
    }

    @Transactional
    public void markProfileAsCompleted(String accountId) {
        Account account = accountRepository.findByAccountId(accountId);
        if (account == null) {
            logger.error("No account found for accountId: {}", accountId);
            return;
        }
        account.setProfileCompleted(true);
        accountRepository.save(account);
    }
}