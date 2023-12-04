package br.ufsm.csi.tpav.pilacoin.websocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping
public class WebSocketController {

    @MessageMapping("/socket")
    @SendTo("/topic/message")
    public String recivedMessage(String message){
        System.out.println("Message socket"+message);
        return message;
    }
}
