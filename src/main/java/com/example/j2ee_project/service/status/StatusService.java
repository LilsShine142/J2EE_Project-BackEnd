package com.example.j2ee_project.service.status;

import com.example.j2ee_project.entity.Status;
import com.example.j2ee_project.exception.ResourceNotFoundException;
import com.example.j2ee_project.model.dto.StatusDTO;
import com.example.j2ee_project.model.request.status.StatusRequest;
import com.example.j2ee_project.repository.StatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StatusService implements StatusServiceInterface {

    private final StatusRepository statusRepository;

    @Override
    @Transactional
    public StatusDTO createStatus(StatusRequest request) {
        Status status = new Status();
        status.setStatusName(request.getStatusName());
        status.setDescription(request.getDescription());
        status.setCode(request.getCode());
        status.setCreatedAt(LocalDateTime.now());
        status.setUpdatedAt(LocalDateTime.now());

        Status saved = statusRepository.save(status);
        return mapToStatusDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StatusDTO> getAllStatuses(int offset, int limit, String search) {
        if (offset < 0) offset = 0;
        if (limit <= 0) limit = 10;
        if (limit > 100) limit = 100;
        if (search == null) search = "";

        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<Status> page = statusRepository.findByStatusNameContainingIgnoreCase(search, pageable);
        return page.map(this::mapToStatusDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public StatusDTO getStatusById(Integer statusId) {
        Status status = statusRepository.findById(statusId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trạng thái với ID: " + statusId));
        return mapToStatusDTO(status);
    }

    @Override
    @Transactional
    public StatusDTO updateStatus(Integer statusId, StatusRequest request) {
        Status status = statusRepository.findById(statusId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trạng thái với ID: " + statusId));

        status.setStatusName(request.getStatusName());
        status.setDescription(request.getDescription());
        status.setCode(request.getCode());
        status.setUpdatedAt(LocalDateTime.now());

        Status updated = statusRepository.save(status);
        return mapToStatusDTO(updated);
    }

    @Override
    @Transactional
    public void deleteStatus(Integer statusId) {
        Status status = statusRepository.findById(statusId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trạng thái với ID: " + statusId));
        statusRepository.delete(status);
    }

    private StatusDTO mapToStatusDTO(Status status) {
        StatusDTO dto = new StatusDTO();
        dto.setStatusID(status.getStatusID());
        dto.setStatusName(status.getStatusName());
        dto.setDescription(status.getDescription());
        dto.setCode(status.getCode());
        dto.setCreatedAt(status.getCreatedAt());
        dto.setUpdatedAt(status.getUpdatedAt());
        return dto;
    }
}