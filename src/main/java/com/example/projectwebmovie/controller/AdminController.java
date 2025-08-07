package com.example.projectwebmovie.controller;

import com.example.projectwebmovie.dto.*;
import com.example.projectwebmovie.dto.promotion.PromotionDTO;
import com.example.projectwebmovie.enums.MovieVersion;
import com.example.projectwebmovie.model.Account;
import com.example.projectwebmovie.model.CinemaRoom;
import com.example.projectwebmovie.model.Employee;
import com.example.projectwebmovie.repository.TypeRepository;
import com.example.projectwebmovie.service.*;
import com.example.projectwebmovie.wapper.SeatForCreateDTOListWrapper;
import com.example.projectwebmovie.dto.promotion.RequestPromotionDTO;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private TypeRepository typeRepository;
    @Autowired
    private AdminService adminService;
    @Autowired
    private MovieService movieService;
    @Autowired
    private CinemaRoomService cinemaRoomService;
    @Autowired
    private SeatService seatService;
    @Autowired
    private PromotionService promotionService;
    @Autowired
    private ComboService comboService;

    @GetMapping("/dashboard")
    public String showAdminPage(Model model) {
        int totalMovies = movieService.getTotalMovie();
        int totalMembers = adminService.countByRoleId(3);
        int totalEmployees = adminService.countByRoleId(2);
        logger.info("Total movies: {}", totalMovies);
        model.addAttribute("totalMovies", totalMovies);
        model.addAttribute("totalMembers", totalMembers);
        model.addAttribute("totalEmployees", totalEmployees);
        return "admin/dashboard";
    }

    @GetMapping("/member-management")
    public String showMemberManagement(Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(required = false) String search) {
        String effectiveSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : "";
        Pageable pageable = PageRequest.of(page, size);
        Page<Account> memberPage = adminService.getAllMembers(pageable, effectiveSearch);
        logger.info("Passing {} members to the member-management view", memberPage.getNumberOfElements());
        model.addAttribute("accounts", memberPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", memberPage.getTotalPages());
        model.addAttribute("pageSize", size);
        model.addAttribute("search", effectiveSearch);
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy - HH:mm");
        model.addAttribute("currentDateTime", now.format(formatter));
        model.addAttribute("adminEmail", "admin@cinema.com");
        Map<String, Integer> accountStats = new HashMap<>();
        accountStats.put("totalAccounts", (int) memberPage.getTotalElements());
        accountStats.put("adminCount", adminService.countByRoleId(1));
        accountStats.put("employeeCount", adminService.countByRoleId(2));
        accountStats.put("customerCount", adminService.countByRoleId(3));
        model.addAttribute("accountStats", accountStats);
        return "admin/member-management";
    }

    @GetMapping("/employee-management")
    public String showEmployeeManagement(Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Employee> employeePage = adminService.getAllEmployees(pageable, search);
        logger.info("Passing {} employees to the employee-management view", employeePage.getNumberOfElements());
        model.addAttribute("employees", employeePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", employeePage.getTotalPages());
        model.addAttribute("pageSize", size);
        model.addAttribute("search", search);
        Map<String, Integer> employeeStats = new HashMap<>();
        employeeStats.put("totalEmployees", (int) employeePage.getTotalElements());
        model.addAttribute("employeeStats", employeeStats);
        return "admin/employee-management";
    }

    @PostMapping("/add-employee")
    @ResponseBody
    public String addEmployee(@ModelAttribute EmployeeDTO employeeDTO,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            if (image != null && !image.isEmpty()) {
                employeeDTO.setImage(image);
            }
            adminService.addEmployee(employeeDTO);
            return "success";
        } catch (Exception e) {
            logger.error("Error adding employee: {}", e.getMessage(), e);
            return "error: " + e.getMessage();
        }
    }

    @PostMapping("/update-employee")
    @ResponseBody
    public String updateEmployee(@ModelAttribute UpdateEmployeeDTO updateEmployeeDTO,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            if (image != null && !image.isEmpty()) {
                updateEmployeeDTO.setImage(image);
            }
            adminService.updateEmployee(updateEmployeeDTO);
            return "success";
        } catch (Exception e) {
            logger.error("Error updating employee: {}", e.getMessage(), e);
            return "error: " + e.getMessage();
        }
    }

    @PostMapping("/toggle-employee-status")
    public String toggleEmployeeStatus(@RequestParam("accountId") String accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        adminService.toggleEmployeeStatus(accountId);
        String redirectUrl = "/admin/employee-management?page=" + page + "&size=" + size;
        if (search != null && !search.trim().isEmpty()) {
            redirectUrl += "&search=" + search;
        }
        return "redirect:" + redirectUrl;
    }

    @GetMapping("/room-management")
    public String showRoomManagement(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(required = false) String search,
            Model model) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("cinemaRoomId").ascending());
        Page<CinemaRoomDTO> roomPage = cinemaRoomService.getAll(pageable, search);

        model.addAttribute("roomPage", roomPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("search", search);

        return "admin/room-management";
    }

    @GetMapping("/add-room")
    public String showAddRoomForm() {
        return "admin/add-room";
    }

    @GetMapping("/add-room/room-template")
    public String generateRoomSeats(@RequestParam int maxRow,
            @RequestParam int maxCol,
            @RequestParam String roomName,
            Model model) {
        if (cinemaRoomService.isExistNameRoom(roomName)) {
            model.addAttribute("errorNameMess", "Room \"" + roomName + "\" is exist in database!");
        }
        if (maxRow * maxCol < 40) {
            model.addAttribute("errorSeatQuantityMess", "The number of seats must be greater than or equal to 40.");
        }
        if (!(cinemaRoomService.isExistNameRoom(roomName) || maxRow * maxCol < 40)) {
            List<String> columnList = seatService.generateColumnList(maxCol);
            SeatForCreateDTOListWrapper seatListWrapper = new SeatForCreateDTOListWrapper();
            seatListWrapper.setSeatList(seatService.createNewSeatMatrixList(maxCol, maxRow));
            model.addAttribute("seatListWrapper", seatListWrapper);
            model.addAttribute("columnList", columnList);
        }
        model.addAttribute("maxCol", maxCol);
        model.addAttribute("roomName", roomName);
        model.addAttribute("maxRow", maxRow);
        return "admin/add-room";
    }

    @PostMapping("/save-seats")
    public String saveSeats(@RequestParam String roomName,
            @ModelAttribute SeatForCreateDTOListWrapper seatListWrapper) {
        cinemaRoomService.addNewRoom(roomName, seatListWrapper.getSeatList());
        return "redirect:/admin/room-management";
    }

    @GetMapping("/movie-management")
    public String showShowtimes(Model model,
            @RequestParam(defaultValue = "1") int page, // Nên để mặc định là 0 vì PageRequest.of(page, size) sử dụng //
            @RequestParam(defaultValue = "5") int size, //
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<AdminViewMovieListDTO> moviePage = movieService.getAllInAdminPage(pageable, search);
        List<CinemaRoom> rooms = movieService.getAllCinemaRooms();
        model.addAttribute("movieVersion", MovieVersion.values());
        // model.addAttribute("movieStatus", MovieStatus.values());
        model.addAttribute("allTypes", typeRepository.findAll());
        model.addAttribute("rooms", rooms);
        model.addAttribute("movies", moviePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", moviePage.getTotalPages());
        model.addAttribute("pageSize", size);
        model.addAttribute("search", search);
        return "admin/movie-management";
    }

    @GetMapping("/ticket-management")
    public String showTickets(Model model) {
        return "admin/ticket-management";
    }

    @GetMapping("/promotion-management")
    public String showPromotions(Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PromotionDTO> promotions;
        try {
            promotions = promotionService.findAllPromotions(pageable);
            logger.info("Successfully fetched {} promotions, total pages={}", promotions.getTotalElements(),
                    promotions.getTotalPages());
        } catch (Exception e) {
            logger.error("Error fetching promotions: {}", e.getMessage(), e);
            promotions = Page.empty(); // Trả về trang rỗng nếu lỗi
        }
        // List<PromotionDTO> promotions = promotionService.findActivePromotions();
        // if (promotions == null || promotions.isEmpty()) {
        // promotions = new ArrayList<>();
        // }
        model.addAttribute("promotions", promotions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", promotions.getTotalPages() > 0 ? promotions.getTotalPages() : 1);
        model.addAttribute("pageSize", size);
        model.addAttribute("createPromotionDTO", new RequestPromotionDTO());
        return "admin/promotion-management";
    }

    @GetMapping("/combo-management")
    public String showCombos(Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) { // Mặc định 5 combo mỗi trang
        logger.info("Entering showCombos method for /admin/combo-management, page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<ComboDTO> comboPage;
        try {
            comboPage = comboService.findAllCombos(pageable); // Cần cập nhật ComboService
            logger.info("Successfully fetched {} combos, total pages={}", comboPage.getTotalElements(),
                    comboPage.getTotalPages());
        } catch (Exception e) {
            logger.error("Error fetching combos: {}", e.getMessage(), e);
            comboPage = Page.empty(); // Trả về trang rỗng nếu lỗi
        }
        model.addAttribute("combos", comboPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", comboPage.getTotalPages());
        model.addAttribute("pageSize", size);
        model.addAttribute("createComboDTO", new RequestComboDTO());
        return "admin/combo-management";
    }

    @PostMapping("/combo-management/add")
    public String addCombo(
            @Valid @ModelAttribute RequestComboDTO createComboDTO,
            BindingResult bindingResult,
            @RequestParam(value = "image", required = false) MultipartFile image,
            RedirectAttributes redirectAttributes,
            Model model) {
        logger.info("Received combo add request: {}", createComboDTO);
        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors: {}", bindingResult.getAllErrors());
            redirectAttributes.addFlashAttribute("errorMessage", "Please check the input data");
            model.addAttribute("createComboDTO", createComboDTO);
            return "admin/combo-management";
        }
        try {
            comboService.addCombo(createComboDTO, image);
            logger.info("Combo added successfully with name: {}", createComboDTO.getComboName());
            redirectAttributes.addFlashAttribute("successMessage", "Combo added successfully!");
        } catch (Exception e) {
            logger.error("Error adding combo: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding combo: " + e.getMessage());
            return "admin/combo-management";
        }
        return "redirect:/admin/combo-management?page=0&size=5";
    }

    @GetMapping("/combo-management/detail/{comboId}")
    public String getComboDetails(@PathVariable Integer comboId, Model model) {
        logger.info("Fetching combo details for ID: {}", comboId);
        try {
            ComboDTO combo = comboService.findComboById(comboId);
            model.addAttribute("combo", combo);
            return "admin/combo-detail";
        } catch (Exception e) {
            logger.error("Error fetching combo details for ID {}: {}", comboId, e.getMessage(), e);
            return "redirect:/admin/combo-management?page=0&size=5";
        }
    }

    @PostMapping("/combo-management/update")
    public String updateCombo(
            @ModelAttribute("combo") RequestComboDTO comboDTO,
            @RequestParam("comboId") Integer comboId,
            @RequestParam(value = "image", required = false) MultipartFile image,
            RedirectAttributes redirectAttributes) {
        try {
            comboService.updateCombo(comboId, comboDTO, image);
            redirectAttributes.addFlashAttribute("successMessage", "Combo updated successfully!");
        } catch (Exception e) {
            logger.error("Error updating combo: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating combo: " + e.getMessage());
        }
        return "redirect:/admin/combo-management/detail/" + comboId;
    }

    @PostMapping("/combo-management/toggle-status")
    public String toggleComboStatus(@RequestParam("comboId") Integer comboId,
            RedirectAttributes redirectAttributes) {
        try {
            comboService.toggleComboStatus(comboId);
            redirectAttributes.addFlashAttribute("successMessage", "Combo status updated successfully!");
        } catch (Exception e) {
            logger.error("Error toggling combo status: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating combo status");
        }
        return "redirect:/admin/combo-management/detail/" + comboId;
    }

    @PostMapping("/toggle-account-status")
    public String toggleAccountStatus(@RequestParam("accountId") String accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        adminService.toggleAccountStatus(accountId);
        String redirectUrl = "/admin/member-management?page=" + page + "&size=" + size;
        if (search != null && !search.trim().isEmpty()) {
            redirectUrl += "&search=" + search;
        }
        return "redirect:" + redirectUrl;
    }

    @PostMapping("/update-account")
    @ResponseBody
    public String updateAccount(@ModelAttribute UpdateAccountDTO updateAccountDTO,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            if (updateAccountDTO == null) {
                throw new IllegalArgumentException("UpdateAccountDTO is null");
            }
            if (image != null && !image.isEmpty()) {
                updateAccountDTO.setImage(image);
            } else if (updateAccountDTO.getImage() == null) {
                updateAccountDTO.setImage(null);
            }
            adminService.updateAccount(updateAccountDTO);
            logger.info("Account updated successfully with ID: {}", updateAccountDTO.getAccountId());
            return "success";
        } catch (IllegalArgumentException e) {
            logger.error("Validation error updating account: {}", e.getMessage(), e);
            return "error: Validation failed - " + e.getMessage();
        } catch (Exception e) {
            logger.error("Error updating account with ID {}: {}",
                    updateAccountDTO != null ? updateAccountDTO.getAccountId() : "null", e.getMessage(), e);
            return "error: Failed to update account - " + e.getMessage();
        }
    }

    @PostMapping("/promotion-management/add")
    public String addPromotion(
            @Valid @ModelAttribute RequestPromotionDTO createPromotionDTO,
            BindingResult bindingResult,
            @RequestParam(name = "image", required = false) MultipartFile promotionImage,
            RedirectAttributes redirectAttributes,
            Model model) {
        logger.info("Received promotion add request: {}", createPromotionDTO);
        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors: {}", bindingResult.getAllErrors());
            redirectAttributes.addFlashAttribute("errorMessage", "Please check the input data");
            model.addAttribute("createPromotionDTO", createPromotionDTO);
            return "admin/promotion-management";
        }
        try {
            promotionService.addPromotion(createPromotionDTO, promotionImage);
            logger.info("Promotion added successfully with code: {}", createPromotionDTO.getPromotionCode());
            redirectAttributes.addFlashAttribute("successMessage", "Promotion added successfully!");
        } catch (Exception e) {
            logger.error("Error adding promotion: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding promotion: " + e.getMessage());
            return "redirect:/admin/promotion-management";
        }
        return "redirect:/admin/promotion-management";
    }

    @GetMapping("/promotion-management/detail/{promotionId}")
    public String getPromotionDetails(@PathVariable String promotionId, Model model) {
        PromotionDTO promotion = promotionService.getPromotionById(promotionId);
        model.addAttribute("promotion", promotion);
        return "admin/promotion-detail";
    }

    @PostMapping("/promotion-management/update")
    public String updatePromotion(
            @ModelAttribute("promotion") RequestPromotionDTO promotionDTO,
            @RequestParam(name = "image", required = false) MultipartFile promotionImage,
            @RequestParam("promotionId") String promotionId,
            RedirectAttributes redirectAttributes) {
        try {
            promotionService.updatePromotion(promotionId, promotionDTO, promotionImage);
            redirectAttributes.addFlashAttribute("successMessage", "Promotion updated successfully!");
        } catch (Exception e) {
            logger.error("Error updating promotion: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating promotion: " + e.getMessage());
        }
        return "redirect:/admin/promotion-management/detail/" + promotionId;
    }

    @PostMapping("/promotion-management/toggle-status/")
    public String togglePromotionStatus(@RequestParam("promotionId") String promotionId,
            RedirectAttributes redirectAttributes) {
        try {
            promotionService.togglePromotionStatus(promotionId);
            redirectAttributes.addFlashAttribute("successMessage", "Promotion status updated successfully!");
        } catch (Exception e) {
            logger.error("Error toggling promotion status: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating promotion status");
        }
        return "redirect:/admin/promotion-management/detail/" + promotionId;
    }
}