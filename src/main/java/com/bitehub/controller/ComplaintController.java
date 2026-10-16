package com.bitehub.controller;

import com.bitehub.dto.ComplaintDTO;
import com.bitehub.dto.ComplaintRequest;
import com.bitehub.dto.ComplaintResolveRequest;
import com.bitehub.model.ComplaintStatus;
import com.bitehub.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ComplaintDTO> createComplaint(@Valid @RequestBody ComplaintRequest request) {
        return new ResponseEntity<>(complaintService.createComplaint(request), HttpStatus.CREATED);
    }

    @GetMapping("/my-complaints")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<ComplaintDTO>> getMyComplaints() {
        return ResponseEntity.ok(complaintService.getMyComplaints());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ComplaintDTO>> getAllComplaints(@RequestParam(required = false) ComplaintStatus status) {
        return ResponseEntity.ok(complaintService.getAllComplaints(status));
    }

    @PutMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComplaintDTO> resolveComplaint(
            @PathVariable Long id,
            @Valid @RequestBody ComplaintResolveRequest request) {
        return ResponseEntity.ok(complaintService.resolveComplaint(id, request));
    }
}
