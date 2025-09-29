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
        Status availableStatus = statusRepository.findByStatusName(EStatus.AVAILABLE.getName());
        if (availableStatus == null) {
            throw new ResourceNotFoundException("Không tìm thấy status Available");
        }

        Status occupiedStatus = statusRepository.findByStatusName(EStatus.OCCUPIED.getName());
        if (occupiedStatus == null) {
            throw new ResourceNotFoundException("Không tìm thấy status Occupied");
        }

        Status pendingStatus = statusRepository.findByStatusName(EStatus.PENDING.getName());
        if (pendingStatus == null) {
            throw new ResourceNotFoundException("Không tìm thấy status Pending");
        }

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
        if (bookingRequest.getNumberOfGuests() > table.getTableType().getCapacity()) {
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

                System.out.println("Creating BookingDetail for bookingID: " + booking.getBookingID() + ", mealID: " + detailRequest.getMealID());
                // Thêm booking detail với bookingID thực
                addBookingDetail(booking.getBookingID(), detailRequest);
            }
        }

        return mapToBookingDTO(booking);
    }

    @Override
    public void cancelBooking(Integer bookingID) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'cancelBooking'");
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

    @Transactional
    public BookingDTO updateBookingMeals(Integer bookingID, List<BookingDetailRequest> mealRequests) {
        // Tìm booking
        Booking booking = bookingRepository.findById(bookingID)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy booking với ID: " + bookingID));

        // CÓ THỂ CẬP NHẬT MÓN CHO CẢ PENDING VÀ CONFIRMED BOOKING
        if (mealRequests != null && !mealRequests.isEmpty()) {
            for (BookingDetailRequest detailRequest : mealRequests) {
                Meal meal = mealRepository.findById(detailRequest.getMealID())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Không tìm thấy món ăn với ID: " + detailRequest.getMealID()));

                if (!meal.getStatus().getStatusName().equals(EStatus.ACTIVE.getName())) {
                    throw new IllegalStateException("Món ăn " + meal.getMealName() + " không khả dụng");
                }

                if (detailRequest.getQuantity() <= 0) {
                    throw new IllegalArgumentException("Số lượng món ăn phải lớn hơn 0");
                }

                addBookingDetail(bookingID, detailRequest);
            }
        }

        // Cập nhật thời gian
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);

        return mapToBookingDTO(booking);
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

    @Override
    public List<BookingDetailDTO> getBookingDetails(Integer bookingID) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBookingDetails'");
    }

    // @Override
    // @Transactional
    // public BookingDetailDTO addBookingDetail(Integer bookingID,
    // BookingDetailRequest request) {
    // Booking booking = bookingRepository.findById(bookingID)
    // .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đặt bàn với
    // ID: " + bookingID));

    // Meal meal = mealRepository.findById(request.getMealID())
    // .orElseThrow(
    // () -> new ResourceNotFoundException("Không tìm thấy món ăn với ID: " +
    // request.getMealID()));
    // if (!"Available".equalsIgnoreCase(meal.getStatus())) {
    // throw new IllegalStateException("Món ăn không sẵn sàng");
    // }

    // BookingDetail detail = new BookingDetail();
    // detail.setBooking(booking);
    // detail.setMeal(meal);
    // detail.setQuantity(request.getQuantity());
    // detail.setCreatedAt(LocalDateTime.now());
    // detail.setUpdatedAt(LocalDateTime.now());

    // detail = bookingDetailRepository.save(detail);
    // return mapToBookingDetailDTO(detail);
    // }

    // @Override
    // @Transactional
    // public BookingDetailDTO updateBookingDetail(Integer detailID,
    // UpdateBookingDetailRequest request) {
    // BookingDetail detail = bookingDetailRepository.findById(detailID)
    // .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi tiết món
    // với ID: " + detailID));

    // if (request.getQuantity() <= 0) {
    // bookingDetailRepository.delete(detail);
    // return null;
    // } else {
    // detail.setQuantity(request.getQuantity());
    // detail.setUpdatedAt(LocalDateTime.now());
    // detail = bookingDetailRepository.save(detail);
    // return mapToBookingDetailDTO(detail);
    // }
    // }

    // @Override
    // @Transactional(readOnly = true)
    // public List<BookingDetailDTO> getBookingDetails(Integer bookingID) {
    // List<BookingDetail> details =
    // bookingDetailRepository.findByBookingBookingID(bookingID);
    // return details.stream()
    // .map(this::mapToBookingDetailDTO)
    // .collect(Collectors.toList());
    // }

    // @Override
    // @Transactional
    // public void cancelBooking(Integer bookingID) {
    // Booking booking = bookingRepository.findById(bookingID)
    // .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đặt bàn với
    // ID: " + bookingID));

    // booking.setStatus("Cancelled");
    // booking.setUpdatedAt(LocalDateTime.now());
    // bookingRepository.save(booking);

    // RestaurantTable table = booking.getRestaurantTable();
    // table.setStatus("Available");
    // table.setUpdatedAt(LocalDateTime.now());
    // restaurantTableRepository.save(table);

    // bookingDetailRepository.deleteByBookingBookingID(bookingID);
    // }

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

    @Override
    public BookingDetailDTO updateBookingDetail(Integer detailID, BookingDetailRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateBookingDetail'");
    }

    // private BookingDetailDTO mapToBookingDetailDTO(BookingDetail detail) {
    // return BookingDetailDTO.builder()
    // .bookingDetailID(detail.getBookingDetailID())
    // .bookingID(detail.getBooking().getBookingID())
    // .mealID(detail.getMeal().getMealID())
    // .mealName(detail.getMeal().getMealName())
    // .quantity(detail.getQuantity())
    // .createdAt(detail.getCreatedAt())
    // .updatedAt(detail.getUpdatedAt())
    // .build();
    // }

    // @Override
    // public BookingDetailDTO updateBookingDetail(Integer detailID,
    // BookingDetailRequest request) {
    // // TODO Auto-generated method stub
    // throw new UnsupportedOperationException("Unimplemented method
    // 'updateBookingDetail'");
    // }

}