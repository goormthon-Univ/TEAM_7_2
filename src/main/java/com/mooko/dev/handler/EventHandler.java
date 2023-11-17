package com.mooko.dev.handler;

import com.mooko.dev.event.ButtonEvent;
import com.mooko.dev.event.LeaveEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventHandler {

    private final SimpMessagingTemplate simpMessagingTemplate;


    @EventListener(ButtonEvent.class)
    public void buttonEventHandler(ButtonEvent buttonEvent) {
        simpMessagingTemplate.convertAndSend("/subscribe/button/"
                        + buttonEvent.getEventId(), buttonEvent);
        log.info("버튼이 눌렸습니다 = {}, 상태값 = {} ", buttonEvent.getEventId(), buttonEvent.isButtonStatus());
    }

    @EventListener(LeaveEvent.class)
    public void leaveEventHandler(LeaveEvent leaveEvent) {
        simpMessagingTemplate.convertAndSend("/subscribe/leave-event/"
                        + leaveEvent.getEventId(), leaveEvent);
        log.info("방장이 방을 나갔습니다 = {}, 상태값 = {} ", leaveEvent.getEventId(), leaveEvent.isEventStatus());

    }
}
