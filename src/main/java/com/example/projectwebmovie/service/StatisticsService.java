package com.example.projectwebmovie.service;

import com.example.projectwebmovie.dto.*;
import com.example.projectwebmovie.model.Booking;
import com.example.projectwebmovie.repository.AccountRepository;
import com.example.projectwebmovie.repository.BookingRepository;
import com.example.projectwebmovie.repository.MovieTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final BookingRepository bookingRepository;
    private final MovieTypeRepository movieTypeRepository;
    private final AccountRepository accountRepository;
    private final AccountService accountService;

    public List<TopCustomerDTO> getTop5CustomersBySpending() {
        List<TopCustomerDTO> customers = bookingRepository.findTop5CustomersBySpending();
        return customers.stream().limit(5).toList();
    }

    public List<TopMovieDTO> getTop5MoviesByBookings() {
        List<TopMovieDTO> movies = bookingRepository.findTop5MoviesByBookings();
        return movies.stream().limit(5).toList();
    }


    public CustomerStatisticsDTO getCustomerStatistics() {
        Long totalCustomers = bookingRepository.countTotalCustomers();
        Long activeCustomers = bookingRepository.countActiveCustomers();
        return new CustomerStatisticsDTO(totalCustomers, activeCustomers);
    }

    public MovieStatisticsDTO getMovieStatistics() {
        Long showingMovies = bookingRepository.countShowingMovies();
        Long activeMovies = bookingRepository.countActiveMovies();
        return new MovieStatisticsDTO(showingMovies, activeMovies);
    }

    public BookingStatisticsDTO getBookingStatistics() {
        Long totalBookings = bookingRepository.countTotalBookings();
        Long successfulBookings = bookingRepository.countSuccessfulBookings();
        Long pendingBookings = bookingRepository.countPendingBookings();
        Long cancelledBookings = bookingRepository.countCancelledBookings();
        return new BookingStatisticsDTO(totalBookings, successfulBookings, pendingBookings, cancelledBookings);
    }

    public RevenueStatisticsDTO getRevenueByDate(LocalDate date) {
        Double revenue = bookingRepository.getRevenueByDate(date);
        Long bookings = bookingRepository.getBookingCountByDate(date);

        // Get previous day data
        LocalDate previousDate = date.minusDays(1);
        Double previousRevenue = bookingRepository.getRevenueByDate(previousDate);
        Long previousBookings = bookingRepository.getBookingCountByDate(previousDate);

        return createRevenueStatisticsWithComparison(
                date.toString(), revenue, bookings, previousRevenue, previousBookings
        );
    }

    public RevenueStatisticsDTO getRevenueByMonth(int year, int month) {
        Double revenue = bookingRepository.getRevenueByMonth(year, month);
        Long bookings = bookingRepository.getBookingCountByMonth(year, month);

        // Get previous month data
        LocalDate currentMonth = LocalDate.of(year, month, 1);
        LocalDate previousMonth = currentMonth.minusMonths(1);
        Double previousRevenue = bookingRepository.getRevenueByMonth(
                previousMonth.getYear(), previousMonth.getMonthValue()
        );
        Long previousBookings = bookingRepository.getBookingCountByMonth(
                previousMonth.getYear(), previousMonth.getMonthValue()
        );

        String period = String.format("%04d-%02d", year, month);
        return createRevenueStatisticsWithComparison(
                period, revenue, bookings, previousRevenue, previousBookings
        );
    }

    public RevenueStatisticsDTO getRevenueByYear(int year) {
        Double revenue = bookingRepository.getRevenueByYear(year);
        Long bookings = bookingRepository.getBookingCountByYear(year);

        // Get previous year data
        Double previousRevenue = bookingRepository.getRevenueByYear(year - 1);
        Long previousBookings = bookingRepository.getBookingCountByYear(year - 1);

        return createRevenueStatisticsWithComparison(
                String.valueOf(year), revenue, bookings, previousRevenue, previousBookings
        );
    }

    private RevenueStatisticsDTO createRevenueStatisticsWithComparison(
            String period, Double currentRevenue, Long currentBookings,
            Double previousRevenue, Long previousBookings) {

        // Calculate revenue comparison
        String revenueStatus = "no_change";
        Double revenueChangePercent = 0.0;

        if (previousRevenue != null && previousRevenue > 0) {
            revenueChangePercent = ((currentRevenue - previousRevenue) / previousRevenue) * 100;
            if (revenueChangePercent > 0) {
                revenueStatus = "increase";
            } else if (revenueChangePercent < 0) {
                revenueStatus = "decrease";
                revenueChangePercent = Math.abs(revenueChangePercent);
            }
        }

        // Calculate booking comparison
        String bookingStatus = "no_change";
        Double bookingChangePercent = 0.0;

        if (previousBookings != null && previousBookings > 0) {
            bookingChangePercent = ((currentBookings.doubleValue() - previousBookings.doubleValue()) / previousBookings.doubleValue()) * 100;
            if (bookingChangePercent > 0) {
                bookingStatus = "increase";
            } else if (bookingChangePercent < 0) {
                bookingStatus = "decrease";
                bookingChangePercent = Math.abs(bookingChangePercent);
            }
        }

        return new RevenueStatisticsDTO(
                period, currentRevenue, currentBookings,
                revenueStatus, Math.round(revenueChangePercent * 100.0) / 100.0,
                bookingStatus, Math.round(bookingChangePercent * 100.0) / 100.0
        );
    }

    public RevenueStatisticsDTO getRevenueBetweenDates(LocalDate startDate, LocalDate endDate) {
        Double revenue = bookingRepository.getRevenueBetweenDates(startDate, endDate);
        Long bookings = bookingRepository.getBookingCountBetweenDates(startDate, endDate);
        String period = startDate + " to " + endDate;
        return new RevenueStatisticsDTO(period, revenue, bookings);
    }
    public Long getBookingsCountByDate(LocalDate date) {
        return bookingRepository.getBookingCountByDate(date);
    }

    public Long getBookingsCountByMonth(int year, int month) {
        return bookingRepository.getBookingCountByMonth(year, month);
    }

    public Long getBookingsCountByYear(int year) {
        return bookingRepository.getBookingCountByYear(year);
    }

    public Long getBookingsCountBetweenDates(LocalDate startDate, LocalDate endDate) {
        return bookingRepository.getBookingCountBetweenDates(startDate, endDate);
    }
    public List<TopPhimHotDTO> getTopPhimHotByBooking() {
        List<TopPhimHotDTO> movies = bookingRepository.findAllMoviesByBookingAndRevenue();

        // Set types for each movie
        for (TopPhimHotDTO movie : movies) {
            List<String> types = movieTypeRepository.findTypeNamesByMovieId(movie.getMovieId());
            movie.setTypes(types.isEmpty() ? List.of("Chưa phân loại") : types);
        }

        return movies;
    }
    public List<TopPhimHotDTO> getAllMoviesByBookingAndRevenue() {
        return getTopPhimHotByBooking();
    }

    public AccountStaticsResponseDTO getAccountStatistics(int year, int month, int memberRoleId) {
    // Tổng số thành viên có roleId = memberRoleId
    long total = accountRepository.countByRoleId(memberRoleId);

    // Số thành viên đăng ký trong tháng này
    long currentMonth = accountService.countAccountsRegisteredInMonthByRole(year, month, memberRoleId);

    // Số thành viên đăng ký trong tháng trước (để so sánh tăng/giảm)
    int previousMonth = (month == 1) ? 12 : month - 1;
    int previousYear = (month == 1) ? year - 1 : year;
    long previousMonthCount = accountService.countAccountsRegisteredInMonthByRole(previousYear, previousMonth, memberRoleId);

    // Tính số tăng/thay đổi
    int increased = (int) (currentMonth - previousMonthCount);

    return new AccountStaticsResponseDTO((int) total, increased);
    }

}