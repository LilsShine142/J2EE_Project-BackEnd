package com.example.j2ee_project.service.status;

import com.example.j2ee_project.model.dto.StatusDTO;
import com.example.j2ee_project.model.request.status.StatusRequest;
import org.springframework.data.domain.Page;

public interface StatusServiceInterface {
    StatusDTO createStatus(StatusRequest request);

    Page<StatusDTO> getAllStatuses(int offset, int limit, String search);

    StatusDTO getStatusById(Integer statusId);

    StatusDTO updateStatus(Integer statusId, StatusRequest request);

    void deleteStatus(Integer statusId);
}