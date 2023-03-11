package org.olac.reservation.engine.impl;

import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.olac.reservation.resource.TicketDatastoreAccess;
import org.olac.reservation.resource.model.Payment;
import org.olac.reservation.resource.model.PaymentStatus;
import org.olac.reservation.resource.model.Reservation;
import org.olac.reservation.resource.model.TicketType;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.lang.Math.max;
import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
public class ThymeleafTemplateEngine implements org.olac.reservation.engine.TemplateEngine {

    private final ITemplateEngine templateEngine;
    private final TicketDatastoreAccess ticketDatastoreAccess;
    private final FormattingUtilities formattingUtilities;

    @Override
    public String createPaymentReceivedConfirmation(Reservation reservation) {
        Context ctx = getContextWithInvoice(reservation);
        return templateEngine.process("email/payment-confirmation.html", ctx);
    }

    @Override
    public String createPaymentInstructions(Reservation reservation) {
        Context ctx = getContextWithInvoice(reservation);
        return templateEngine.process("email/payment-instructions.html", ctx);
    }

    @NotNull
    private Context getContextWithInvoice(Reservation reservation) {
        double totalCharges = reservation.getAmountDue();
        double totalPaid = reservation.getPayments().stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESSFUL)
                .mapToDouble(Payment::getAmount)
                .sum();
        double amountDue = max(0.0, totalCharges - totalPaid);

        Map<String, TicketType> ticketTypes = ticketDatastoreAccess.getTicketTypes().stream()
                .collect(toMap(TicketType::getCode, Function.identity()));

        List<TicketRow> ticketCounts = reservation.getTicketCounts().stream()
                .map(c -> TicketRow.builder()
                        .description(ticketTypes.get(c.getTicketTypeCode()).getDescription())
                        .cost(ticketTypes.get(c.getTicketTypeCode()).getCostPerTicket())
                        .count(c.getCount())
                        .total(c.getCount() * ticketTypes.get(c.getTicketTypeCode()).getCostPerTicket())
                        .build())
                .sorted(Comparator.comparing(TicketRow::getCost).reversed())
                .toList();

        Context ctx = new Context();
        ctx.setVariable("reservation", reservation);
        ctx.setVariable("totalCharges", totalCharges);
        ctx.setVariable("paymentReceived", totalPaid);
        ctx.setVariable("amountDue", amountDue);
        ctx.setVariable("ticketCounts", ticketCounts);
        ctx.setVariable("tutil", formattingUtilities);
        return ctx;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketRow {
        String description;
        double cost;
        long count;
        double total;
    }

}
