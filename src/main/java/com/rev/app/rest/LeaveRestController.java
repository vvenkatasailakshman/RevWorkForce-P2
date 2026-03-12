package com.rev.app.rest;

import com.rev.app.dto.LeaveDto;
import com.rev.app.service.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/leaves")
public class LeaveRestController {

    @Autowired
    private LeaveService leaveService;

    @GetMapping("/employee/{empId}")
    public org.springframework.data.domain.Page<LeaveDto> getEmployeeLeaves(
            @PathVariable String empId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startDate") String sortBy) {
        return leaveService.getEmployeeLeaves(empId, page, size, sortBy);
    }

    @PostMapping("/apply")
    public LeaveDto applyLeave(@RequestBody LeaveDto dto) {
        return leaveService.applyLeave(dto);
    }

    @PutMapping("/approve/{id}")
    public LeaveDto approveLeave(@PathVariable Long id, @RequestParam String managerId,
            @RequestParam(required = false) String comment) {
        return leaveService.approveLeave(id, managerId, comment);
    }
}
