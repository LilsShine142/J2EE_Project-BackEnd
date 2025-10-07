package com.example.j2ee_project.service.booking;

import com.example.j2ee_project.entity.*;
import com.example.j2ee_project.entity.keys.KeyBookingDetailId;
import com.example.j2ee_project.exception.ResourceNotFoundException;
import com.example.j2ee_project.model.dto.BookingDTO;
import com.example.j2ee_project.model.dto.BookingDetailDTO;
import com.example.j2ee_project.model.request.booking.BookingDetailRequest;
import com.example.j2ee_project.model.request.booking.BookingRequestDTO;
import com.example.j2ee_project.repository.BookingDetailRepository;
import com.example.j2ee_project.repository.BookingRepository;
import com.example.j2ee_project.repository.MealRepository;
import com.example.j2ee_project.repository.RestaurantTableRepository;
import com.example.j2ee_project.repository.StatusRepository;
import com.example.j2ee_project.repository.UserRepository;
import com.example.j2ee_project.utils._enum.EStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService implements BookingServiceInterface {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final RestaurantTableRepository restaurantTableRepository;
    private final MealRepository mealRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final StatusRepository statusRepository;

    private List<BookingDetail> addBookingDetails;

    @Override
    @Transactional
    public BookingDTO createBooking(BookingRequestDTO bookingRequestDTO) {
        // Validate user
        User user = userRepository.findById(bookingRequestDTO.getUserID())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + bookingRequestDTO.getUserID()));

        // Validate table
        RestaurantTable table = restaurantTableRepository.findById(bookingRequestDTO.getTableID())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bàn với ID: " + bookingRequestDTO.getTableID()));

        // Check table availability
        List<String> excludedStatuses = Arrays.asList(EStatus.CANCELLED.getName(), EStatus.COMPLETED.getName());
        if (bookingRepository.existsOverlappingBooking(
                bookingRequestDTO.getTableID(),
                bookingRequestDTO.getStartTime(),
                bookingRequestDTO.getEndTime(),
                excludedStatuses)) {
            throw new IllegalStateException("Bàn đã được đặt trong khung giờ này");
        }

        // Check table status
        if (!table.getStatus().getStatusName().equals(EStatus.AVAILABLE.getName())) {
            throw new IllegalStateException("Bàn không sẵn sàng để đặt");
        }

        // Check table capacity
        if (bookingRequestDTO.getNumberOfGuests() > table.getTableType().getNumberOfGuests()) {
            throw new IllegalStateException("Bàn không đủ sức chứa");
        }

        // Validate status (default to PENDING)
        Status status = statusRepository.findByStatusName(EStatus.PENDING.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trạng thái PENDING"));

        // Create booking
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setRestaurantTable(table);
        booking.setBookingDate(bookingRequestDTO.getBookingDate());
        booking.setStartTime(bookingRequestDTO.getStartTime());
        booking.setEndTime(bookingRequestDTO.getEndTime());
        booking.setNotes(bookingRequestDTO.getNotes());
        booking.setNumberOfGuests(bookingRequestDTO.getNumberOfGuests());
        booking.setInitialPayment(bookingRequestDTO.getInitialPayment() != null ? bookingRequestDTO.getInitialPayment() : BigDecimal.ZERO);
        booking.setPaymentMethod(bookingRequestDTO.getPaymentMethod());
        booking.setPaymentTime(bookingRequestDTO.getPaymentTime());
        booking.setStatus(status);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());

        // Save booking
        Booking savedBooking = bookingRepository.save(booking);


        // Handle booking details (meals) if provided
        if (bookingRequestDTO.getMeals() != null && !bookingRequestDTO.getMeals().isEmpty()) {
            addBookingDetails = new ArrayList<>();
            BookingDetail savedDetail;
            for (BookingDetailRequest detailRequest : bookingRequestDTO.getMeals()) {
                Meal meal = mealRepository.findById(detailRequest.getMealID())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy món ăn với ID: " + detailRequest.getMealID()));

                BookingDetail detail = BookingDetail.builder()
                        .id(new KeyBookingDetailId(savedBooking.getBookingID(), detailRequest.getMealID()))
                        .booking(savedBooking)
                        .meal(meal)
                        .quantity(detailRequest.getQuantity())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                savedDetail = bookingDetailRepository.save(detail);
                addBookingDetails.add(savedDetail);
            }
        }
        savedBooking.setBookingDetails(addBookingDetails);
        return mapToBookingDTO(savedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingDTO> getAllBookings(int offset, int limit, String search, Integer statusId, Integer userId, Integer tableId) {
        if (offset < 0) offset = 0;
        if (limit <= 0) limit = 10;
        if (limit > 100) limit = 100;

        if (search == null) search = "";

        Pageable pageable = PageRequest.of(offset / limit, limit);

        Page<Booking> bookingPage = bookingRepository.findByFilters(search, statusId, userId, tableId, pageable);

        return bookingPage.map(this::mapToBookingDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDTO getBookingById(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy booking với ID: " + bookingId));
        return mapToBookingDTO(booking);
    }

    @Override
    @Transactional
    public BookingDTO updateBooking(Integer bookingId, BookingRequestDTO bookingRequestDTO) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy booking với ID: " + bookingId));

        // Check if update is allowed (within 2 hours from createdAt)
        LocalDateTime now = LocalDateTime.now();
        if (booking.getCreatedAt().plusHours(2).isBefore(now)) {
            throw new IllegalStateException("Không thể sửa đổi booking sau 2 giờ kể từ khi tạo");
        }

        // Validate table if provided
        if (bookingRequestDTO.getTableID() != null) {
            RestaurantTable table = restaurantTableRepository.findById(bookingRequestDTO.getTableID())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bàn với ID: " + bookingRequestDTO.getTableID()));

            // Check table availability
            List<String> excludedStatuses = Arrays.asList(EStatus.CANCELLED.getName(), EStatus.COMPLETED.getName());
            if (bookingRepository.existsOverlappingBooking(
                    bookingRequestDTO.getTableID(),
                    bookingRequestDTO.getStartTime() != null ? bookingRequestDTO.getStartTime() : booking.getStartTime(),
                    bookingRequestDTO.getEndTime() != null ? bookingRequestDTO.getEndTime() : booking.getEndTime(),
                    excludedStatuses)) {
                throw new IllegalStateException("Bàn đã được đặt trong khung giờ này");
            }

            // Check table status
            if (!table.getStatus().getStatusName().equals(EStatus.AVAILABLE.getName())) {
                throw new IllegalStateException("Bàn không sẵn sàng để đặt");
            }

            // Check table capacity
            if (bookingRequestDTO.getNumberOfGuests() != null &&
                    bookingRequestDTO.getNumberOfGuests() > table.getTableType().getNumberOfGuests()) {
                throw new IllegalStateException("Bàn không đủ sức chứa");
            }

            booking.setRestaurantTable(table);
        }

        // Update user if provided
        if (bookingRequestDTO.getUserID() != null) {
            User user = userRepository.findById(bookingRequestDTO.getUserID())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + bookingRequestDTO.getUserID()));
            booking.setUser(user);
        }

        // Update other fields if provided
        if (bookingRequestDTO.getBookingDate() != null) {
            booking.setBookingDate(bookingRequestDTO.getBookingDate());
        }
        if (bookingRequestDTO.getStartTime() != null) {
            booking.setStartTime(bookingRequestDTO.getStartTime());
        }
        if (bookingRequestDTO.getEndTime() != null) {
            booking.setEndTime(bookingRequestDTO.getEndTime());
        }
        if (bookingRequestDTO.getNotes() != null) {
            booking.setNotes(bookingRequestDTO.getNotes());
        }
        if (bookingRequestDTO.getNumberOfGuests() != null) {
            booking.setNumberOfGuests(bookingRequestDTO.getNumberOfGuests());
        }
        if (bookingRequestDTO.getInitialPayment() != null) {
            booking.setInitialPayment(bookingRequestDTO.getInitialPayment());
        }
        if (bookingRequestDTO.getPaymentMethod() != null) {
            booking.setPaymentMethod(bookingRequestDTO.getPaymentMethod());
        }
        if (bookingRequestDTO.getPaymentTime() != null) {
            booking.setPaymentTime(bookingRequestDTO.getPaymentTime());
        }

        // Update booking details (meals) if provided
        if (bookingRequestDTO.getMeals() != null) {
            // Remove existing booking details
            bookingDetailRepository.deleteByBookingId(bookingId);

            // Add new booking details
            for (BookingDetailRequest detailRequest : bookingRequestDTO.getMeals()) {
                Meal meal = mealRepository.findById(detailRequest.getMealID())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy món ăn với ID: " + detailRequest.getMealID()));

                BookingDetail detail = BookingDetail.builder()
                        .id(new KeyBookingDetailId(bookingId, detailRequest.getMealID()))
                        .booking(booking)
                        .meal(meal)
                        .quantity(detailRequest.getQuantity())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                bookingDetailRepository.save(detail);
            }
        }

        booking.setUpdatedAt(LocalDateTime.now());
        Booking updatedBooking = bookingRepository.save(booking);
        return mapToBookingDTO(updatedBooking);
    }

    @Override
    @Transactional
    public void deleteBooking(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy booking với ID: " + bookingId));

        // Check if cancellation is allowed (within 2 hours from createdAt)
        LocalDateTime now = LocalDateTime.now();
        if (booking.getCreatedAt().plusHours(2).isBefore(now)) {
            throw new IllegalStateException("Không thể hủy booking sau 2 giờ kể từ khi tạo");
        }

        // Set status to CANCELLED instead of deleting
        Status cancelledStatus = statusRepository.findByStatusName(EStatus.CANCELLED.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trạng thái CANCELLED"));
        booking.setStatus(cancelledStatus);
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);
    }

    private BookingDTO mapToBookingDTO(Booking booking) {
        List<BookingDetailDTO> detailDTOs = booking.getBookingDetails() != null
                ? booking.getBookingDetails().stream()
                .map(detail ->BookingDetailDTO.builder()
                        .bookingID(detail.getBooking().getBookingID())
                        .mealID(detail.getMeal().getMealID())
                        .quantity(detail.getQuantity())
                        .createdAt(detail.getCreatedAt())
                        .updatedAt(detail.getUpdatedAt())
                        .build())
                .collect(Collectors.toList())
                : null;

        return BookingDTO.builder()
                .bookingID(booking.getBookingID())
                .userID(booking.getUser().getUserID())
                .tableID(booking.getRestaurantTable().getTableID())
                .tableName(booking.getRestaurantTable().getTableName())
                .bookingDate(booking.getBookingDate())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .notes(booking.getNotes())
                .numberOfGuests(booking.getNumberOfGuests())
                .initialPayment(booking.getInitialPayment())
                .paymentMethod(booking.getPaymentMethod())
                .paymentTime(booking.getPaymentTime())
                .statusId(booking.getStatus().getStatusID())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .bookingDetails(detailDTOs)
                .build();
    }
}