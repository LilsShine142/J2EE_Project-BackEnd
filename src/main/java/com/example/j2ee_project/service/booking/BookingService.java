package com.example.j2ee_project.service.booking;

import com.example.j2ee_project.entity.Booking;
import com.example.j2ee_project.entity.BookingDetail;
import com.example.j2ee_project.entity.Meal;
import com.example.j2ee_project.entity.RestaurantTable;
import com.example.j2ee_project.entity.Status;
import com.example.j2ee_project.entity.User;
import com.example.j2ee_project.entity.keys.KeyBookingDetailId;
import com.example.j2ee_project.exception.ResourceNotFoundException;
import com.example.j2ee_project.model.dto.BookingDTO;
import com.example.j2ee_project.model.dto.BookingDetailDTO;
import com.example.j2ee_project.model.dto.UserDTO;
import com.example.j2ee_project.model.request.booking.BookingDetailRequest;
import com.example.j2ee_project.model.request.booking.BookingRequestDTO;
import com.example.j2ee_project.repository.BookingDetailRepository;
import com.example.j2ee_project.repository.BookingRepository;
import com.example.j2ee_project.repository.MealRepository;
import com.example.j2ee_project.repository.RestaurantTableRepository;
import com.example.j2ee_project.repository.StatusRepository;
import com.example.j2ee_project.repository.UserRepository;
import com.example.j2ee_project.utils._enum.EStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookingService implements BookingServiceInterface {
    private final RestaurantTableRepository restaurantTableRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final StatusRepository statusRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final MealRepository mealRepository;

    @Autowired
    public BookingService(RestaurantTableRepository restaurantTableRepository,
            UserRepository userRepository,
            BookingRepository bookingRepository,
            StatusRepository statusRepository,
            BookingDetailRepository bookingDetailRepository,
            MealRepository mealRepository) {
        this.restaurantTableRepository = restaurantTableRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.statusRepository = statusRepository;
        this.bookingDetailRepository = bookingDetailRepository;
        this.mealRepository = mealRepository;
    }

    @Override
    @Transactional
    public BookingDTO createBooking(BookingRequestDTO bookingRequest) {
        // Lấy status từ StatusRepository
        Status availableStatus = statusRepository.findByStatusName(EStatus.AVAILABLE.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy status Available"));

        Status occupiedStatus = statusRepository.findByStatusName(EStatus.OCCUPIED.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy status Occupied"));

        Status pendingStatus = statusRepository.findByStatusName(EStatus.PENDING.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy status Pending"));

        // Validate user
        User user = userRepository.findById(bookingRequest.getUserID())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy người dùng với ID: " + bookingRequest.getUserID()));

        // Validate table
        RestaurantTable table = restaurantTableRepository.findById(bookingRequest.getTableID())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy bàn với ID: " + bookingRequest.getTableID()));

        // Kiểm tra bàn trống
        List<String> excludedStatuses = Arrays.asList(
                EStatus.CANCELLED.getName(),
                EStatus.COMPLETED.getName());
        if (bookingRepository.existsOverlappingBooking(bookingRequest.getTableID(), bookingRequest.getStartTime(),
                bookingRequest.getEndTime(), excludedStatuses)) {
            throw new IllegalStateException("Bàn đã được đặt trong khung giờ này");
        }

        // Kiểm tra status bàn
        if (!table.getStatus().getStatusName().equals(EStatus.AVAILABLE.getName())) {
            throw new IllegalStateException("Bàn không sẵn sàng để đặt");
        }

        // Kiểm tra sức chứa
        if (bookingRequest.getNumberOfGuests() > table.getTableType().getNumberOfGuests()) {
            throw new IllegalStateException("Bàn không đủ sức chứa");
        }

        // Cập nhật bàn
        table.setStatus(occupiedStatus);
        table.setUpdatedAt(LocalDateTime.now());
        restaurantTableRepository.save(table);

        // Tạo Booking
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setRestaurantTable(table);
        booking.setBookingDate(
                bookingRequest.getBookingDate() != null ? bookingRequest.getBookingDate() : LocalDateTime.now());
        booking.setStartTime(bookingRequest.getStartTime());
        booking.setEndTime(bookingRequest.getEndTime());
        booking.setStatus(pendingStatus);
        booking.setNumberOfGuests(bookingRequest.getNumberOfGuests());
        booking.setNotes(bookingRequest.getNotes());
        booking.setInitialPayment(bookingRequest.getInitialPayment() != null ? bookingRequest.getInitialPayment()
                : java.math.BigDecimal.ZERO);
        booking.setPaymentMethod(bookingRequest.getPaymentMethod());
        booking.setPaymentTime(bookingRequest.getPaymentTime());
        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());

        // Lưu Booking
        booking = bookingRepository.save(booking);
        if (booking.getBookingID() == null) {
            throw new IllegalStateException("Không thể sinh bookingID");
        }

        // Thêm booking detail khi có món
        if (bookingRequest.getMeals() != null && !bookingRequest.getMeals().isEmpty()) {
            for (BookingDetailRequest detailRequest : bookingRequest.getMeals()) {
                // Validate meal
                Meal meal = mealRepository.findById(detailRequest.getMealID())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Không tìm thấy món ăn với ID: " + detailRequest.getMealID()));

                if (!meal.getStatus().getStatusName().equals(EStatus.ACTIVE.getName())) {
                    throw new IllegalStateException("Món ăn " + meal.getMealName() + " không khả dụng");
                }

                if (detailRequest.getQuantity() <= 0) {
                    throw new IllegalArgumentException("Số lượng món ăn phải lớn hơn 0");
                }

                System.out.println("Creating BookingDetail for bookingID: " + booking.getBookingID() + ", mealID: "
                        + detailRequest.getMealID());
                // Thêm booking detail với bookingID thực
                addBookingDetail(booking.getBookingID(), detailRequest);
            }
        }

        return mapToBookingDTO(booking);
    }

    @Override
    @Transactional
    public BookingDetailDTO addBookingDetail(Integer bookingID, BookingDetailRequest request) {
        // Tìm booking
        Booking booking = bookingRepository.findById(bookingID)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingID));

        // Tìm meal
        Meal meal = mealRepository.findById(request.getMealID())
                .orElseThrow(() -> new ResourceNotFoundException("Meal not found with ID: " + request.getMealID()));

        // Kiểm tra trạng thái món ăn
        if (!meal.getStatus().getStatusName().equals(EStatus.ACTIVE.getName())) {
            throw new IllegalStateException("Món ăn " + meal.getMealName() + " không khả dụng");
        }

        // Kiểm tra quantity
        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Số lượng món ăn phải lớn hơn 0");
        }

        // Kiểm tra xem booking detail đã tồn tại chưa
        Optional<BookingDetail> existingDetail = bookingDetailRepository
                .findByBookingBookingIDAndMealMealID(bookingID, request.getMealID());

        BookingDetail bookingDetail;

        if (existingDetail.isPresent()) {
            // Nếu đã tồn tại, update quantity
            bookingDetail = existingDetail.get();
            bookingDetail.setQuantity(bookingDetail.getQuantity() + request.getQuantity());
            bookingDetail.setUpdatedAt(LocalDateTime.now());
        } else {
            // Nếu chưa tồn tại, tạo mới với builder để đảm bảo đầy đủ các trường
            bookingDetail = BookingDetail.builder()
                    .id(new KeyBookingDetailId(booking.getBookingID(), meal.getMealID()))
                    .booking(booking)
                    .meal(meal)
                    .quantity(request.getQuantity())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
        }

        // Lưu booking detail
        BookingDetail savedDetail = bookingDetailRepository.save(bookingDetail);
        System.err.println("Saved Detail: " + savedDetail);

        // Convert to DTO và return
        return convertToDTO(savedDetail);
    }

    // Cập nhật booking
    @Transactional
    public BookingDTO updateBooking(Integer bookingID, BookingDTO bookingDTO) {
        Booking booking = bookingRepository.findById(bookingID)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking"));

        // Kiểm tra nếu thay đổi thời gian hoặc bàn
        if (!booking.getRestaurantTable().getTableID().equals(bookingDTO.getTableID()) ||
                !booking.getStartTime().equals(bookingDTO.getStartTime()) ||
                !booking.getEndTime().equals(bookingDTO.getEndTime())) {

            if (!isTableAvailable(bookingDTO.getTableID(), bookingDTO.getStartTime(), bookingDTO.getEndTime())) {
                throw new RuntimeException("Bàn đã được đặt trong khung giờ này");
            }

            booking.setRestaurantTable(restaurantTableRepository.findById(bookingDTO.getTableID())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn")));
        }

        booking.setStartTime(bookingDTO.getStartTime());
        booking.setEndTime(bookingDTO.getEndTime());
        booking.setNotes(bookingDTO.getNotes());
        booking.setNumberOfGuests(bookingDTO.getNumberOfGuests());
        booking.setUpdatedAt(LocalDateTime.now());

        Booking updatedBooking = bookingRepository.save(booking);
        return convertToDTO(updatedBooking);
    }

    // Hủy booking
    @Transactional
    public void cancelBooking(Integer bookingID) {
        Booking booking = bookingRepository.findById(bookingID)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking"));

        // Set status = Cancelled (statusID = 4)
        booking.setStatus(statusRepository.findById(4)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy status")));
        booking.setUpdatedAt(LocalDateTime.now());

        bookingRepository.save(booking);
    }

    // Xác nhận booking
    @Transactional
    public BookingDTO confirmBooking(Integer bookingID) {
        Booking booking = bookingRepository.findById(bookingID)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking"));

        // Set status = Confirmed (statusID = 2)
        booking.setStatus(statusRepository.findById(2)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy status")));
        booking.setUpdatedAt(LocalDateTime.now());

        Booking confirmedBooking = bookingRepository.save(booking);
        return convertToDTO(confirmedBooking);
    }

    // Hoàn thành booking
    @Transactional
    public BookingDTO completeBooking(Integer bookingID) {
        Booking booking = bookingRepository.findById(bookingID)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking"));

        // Set status = Completed (statusID = 5)
        booking.setStatus(statusRepository.findById(5)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy status")));
        booking.setUpdatedAt(LocalDateTime.now());

        Booking completedBooking = bookingRepository.save(booking);
        return convertToDTO(completedBooking);
    }

    // Kiểm tra xem bàn có sẵn trong khoảng thời gian không
    private boolean isTableAvailable(Integer tableID, LocalDateTime startTime, LocalDateTime endTime) {
        List<String> excludedStatuses = Arrays.asList(
                EStatus.CANCELLED.getName(),
                EStatus.COMPLETED.getName());
        return !bookingRepository.existsOverlappingBooking(tableID, startTime, endTime, excludedStatuses);
    }

    private BookingDetailDTO convertToDTO(BookingDetail bookingDetail) {
        return BookingDetailDTO.builder()
                .id(new KeyBookingDetailId(bookingDetail.getBooking().getBookingID(),
                        bookingDetail.getMeal().getMealID()))
                .bookingID(bookingDetail.getBooking().getBookingID())
                .mealID(bookingDetail.getMeal().getMealID())
                // .mealName(bookingDetail.getMeal().getMealName())
                // .price(bookingDetail.getMeal().getPrice())
                .quantity(bookingDetail.getQuantity())
                // .subTotal(bookingDetail.getMeal().getPrice().multiply(BigDecimal.valueOf(bookingDetail.getQuantity())))
                .build();
    }

    // Convert Entity sang DTO (Booking -> BookingDTO)
    private BookingDTO convertToDTO(Booking booking) {
        return BookingDTO.builder()
                .bookingID(booking.getBookingID())
                .userID(booking.getUser().getUserID())
                .userName(booking.getUser().getFullName())
                .tableID(booking.getRestaurantTable().getTableID())
                .tableName(booking.getRestaurantTable().getTableName())
                .bookingDate(booking.getBookingDate())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .status(booking.getStatus().getStatusName())
                .notes(booking.getNotes())
                .numberOfGuests(booking.getNumberOfGuests())
                .initialPayment(booking.getInitialPayment())
                .paymentMethod(booking.getPaymentMethod())
                .paymentTime(booking.getPaymentTime())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }

    private BookingDTO mapToBookingDTO(Booking booking) {
        BookingDTO.BookingDTOBuilder builder = BookingDTO.builder()
                .bookingID(booking.getBookingID())
                .userID(booking.getUser().getUserID())
                .tableID(booking.getRestaurantTable().getTableID())
                .bookingDate(booking.getBookingDate())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .status(booking.getStatus().getStatusName())
                .notes(booking.getNotes())
                .initialPayment(booking.getInitialPayment())
                .paymentMethod(booking.getPaymentMethod())
                .paymentTime(booking.getPaymentTime())
                .numberOfGuests(booking.getNumberOfGuests())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt());
        return builder.build();
    }

    private BookingDetailDTO convertDetailToDTO(BookingDetail detail) {
        return BookingDetailDTO.builder()
                .id(detail.getId())
                .bookingID(detail.getBooking().getBookingID())
                .mealID(detail.getMeal().getMealID())
                .quantity(detail.getQuantity())
                .createdAt(detail.getCreatedAt())
                .updatedAt(detail.getUpdatedAt())
                .build();
    }

    @Override
    public BookingDetailDTO updateBookingDetail(Integer detailID, BookingDetailRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateBookingDetail'");
    }

    // Lấy tất cả bookings
    public List<BookingDTO> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Lấy booking theo ID
    public BookingDTO getBookingById(Integer bookingID) {
        Booking booking = bookingRepository.findById(bookingID)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking"));
        return convertToDTO(booking);
    }

    // Lấy bookings theo user
    public List<BookingDTO> getBookingsByUser(Integer userID) {
        return bookingRepository.findByUser_UserID(userID).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Lấy bookings sắp tới của user
    public List<BookingDTO> getUpcomingBookingsByUser(Integer userID) {
        return bookingRepository.findUpcomingBookingsByUser(userID, LocalDateTime.now()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Lấy lịch sử bookings của user
    public List<BookingDTO> getBookingHistoryByUser(Integer userID) {
        return bookingRepository.findBookingHistoryByUser(userID, LocalDateTime.now()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Lấy bookings theo ngày
    public List<BookingDTO> getBookingsByDate(LocalDateTime date) {
        return bookingRepository.findByBookingDate(date).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Lấy bookings theo table
    public List<BookingDTO> getBookingsByTable(Integer tableID) {
        return bookingRepository.findByRestaurantTable_TableID(tableID).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Lấy bookings theo status
    public List<BookingDTO> getBookingsByStatus(Integer statusID) {
        return bookingRepository.findByStatus_StatusID(statusID).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDetailDTO> getBookingDetails(Integer bookingID) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBookingDetails'");
    }

    // Lấy chi tiết booking (món ăn đã đặt)
    // public List<BookingDetailDTO> getBookingDetails(Integer bookingID) {
    // return bookingDetailRepository.findById_BookingID(bookingID).stream()
    // .map(this::convertDetailToDTO)
    // .collect(Collectors.toList());
    // }
}