package com.lrms.service;

import com.lrms.GlobalExceptionHandler;
import com.lrms.entity.HousekeepingTask;
import com.lrms.entity.RestaurantTable;
import com.lrms.entity.Staff;
import com.lrms.repository.HousekeepingTaskRepository;
import com.lrms.repository.RestaurantTableRepository;
import com.lrms.repository.StaffRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class HousekeepingService {

    private final HousekeepingTaskRepository taskRepository;
    private final StaffRepository staffRepository;
    private final RestaurantTableRepository tableRepository;

    public HousekeepingService(HousekeepingTaskRepository taskRepository, StaffRepository staffRepository, RestaurantTableRepository tableRepository) {
        this.taskRepository = taskRepository;
        this.staffRepository = staffRepository;
        this.tableRepository = tableRepository;
    }

    public List<HousekeepingTask> getAllTasks() { return taskRepository.findAll(); }

    public HousekeepingTask createTask(HousekeepingTask task) {
        return taskRepository.save(task);
    }

    public void updateTaskStatus(Long id, String status) {
        HousekeepingTask task = taskRepository.findById(id).orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Task not found"));
        task.setStatus(status);
        if ("COMPLETED".equals(status) || "APPROVED".equals(status)) {
            task.setCompletedAt(LocalDateTime.now());
            // Auto-set room to AVAILABLE when housekeeping is done
            if (task.getRoom() != null && "CLEANING".equals(task.getRoom().getStatus())) {
                task.getRoom().setStatus("AVAILABLE");
            }
            // Auto-set restaurant table to AVAILABLE when cleaning is done
            if (task.getRestaurantTable() != null && "CLEANING".equals(task.getRestaurantTable().getStatus())) {
                task.getRestaurantTable().setStatus("AVAILABLE");
                tableRepository.save(task.getRestaurantTable());
            }
        }
        taskRepository.save(task);
    }

    public HousekeepingTask createCleaningTask(com.lrms.entity.Room room) {
        HousekeepingTask task = new HousekeepingTask();
        task.setRoom(room);
        task.setTaskType("CLEANING");
        task.setPriority("NORMAL");
        task.setStatus("PENDING");
        task.setNotes("Auto-generated after checkout");
        return taskRepository.save(task);
    }

    public HousekeepingTask createTableCleaningTask(RestaurantTable table) {
        HousekeepingTask task = new HousekeepingTask();
        task.setRestaurantTable(table);
        task.setTaskType("CLEANING");
        task.setPriority("NORMAL");
        task.setStatus("PENDING");
        task.setNotes("Auto-generated: Table " + table.getTableNumber() + " cleaning after order served");
        return taskRepository.save(task);
    }

    public long countPendingTasks() {
        return taskRepository.findAll().stream()
                .filter(t -> !"COMPLETED".equals(t.getStatus()) && !"APPROVED".equals(t.getStatus()))
                .count();
    }

    public List<Staff> getAllStaff() { return staffRepository.findAll(); }

    public Staff createStaff(Staff staff) {
        return staffRepository.save(staff);
    }

    public Staff updateStaffRole(Long id, String role) {
        Staff s = staffRepository.findById(id).orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Staff not found"));
        s.setRole(role);
        return staffRepository.save(s);
    }

    public Staff toggleStaffActive(Long id) {
        Staff s = staffRepository.findById(id).orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("Staff not found"));
        s.setIsActive(!Boolean.TRUE.equals(s.getIsActive()));
        return staffRepository.save(s);
    }
}
