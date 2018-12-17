package com.teodorus.helpdesk.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.teodorus.helpdesk.domain.ChangeStatus;

public interface ChangeStatusRepository extends MongoRepository<ChangeStatus, String>{
	Iterable<ChangeStatus> findByTicketIdOrderByDateDesc(String ticketId);
}
