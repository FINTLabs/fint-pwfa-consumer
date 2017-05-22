package no.fint.consumer.event;

import lombok.extern.slf4j.Slf4j;
import no.fint.audit.FintAuditService;
import no.fint.event.model.Event;
import no.fint.event.model.Status;
import no.fint.events.FintEvents;
import no.fint.events.FintEventsHealth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class ConsumerEventUtil {

    @Autowired
    private FintEvents fintEvents;

    @Autowired
    private FintEventsHealth fintEventsHealth;

    @Autowired
    private FintAuditService fintAuditService;

    public Optional<Event> healthCheck(Event event) {
        fintAuditService.audit(event);

        event.setStatus(Status.DOWNSTREAM_QUEUE);
        fintAuditService.audit(event);

        log.info("Sending health check event {} to {}", event.getAction(), event.getOrgId());
        Event response = fintEventsHealth.sendHealthCheck(event.getOrgId(), event.getCorrId(), event);
        response.setStatus(Status.SENT_TO_CLIENT);
        fintAuditService.audit(response);

        return Optional.of(response);
    }

    public void send(Event event) {
        fintAuditService.audit(event);

        event.setStatus(Status.DOWNSTREAM_QUEUE);
        fintAuditService.audit(event);

        log.info("Sending replyTo event {} to {}", event.getAction(), event.getOrgId());
        fintEvents.sendDownstream(event.getOrgId(), event);
        event.setStatus(Status.SENT_TO_CLIENT);
        fintAuditService.audit(event);
    }
}
