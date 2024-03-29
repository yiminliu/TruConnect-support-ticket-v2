package com.trc.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trc.domain.ticket.AdminTicket;
import com.trc.domain.ticket.AgentTicket;
import com.trc.domain.ticket.CustomerTicket;
import com.trc.domain.ticket.InquiryTicket;
import com.trc.domain.ticket.Ticket;
import com.trc.domain.ticket.TicketNote;
import com.trc.domain.ticket.TicketStatus;
import com.trc.domain.ticket.TicketType;
import com.trc.manager.UserManager;
import com.trc.user.User;

@Repository
@Transactional
public class TicketDao extends HibernateDaoSupport implements TicketDaoModel {

	@Autowired
	public void init(HibernateTemplate hibernateTemplate) {
		setHibernateTemplate(hibernateTemplate);
	}

	@Autowired
	UserManager userManager;// = new UserManager();

	/*************** Ticket Operations ******************/

	@Override
	public int createTicket(Ticket ticket) {
		int id = 0;
		validateTicket(ticket);
		if (ticket.getStatus() == null || ticket.getStatus() != TicketStatus.OPEN)
			ticket.setStatus(TicketStatus.OPEN);
		ticket.setCreatedDate(ticket.getCreatedDate());		
		if(ticket.getClass().equals(CustomerTicket.class) || ticket.getClass().equals(AgentTicket.class) || ticket.getClass().equals(AdminTicket.class)){
			id = createCustomerTicket((CustomerTicket) ticket);
		}
		else if(ticket.getClass().equals(InquiryTicket.class)){
			id = createInquiryTicket((InquiryTicket) ticket);
		}
		return id;		
	}		
	
	@Override
	public void updateTicket(Ticket ticket) {
		validateTicket(ticket);
		ticket.setLastModifiedDate(new Date());
		getHibernateTemplate().saveOrUpdate(ticket);
	}

