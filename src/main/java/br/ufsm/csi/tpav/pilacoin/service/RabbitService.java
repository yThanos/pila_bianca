package br.ufsm.csi.tpav.pilacoin.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class RabbitService {
    private final ValidaService validaService;
    private final MineraService mineraService;

    public RabbitService(ValidaService validaService, MineraService mineraService) {
        this.validaService = validaService;
        this.mineraService = mineraService;

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
    public void msgs(@Payload String msg){
        System.out.println("-=+=".repeat(10)+"\n"+msg+"\n"+"-=+=".repeat(10));
    }

    @RabbitListener(queues = "report")
    public void report(@Payload String report){
        System.out.println(report);
    }

    @RabbitListener(queues = "biancamagro-query")
    public void resultadoQuery(@Payload String resultado){
        System.out.println(resultado);
    }

    @RabbitListener(queues = "clients-errors")
    public void errors(@Payload String error){
        System.out.println("Error: "+error);
    }
}
