package com.teodorus.helpdesk.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.teodorus.helpdesk.domain.ChangeStatus;
import com.teodorus.helpdesk.domain.Ticket;
import com.teodorus.helpdesk.repository.ChangeStatusRepository;
import com.teodorus.helpdesk.repository.TicketRepository;

@Service
public class TicketService {
	
	@Autowired
	private TicketRepository ticketRepository;
	
	@Autowired
	private ChangeStatusRepository changeStatusRepository;
	
	public Ticket createOrUpdate(Ticket ticket) {
		return ticketRepository.save(ticket);
	}
	
	public Ticket findById(String id) {
		return ticketRepository.findById(id).orElse(null);
	}
	
	public void delete(String id) {
		ticketRepository.deleteById(id);
	}
	
	public Page<Ticket> listTicket(int page, int size) {
		Pageable pages = PageRequest.of(page, size);
		return ticketRepository.findAll(pages);
	}
	
	public ChangeStatus createChangeStatus(ChangeStatus changeStatus) {
		return changeStatusRepository.save(changeStatus);
	}
	
	public Iterable<ChangeStatus> listChangeStatus(String ticketId) {
		return changeStatusRepository.findByTicketIdOrderByDateDesc(ticketId);
	}
	
	public Page<Ticket> findByCurrentUser(int page, int size, String userId) {
		Pageable pages = PageRequest.of(page, size);
		return ticketRepository.findByUserIdOrderByDateDesc(pages, userId);
	}
	
	public Page<Ticket> findByParameters(int page, int size, String title, String status, String priority) {
		Pageable pages = PageRequest.of(page, size);
		return ticketRepository.findByTitleIgnoreCaseContainingAndStatusContainingAndPriorityContainingOrderByDateDesc(pages, title, status, priority);
	}
	
	public Page<Ticket> findByParameters(int page, int size, String title, String status, String priority, String userId) {
		Pageable pages = PageRequest.of(page, size);
		return ticketRepository.findByTitleIgnoreCaseContainingAndStatusContainingAndPriorityContainingAndUserIdOrderByDateDesc(pages, title, status, priority, userId);
	}
	
	public Page<Ticket> findByNumber(int page, int size, Integer number) {
		Pageable pages = PageRequest.of(page, size);
		return ticketRepository.findByNumber(number, pages);
	}
	
	public Iterable<Ticket> findAll() {
		return ticketRepository.findAll();
	}
	
	public Page<Ticket> findByParametersAndAssignedUser(int page, int size, String title, String status, String priority, String assignedUser) {
		Pageable pages = PageRequest.of(page, size);
		return ticketRepository.findByTitleIgnoreCaseContainingAndStatusContainingAndPriorityContainingAndAssignedUserIdOrderByDateDesc(pages, title, status, priority, assignedUser);
	}

}