	@Override
	public void deleteTicket(Ticket ticket) {
		validateTicket(ticket);
		getHibernateTemplate().delete(ticket);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Ticket> getAllTickets() {
		return getHibernateTemplate().find("from Ticket t order by t.id desc");
	}

	@Override
	@Transactional(readOnly = true)
	public List<User> getAllCreators() {
		return getHibernateTemplate().find("select distinct t.creator from Ticket t");
	}

	@Override
	public void closeTicket(Ticket ticket) {
		validateTicket(ticket);
		ticket.setStatus(TicketStatus.CLOSED);
		ticket.setLastModifiedDate(new Date());
		updateTicket(ticket);
	}

	@Override
	public void resolveTicket(Ticket ticket) {
		validateTicket(ticket);
		ticket.setStatus(TicketStatus.RESOLVED);
		ticket.setLastModifiedDate(new Date());
		updateTicket(ticket);
	}

	@Override
	public void rejectTicket(Ticket ticket) {
		validateTicket(ticket);
		ticket.setStatus(TicketStatus.REJECTED);
		ticket.setLastModifiedDate(new Date());
		updateTicket(ticket);
	}

	@Override
	public void reopenTicket(Ticket ticket) {
		validateTicket(ticket);
		ticket.setStatus(TicketStatus.REOPEN);
		ticket.setLastModifiedDate(new Date());
		updateTicket(ticket);
	}

	@Override
	@Transactional(readOnly = true)
	public Ticket searchTicketById(int ticketId) {
		Ticket ticket = null;
		try {
			// return (Ticket)getHibernateTemplate().get(Ticket.class, ticketId);
			ticket = (Ticket) getHibernateTemplate().find("from Ticket t where t.id = ?", ticketId).get(0);
			ticket.setStatus(TicketStatus.IN_PROCESS_LOCKED);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return ticket;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Ticket> searchTicketByTitle(String title) {
		return getHibernateTemplate().find("from Ticket t where t.title = ?", title);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Ticket> searchTicketByCustomer(String customerName) {
		return getHibernateTemplate().find("from Ticket t where t.customer.username like ? order by t.id desc", wildCardKeyword(customerName));
	}

	@Override
	@Transactional(readOnly = true)
	public List<Ticket> searchTicketByCreator(String creatorName) {
		return getHibernateTemplate().find("from Ticket t where t.creator.username like ? order by t.id desc", wildCardKeyword(creatorName));
	}

	@Override
	@Transactional(readOnly = true)
	public List<Ticket> searchTicketByAssignee(String assigneeName) {
		return getHibernateTemplate().find("from Ticket t where t.assignee.username like ? order by t.id desc", wildCardKeyword(assigneeName));
	}

	@Override
	@Transactional(readOnly = true)
	public List<Ticket> searchTicketByKeyword(String keyword) {

		HibernateTemplate ht = getHibernateTemplate();

		List<Ticket> resultList = new ArrayList<Ticket>();

		List<Ticket> titleList = ht.find("from Ticket t where t.title like ? order by t.id desc", wildCardKeyword(keyword));
		List<Ticket> creatorList = ht.find("from Ticket t where t.creator.username like ? order by t.id desc", wildCardKeyword(keyword));// searchTicket
																																																																			// By
																																																																			// Creator;
		List<Ticket> assigneeList = ht.find("from Ticket t where t.assignee.username like ? order by t.id desc", wildCardKeyword(keyword));
		List<Ticket> custList = ht.find("from Ticket t where t.customer.username like ? order by t.id desc", wildCardKeyword(keyword));// searchTicketByCustomer(keyword);
		// List<Ticket> noteList =
		// ht.find("from Ticket t where t.notes.note like ?",
		// wildCardKeyword(keyword));

		resultList.addAll(titleList);
		resultList.addAll(creatorList);
		resultList.addAll(assigneeList);
		resultList.addAll(custList);
		// resultList.addAll(noteList);

		return resultList;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Ticket> searchTicketByStatus(TicketStatus status) {
		List<Ticket> t = getHibernateTemplate().find("from Ticket t where t.status = ? order by t.id desc", status);
		return t;
	}

	private void validateTicket(Ticket ticket) {
		if (ticket == null)
			throw new IllegalArgumentException("Ticket cannot be null");
	}

	/*************** TicketNote Operations ******************/

	public int createTicketNote(TicketNote ticketNote) {
		return (Integer) getHibernateTemplate().save(ticketNote);
	}

	public List<TicketNote> searchTicketNotes(Ticket ticket) {
		validateTicket(ticket);
		int ticketId = ticket.getId();
		return getHibernateTemplate().find("from TicketNote where ticketId = ?", ticketId);
	}

	public void updateTicketNote(TicketNote ticketNote) {
		if (ticketNote.getCreator() == null) {
			ticketNote.setCreator(userManager.getCurrentUser());
		}
		getHibernateTemplate().saveOrUpdate(ticketNote);
	}

	/*************** Helper Methods ******************/

	private String wildCardKeyword(String keyword) {
		if (keyword == null || keyword.equals(""))
			return null;
		else
			return "%" + keyword + "%";
	}
	
	private int createCustomerTicket(CustomerTicket ticket) {
		User customer = null;
		HibernateTemplate ht = getHibernateTemplate();
		if (ticket.getCustomer() != null)
			customer = userManager.getUserByUsername(ticket.getCustomer().getUsername());//have a persistent customer object
		else {  //retrieve customer from session		 	
			if(ticket.getClass().equals(AgentTicket.class) || ticket.getClass().equals(AdminTicket.class)){
				if(ticket.getClass().equals(AdminTicket.class))
                   ticket.setType(TicketType.ADMIN);
				if(ticket.getClass().equals(AgentTicket.class))
	               ticket.setType(TicketType.AGENT);
				customer = userManager.getLoggedInUser();			    
			}
			else {
				ticket.setType(TicketType.CUSTOMER);
				customer = userManager.getCurrentUser();
			}
			//customer = userManager.getUserById(customer.getUserId());//have a persistent customer object
			customer = userManager.getUserByUsername(customer.getUsername());//have a persistent customer object
			
		}	
		ticket.setCustomer(customer);
		if(ticket.getClass().equals(AgentTicket.class)){
		   ticket = setCreator((AgentTicket)ticket);
		}   
		int id = (Integer) ht.save(ticket);
		return id;
	}
	
	private int createInquiryTicket(InquiryTicket ticket){
		HibernateTemplate ht = getHibernateTemplate();
		int id = (Integer) ht.save(ticket);
		return id;
	}
	
	private CustomerTicket setCreator(AgentTicket agentTicket){		
		User creator = null;
		if (agentTicket.getCreator() != null)	
			creator = userManager.getUserByUsername(agentTicket.getCreator().getUsername());//have a persistent customer object
		else {
			if ((TicketType.CUSTOMER).equals(agentTicket.getType())) 
				creator = userManager.getCurrentUser();
			else if ((TicketType.INQUIRY).equals(agentTicket.getType()))
				creator = null;
			if ((TicketType.ADMIN).equals(agentTicket.getType()) || (TicketType.AGENT).equals(agentTicket.getType()))
			    creator = userManager.getSessionControllingUser();
			if(creator != null)
			   creator = userManager.getUserById(creator.getUserId());				
		}	
		agentTicket.setCreator(creator);		
		return agentTicket;
	}		
}