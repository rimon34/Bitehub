package com.bitehub.service;

import com.bitehub.dto.ComplaintDTO;
import com.bitehub.dto.ComplaintRequest;
import com.bitehub.dto.ComplaintResolveRequest;
import com.bitehub.exception.BadRequestException;
import com.bitehub.exception.ResourceNotFoundException;
import com.bitehub.model.*;
import com.bitehub.repository.ComplaintRepository;
import com.bitehub.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final OrderRepository orderRepository;
    private final AuthService authService;

    public ComplaintService(ComplaintRepository complaintRepository,
                            OrderRepository orderRepository,
                            AuthService authService) {
        this.complaintRepository = complaintRepository;
        this.orderRepository = orderRepository;
        this.authService = authService;
    }

    @Transactional
    public ComplaintDTO createComplaint(ComplaintRequest request) {
        User customer = authService.getCurrentUser();
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.getOrderId()));

        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new BadRequestException("You can only raise complaints for your own orders");
        }

        Complaint complaint = new Complaint();
        complaint.setOrder(order);
        complaint.setCustomer(customer);
        complaint.setSubject(request.getSubject());
        complaint.setDescription(request.getDescription());
        complaint.setStatus(ComplaintStatus.OPEN);
        complaint.setCreatedAt(LocalDateTime.now());

        return new ComplaintDTO(complaintRepository.save(complaint));
    }

    public List<ComplaintDTO> getMyComplaints() {
        User customer = authService.getCurrentUser();
        return complaintRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId()).stream()
                .map(ComplaintDTO::new)
                .collect(Collectors.toList());
    }

    public List<ComplaintDTO> getAllComplaints(ComplaintStatus status) {
        List<Complaint> list;
        if (status != null) {
            list = complaintRepository.findByStatusOrderByCreatedAtDesc(status);
        } else {
            list = complaintRepository.findAllByOrderByCreatedAtDesc();
        }
        return list.stream().map(ComplaintDTO::new).collect(Collectors.toList());
    }

    @Transactional
    public ComplaintDTO resolveComplaint(Long id, ComplaintResolveRequest request) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));

        complaint.setStatus(request.getStatus());
        complaint.setAdminNotes(request.getAdminNotes());
        if (request.getStatus() == ComplaintStatus.RESOLVED || request.getStatus() == ComplaintStatus.REJECTED) {
            complaint.setResolvedAt(LocalDateTime.now());
        }

        return new ComplaintDTO(complaintRepository.save(complaint));
    }
}
