package com.example.j2ee_project.service.log;

import com.example.j2ee_project.model.dto.LogDTO;
import com.example.j2ee_project.model.request.log.LogRequest;
import org.springframework.data.domain.Page;

public interface LogServiceInterface {
    LogDTO createLog(LogRequest request);

    Page<LogDTO> getAllLogs(int offset, int limit, String search);

    LogDTO getLogById(Integer logId);

    LogDTO updateLog(Integer logId, LogRequest request);

    void deleteLog(Integer logId);
}