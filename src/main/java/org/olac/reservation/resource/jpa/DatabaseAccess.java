package org.olac.reservation.resource.jpa;

import org.olac.reservation.resource.TicketRA;
import org.olac.reservation.resource.TicketType;
import org.olac.reservation.resource.jpa.entity.TicketTypeEntity;
import org.olac.reservation.resource.jpa.repository.TicketTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class DatabaseAccess implements TicketRA {

    private final TicketTypeRepository ticketTypeRepository;

    @Autowired
    public DatabaseAccess(TicketTypeRepository ticketTypeRepository) {
        this.ticketTypeRepository = ticketTypeRepository;
    }

    @Override
    public List<TicketType> getTicketTypes() {
        return StreamSupport.stream(ticketTypeRepository.findAll().spliterator(), false)
                .map(t -> new TicketType(t.getTicketType(), t.getTicketCost()))
                .toList();
    }

    @Override
    public void saveTicketType(TicketType ticketType) {
        TicketTypeEntity type = ticketTypeRepository.findByTicketType(ticketType.getTicketType())
                .orElseGet(() -> new TicketTypeEntity(ticketType.getTicketType(), ticketType.getCostPerTicket()));

        type.setTicketCost(ticketType.getCostPerTicket());

        ticketTypeRepository.save(type);
    }

    @Override
    public void renameTicketType(String oldName, String newName) {
        ticketTypeRepository.findByTicketType(oldName).ifPresent(t -> {
            t.setTicketType(newName);
            ticketTypeRepository.save(t);
        });
    }

}
