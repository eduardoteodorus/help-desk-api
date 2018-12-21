package com.teodorus.helpdesk.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.DuplicateKeyException;
import com.teodorus.helpdesk.domain.ChangeStatus;
import com.teodorus.helpdesk.domain.Summary;
import com.teodorus.helpdesk.domain.Ticket;
import com.teodorus.helpdesk.domain.User;
import com.teodorus.helpdesk.enums.ProfileEnum;
import com.teodorus.helpdesk.enums.StatusEnum;
import com.teodorus.helpdesk.security.jwt.JwtTokenUtil;
import com.teodorus.helpdesk.service.TicketService;
import com.teodorus.helpdesk.service.UserService;

@RestController
@RequestMapping("/api/ticket")
@CrossOrigin(origins = "*")
public class TicketController {

	@Autowired
	private TicketService service;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private JwtTokenUtil jwtTokentUtil;
	
	
	@PostMapping
	@PreAuthorize("hasAnyRole('CUSTOMER')")
	public ResponseEntity<Response<Ticket>> create(HttpServletRequest request, @RequestBody Ticket ticket,
			BindingResult result) {
		Response<Ticket> response = new Response<Ticket>();
		
		try {
			validateCreateTicket(ticket, result);
			if (result.hasErrors()) {
				result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
				return ResponseEntity.badRequest().body(response);
			}
			
			ticket.setStatus(StatusEnum.NEW);
			ticket.setUser(getUserFromRequest(request));
			ticket.setDate(new Date());
			ticket.setNumber(new Random().nextInt(9999));
			Ticket newTicket = service.createOrUpdate(ticket);
			
			response.setData(newTicket);
			
		} catch (DuplicateKeyException dke) {
			response.getErrors().add("E-mail already registered.");
			return ResponseEntity.badRequest().body(response);
		} catch (Exception e) {
			response.getErrors().add(e.getMessage());
			return ResponseEntity.badRequest().body(response);
		}
		
		return ResponseEntity.ok(response);
	}
	
	
	@PutMapping
	@PreAuthorize("hasAnyRole('CUSTOMER')")
	public ResponseEntity<Response<Ticket>> update(HttpServletRequest request, @RequestBody Ticket ticket,
			BindingResult result) {
		Response<Ticket> response = new Response<Ticket>();
		
		try {
			validateCreateTicket(ticket, result);
			if (result.hasErrors()) {
				result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
				return ResponseEntity.badRequest().body(response);
			}
			
			Ticket currentTicket = service.findById(ticket.getId());
			
			ticket.setStatus(currentTicket.getStatus());
			ticket.setUser(currentTicket.getUser());
			ticket.setNumber(currentTicket.getNumber());
			ticket.setDate(currentTicket.getDate());
			
			if (currentTicket.getAssignedUser() != null) {
				ticket.setAssignedUser(currentTicket.getAssignedUser());
			}
			
			Ticket updatedTicket = service.createOrUpdate(ticket);
			
			response.setData(updatedTicket);
			
		} catch (DuplicateKeyException dke) {
			response.getErrors().add("E-mail already registered.");
			return ResponseEntity.badRequest().body(response);
		} catch (Exception e) {
			response.getErrors().add(e.getMessage());
			return ResponseEntity.badRequest().body(response);
		}
		
		return ResponseEntity.ok(response);
	}
	
	
	@GetMapping(value = "{id}")
	@PreAuthorize("hasAnyRole('CUSTOMER', 'TECHNICIAN')")
	public ResponseEntity<Response<Ticket>> findById(@PathVariable("id") String id) {
		Response<Ticket> response = new Response<Ticket>();
		
		Ticket ticket = service.findById(id);
		
		if (ticket == null) {
			response.getErrors().add("Register not found with id: " + id);
			return ResponseEntity.badRequest().body(response);
		}
		
		List<ChangeStatus> changes = new ArrayList<>();		
		service.listChangeStatus(ticket.getId()).forEach(c -> {
			c.setTicket(null);
			changes.add(c);
		});
		
		ticket.setChanges(changes);		
		response.setData(ticket);			
		return ResponseEntity.ok(response);
	}
	
	@DeleteMapping(value = "{id}")
	@PreAuthorize("hasAnyRole('CUSTOMER')")
	public ResponseEntity<Response<String>> delete(@PathVariable("id") String id) {
		service.delete(id);
		return ResponseEntity.ok(new Response<String>());
	}
	
	
	@GetMapping(value = "{page}/{size}")
	@PreAuthorize("hasAnyRole('CUSTOMER', 'TECHNICIAN')")
	public ResponseEntity<Response<Page<Ticket>>> findAll(HttpServletRequest request, 
			@PathVariable("page") int page, @PathVariable("size") int size) {
		Response<Page<Ticket>> response = new Response<Page<Ticket>>();
		
		Page<Ticket> tickets = null;
		User user = getUserFromRequest(request);
		
		if (ProfileEnum.ROLE_TECHICIAN.equals(user.getProfile())) {
			tickets = service.listTicket(page, size);
		} else {
			tickets = service.findByCurrentUser(page, size, user.getId());
		}
		
		response.setData(tickets);	
		
		return ResponseEntity.ok(response);
	}
	
