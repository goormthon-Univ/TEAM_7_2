package com.mooko.dev.handler;

import com.mooko.dev.event.ButtonEvent;
import com.mooko.dev.event.LeaveEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventHandler {

    private final SimpMessagingTemplate simpMessagingTemplate;


    @EventListener(ButtonEvent.class)
    public void buttonEventHandler(ButtonEvent buttonEvent) {
        simpMessagingTemplate.convertAndSend("/subscribe/button/"
                        + buttonEvent.getEventId(), buttonEvent);
    }

    @EventListener(LeaveEvent.class)
    public void leaveEventHandler(LeaveEvent leaveEvent) {
        simpMessagingTemplate.convertAndSend("/subscribe/leave-event/"
                        + leaveEvent.getEventId(), leaveEvent);
    }
}
