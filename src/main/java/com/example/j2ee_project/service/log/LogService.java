package com.example.j2ee_project.service.log;

import com.example.j2ee_project.entity.Log;
import com.example.j2ee_project.entity.User;
import com.example.j2ee_project.exception.ResourceNotFoundException;
import com.example.j2ee_project.model.dto.LogDTO;
import com.example.j2ee_project.model.request.log.LogRequest;
import com.example.j2ee_project.repository.LogRepository;
import com.example.j2ee_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LogService implements LogServiceInterface {

    private final LogRepository logRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public LogDTO createLog(LogRequest request) {
        User user = null;
        if (request.getUserID() != null) {
            user = userRepository.findById(request.getUserID())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + request.getUserID()));
        }

        Log log = new Log();
        log.setTableName(request.getTableName());
        log.setRecordID(request.getRecordID());
        log.setAction(request.getAction());
        log.setChangeDetails(request.getChangeDetails());
        log.setChangeTime(LocalDateTime.now());
        log.setUser(user);

        Log saved = logRepository.save(log);
        return mapToLogDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LogDTO> getAllLogs(int offset, int limit, String search) {
        if (offset < 0) offset = 0;
        if (limit <= 0) limit = 10;
        if (limit > 100) limit = 100;
        if (search == null) search = "";

        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<Log> page = logRepository.findByTableNameContainingIgnoreCaseOrActionContainingIgnoreCase(search, search, pageable);
        return page.map(this::mapToLogDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public LogDTO getLogById(Integer logId) {
        Log log = logRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy log với ID: " + logId));
        return mapToLogDTO(log);
    }

    @Override
    @Transactional
    public LogDTO updateLog(Integer logId, LogRequest request) {
        Log log = logRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy log với ID: " + logId));

        User user = null;
        if (request.getUserID() != null) {
            user = userRepository.findById(request.getUserID())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + request.getUserID()));
        }

        log.setTableName(request.getTableName());
        log.setRecordID(request.getRecordID());
        log.setAction(request.getAction());
        log.setChangeDetails(request.getChangeDetails());
        log.setChangeTime(LocalDateTime.now());
        log.setUser(user);

        Log updated = logRepository.save(log);
        return mapToLogDTO(updated);
    }

    @Override
    @Transactional
    public void deleteLog(Integer logId) {
        Log log = logRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy log với ID: " + logId));
        logRepository.delete(log);
    }

    private LogDTO mapToLogDTO(Log log) {
        LogDTO dto = new LogDTO();
        dto.setLogID(log.getLogID());
        dto.setTableName(log.getTableName());
        dto.setRecordID(log.getRecordID());
        dto.setAction(log.getAction());
        dto.setChangeTime(log.getChangeTime());
        dto.setChangeDetails(log.getChangeDetails());
        dto.setUserID(log.getUser() != null ? log.getUser().getUserID() : null);
        dto.setUserName(log.getUser() != null ? log.getUser().getFullName() : null);
        return dto;
    }
}