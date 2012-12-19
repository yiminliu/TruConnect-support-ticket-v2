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
@DiscriminatorValue(value = "AGENT")
public class AgentTicket extends CustomerTicket {
	private static final long serialVersionUID = 6566960793806943444L;
	private User creator;

	public AgentTicket() {
		this.type = TicketType.AGENT;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@NotFound(action = NotFoundAction.IGNORE)
	@JoinColumn(name = "creator", insertable = true, updatable = false, nullable = true)
	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

}