package com.trc.domain.ticket;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import com.trc.domain.ticket.TicketType;
import com.trc.user.User;

@Entity
@DiscriminatorValue(value = "CUSTOMER")
public class CustomerTicket extends Ticket {
	private static final long serialVersionUID = -8249415459905630871L;
	private User customer;
	private User assignee;

	public CustomerTicket() {
		this.type = TicketType.CUSTOMER;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@NotFound(action = NotFoundAction.IGNORE)
	@JoinColumn(name = "customer", insertable = true, updatable = true, nullable = true)
	public User getCustomer() {
		return customer;
	}

	public void setCustomer(User customer) {
		this.customer = customer;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@NotFound(action = NotFoundAction.IGNORE)
	@JoinColumn(name = "assignee", insertable = true, updatable = true, nullable = true)
	public User getAssignee() {
		return assignee;
	}

	public void setAssignee(User assignee) {
		this.assignee = assignee;
	}

}
