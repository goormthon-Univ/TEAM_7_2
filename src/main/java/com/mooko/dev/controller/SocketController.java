package com.mooko.dev.controller;

import com.mooko.dev.dto.event.socket.UserEventCheckStatusDto;
import com.mooko.dev.facade.AggregationFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequiredArgsConstructor
public class SocketController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final AggregationFacade aggregationFacade;


    //3-3. 사진 업로드 완료 버튼
    @MessageMapping("/check/{eventId}")
    public void updateUserEventCheckStatus(@DestinationVariable Long eventId, @RequestBody UserEventCheckStatusDto userEventCheckStatusDto) {
        UserEventCheckStatusDto messageBody = aggregationFacade.updateUserEventCheckStatus(userEventCheckStatusDto, eventId);
        simpMessagingTemplate.convertAndSend("/subscribe/check/"+eventId.toString() + messageBody);
    }
}
