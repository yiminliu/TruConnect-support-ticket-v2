package com.trc.web.validation;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.trc.domain.ticket.AdminTicket;
import com.trc.domain.ticket.AgentTicket;
import com.trc.domain.ticket.CustomerTicket;
import com.trc.domain.ticket.InquiryTicket;
import com.trc.domain.ticket.Ticket;

public class TicketValidator implements Validator {

	@Override
	public boolean supports(Class<?> myClass) {
		return Ticket.class.isAssignableFrom(myClass) || InquiryTicket.class.isAssignableFrom(myClass) || CustomerTicket.class.isAssignableFrom(myClass)
				|| AgentTicket.class.isAssignableFrom(myClass) || AdminTicket.class.isAssignableFrom(myClass);
	}

	@Override
	public void validate(Object target, Errors errors) {
		Ticket ticket = (Ticket) target;
		generalValidate(ticket, errors);
		if (ticket instanceof InquiryTicket) {
			checkInquiryTicket(ticket, errors);
		} else if (ticket instanceof CustomerTicket) {
			checkCustomerTicket(ticket, errors);
		} else if (ticket instanceof AgentTicket) {
			checkAgentTicket(ticket, errors);
		} else if (ticket instanceof AdminTicket) {
			checkAdminTicket(ticket, errors);
		}
	}
	
	private void generalValidate(Ticket ticket, Errors errors){
		if(ticket.getDescription() == null || ticket.getDescription().isEmpty()){
			errors.rejectValue("Description", "ticket.description.required", "Description is empty");
		} else if(ticket.getDescription().length() < 5)
			errors.rejectValue("Description", "ticket.description.short", "Description is too short");	
		if(ticket.getCategory() == null)
			errors.rejectValue("Category", "ticket.category.required", "Please select a category");		
	}

	private void checkAdminTicket(Ticket ticket, Errors errors) {
		AdminTicket adminTicket = (AdminTicket) ticket;
		if(adminTicket != null && adminTicket.getPriority() == null)
			errors.rejectValue("Priority", "ticket.priority.required", "Please select a priority");
	}

	private void checkAgentTicket(Ticket ticket, Errors errors) {
		AgentTicket agentTicket = (AgentTicket) ticket;
		if(agentTicket != null && agentTicket.getPriority() == null)
			errors.rejectValue("Priority", "ticket.priority.required", "Please select a priority");
	}

	private void checkCustomerTicket(Ticket ticket, Errors errors) {
		// TODO Auto-generated method stub

	}

	private void checkInquiryTicket(Ticket ticket, Errors errors) {
				
		InquiryTicket inquiryTicket = (InquiryTicket)ticket;
		if(inquiryTicket != null && ValidationUtil.isEmpty(inquiryTicket.getContactEmail())){
			errors.rejectValue("Email", "email.required", "Please enter an e-mail address");	
		}
	    else if (!EmailValidator.checkEmail(inquiryTicket.getContactEmail())) {
	      errors.rejectValue("Email", "email.invalid", "E-mail address is invalid");
	    }
	}		

}