	@GetMapping(value = "{page}/{size}/{number}/{title}/{status}/{priority}/{assigned}")
	@PreAuthorize("hasAnyRole('CUSTOMER', 'TECHNICIAN')")
	public ResponseEntity<Response<Page<Ticket>>> findByParams(HttpServletRequest request, 
					@PathVariable("page") int page, 
					@PathVariable("size") int size, 
					@PathVariable("number") Integer number, 
					@PathVariable("title") String title, 
					@PathVariable("status") String status, 
					@PathVariable("priority") String priority, 
					@PathVariable("assigned") boolean assigned) {
		Response<Page<Ticket>> response = new Response<Page<Ticket>>();
		
		title = "uninformed".equals(title)? "" : title;
		status = "uninformed".equals(status)? "" : status;
		priority = "uninformed".equals(priority)? "" : priority;
		
		Page<Ticket> tickets = null;
		User user = getUserFromRequest(request);
		
		if (number > 0) {
			tickets = service.findByNumber(page, size, number);
		} else {
			if (ProfileEnum.ROLE_TECHICIAN.equals(user.getProfile())) {
				if (assigned) {
					tickets = service.findByParametersAndAssignedUser(page, size, title, status, priority, user.getId());
				} else {
					tickets = service.findByParameters(page, size, title, status, priority);
				}
			} else if (ProfileEnum.ROLE_CUSTOMER.equals(user.getProfile())) {
				tickets = service.findByParameters(page, size, title, status, priority, user.getId());
			}
		}
		
		response.setData(tickets);	
		
		return ResponseEntity.ok(response);
	}
	
	@PutMapping(value = "{id}/{status}")
	@PreAuthorize("hasAnyRole('CUSTOMER', 'TECHNICIAN')")
	public ResponseEntity<Response<Ticket>> changeStatus(HttpServletRequest request, 
			@PathVariable("id") String id, 
			@PathVariable("status") String status,
			BindingResult result) {
		
		Response<Ticket> response = new Response<Ticket>();
		
		try {
			validateChangeStatus(id, status, result);
			
			if (result.hasErrors()) {
				result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
				return ResponseEntity.badRequest().body(response);
			}
			
			Ticket currentTicket = service.findById(id);			
			currentTicket.setStatus(StatusEnum.getStatus(status));
			
			if ("Assigned".equals(status)) {
				currentTicket.setAssignedUser(getUserFromRequest(request));
			}
			
			Ticket updatedTicket = service.createOrUpdate(currentTicket);	
			ChangeStatus change = new ChangeStatus();
			change.setUserChange(getUserFromRequest(request));
			change.setDate(new Date());
			change.setStatus(StatusEnum.getStatus(status));
			change.setTicket(updatedTicket);
			
			service.createChangeStatus(change);
			response.setData(updatedTicket);
			
		
		} catch (Exception e) {
			response.getErrors().add(e.getMessage());
			return ResponseEntity.badRequest().body(response);
		}
		
		return ResponseEntity.ok(response);
	}
	
	@GetMapping(value = "/summary")
	public ResponseEntity<Response<Summary>> findSummary() {
		Response<Summary> response = new Response<Summary>();
		
		int amountNew = 0;
		int amountResolved = 0;
		int amountApproved = 0;
		int amountDisapproved = 0;
		int amountAssigned = 0;
		int amountClosed = 0;
		
		Iterable<Ticket> todos = service.findAll();
		
		if (todos != null) {
			for (Ticket t : todos) {
				if (t.getStatus().equals(StatusEnum.NEW)) {
					amountNew++;
				}
				if (t.getStatus().equals(StatusEnum.RESOLVED)) {
					amountResolved++;
				}
				if (t.getStatus().equals(StatusEnum.APPROVED)) {
					amountApproved++;
				}
				if (t.getStatus().equals(StatusEnum.DISAPPROVED)) {
					amountDisapproved++;
				}
				if (t.getStatus().equals(StatusEnum.ASSIGNED)) {
					amountAssigned++;
				}
				if (t.getStatus().equals(StatusEnum.CLOSED)) {
					amountClosed++;
				}
			}
		}
		
		Summary sum = new Summary();
		sum.setAmountNew(amountNew);
		sum.setAmountResolved(amountResolved);
		sum.setAmountApproved(amountApproved);
		sum.setAmountDisapproved(amountDisapproved);
		sum.setAmountAssigned(amountAssigned);
		sum.setAmountClosed(amountClosed);
		
		response.setData(sum);
		
		
		return ResponseEntity.ok(response);
	}
	
	
	
	private User getUserFromRequest(HttpServletRequest request) {
		String token = request.getHeader("Authorization");
		String email = jwtTokentUtil.getUserNameFromToken(token);
		return userService.findByEmail(email);
	}
	
	
	private void validateCreateTicket(Ticket ticket, BindingResult result) {
		if (ticket.getTitle() == null) {
			result.addError(new ObjectError("Ticket", "Title is required"));
		}
	}
	
	private void validateUpdateTicket(Ticket ticket, BindingResult result) {
		if (ticket.getId() == null) {
			result.addError(new ObjectError("Ticket", "Id is required"));
		}
		if (ticket.getTitle() == null) {
			result.addError(new ObjectError("Ticket", "Title is required"));
		}
	}
	
	private void validateChangeStatus(String id, String status, BindingResult result) {
		if (id == null || id.isEmpty()) {
			result.addError(new ObjectError("Ticket", "Id is required"));
		}
		if (status == null || status.isEmpty()) {
			result.addError(new ObjectError("Ticket", "Status is required"));
		}
	}
	
}
