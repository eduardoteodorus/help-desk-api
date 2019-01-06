package com.teodorus.helpdesk.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.teodorus.helpdesk.domain.Ticket;

public interface TicketRepository extends MongoRepository<Ticket, String>{
	
	Page<Ticket> findByUserIdOrderByDateDesc(Pageable pages, String userId);
	
	Page<Ticket> findByTitleIgnoreCaseContainingAndStatusContainingAndPriorityContainingOrderByDateDesc(Pageable pages, String tittle, String status, String prioridade);
	
	Page<Ticket> findByTitleIgnoreCaseContainingAndStatusContainingAndPriorityContainingAndUserIdOrderByDateDesc(Pageable pages, String tittle, String status, String prioridade, String userId);
	
	Page<Ticket> findByTitleIgnoreCaseContainingAndStatusContainingAndPriorityContainingAndAssignedUserIdOrderByDateDesc(Pageable pages, String tittle, String status, String prioridade, String userId);
	
	Page<Ticket> findByNumber(Integer number, Pageable pages);

}
