package br.ufsm.csi.tpav.pilacoin.service;

import br.ufsm.csi.tpav.pilacoin.model.Mensagem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RabbitService {
    private final ValidaService validaService;
    private final MineraService mineraService;
    private final QueryService queryService;
    private final SimpMessagingTemplate template;
    private List<Mensagem> msgs = new ArrayList<>();

    public RabbitService(ValidaService validaService, MineraService mineraService, QueryService queryService, SimpMessagingTemplate simpMessagingTemplate) {
        this.validaService = validaService;
        this.mineraService = mineraService;
        this.queryService = queryService;
        this.template = simpMessagingTemplate;

    }

    @RabbitListener(queues = "descobre-bloco")
    public void descobreBloco(@Payload String bloco){
        System.out.println("-=+=".repeat(10)+"\nBloco descoberto\n"+"-=+=".repeat(10));
        mineraService.mineraBloco(bloco);
    }

    @RabbitListener(queues = "pila-minerado")
    public void validaPila(@Payload String pila){
        validaService.validaPila(pila);
    }

    @RabbitListener(queues = "bloco-minerado")
    public void validaBloco(@Payload String bloco){
        validaService.validaBloco(bloco);
    }

    @RabbitListener(queues = "biancamagro")
    public void msgs(@Payload String msg) throws JsonProcessingException {
        System.out.println("-=+=".repeat(10)+"\n"+msg+"\n"+"-=+=".repeat(10));
        ObjectMapper om = new ObjectMapper();
        Mensagem message = om.readValue(msg, Mensagem.class);
        msgs.add(0,message);
        template.convertAndSend("/topic/message", msgs);
    }

    @RabbitListener(queues = "report")
    public void report(@Payload String report){
        System.out.println(report);
    }

    @RabbitListener(queues = "biancamagro-query")
    public void resultadoQuery(@Payload String resultado){
        queryService.recebeQuery(resultado);
    }

    @RabbitListener(queues = "clients-errors")
    public void errors(@Payload String error){
        System.out.println("Error: "+error);
    }
}
