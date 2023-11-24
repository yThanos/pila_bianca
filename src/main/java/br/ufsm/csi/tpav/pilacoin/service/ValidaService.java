package br.ufsm.csi.tpav.pilacoin.service;

import br.ufsm.csi.tpav.pilacoin.model.Bloco;
import br.ufsm.csi.tpav.pilacoin.model.BlocoValido;
import br.ufsm.csi.tpav.pilacoin.model.Pilacoin;
import br.ufsm.csi.tpav.pilacoin.model.PilacoinValido;
import br.ufsm.csi.tpav.pilacoin.util.PilaUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
public class ValidaService {

    private final RabbitTemplate rabbitTemplate;

    public ValidaService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @SneakyThrows
    public void validaPila(String pilaStr){
        ObjectMapper objectMapper = new ObjectMapper();
        Pilacoin pila = objectMapper.readValue(pilaStr, Pilacoin.class);
        if (pila.getNomeCriador().equals(PilaUtil.USERNAME)){
            rabbitTemplate.convertAndSend("pila-minerado", pilaStr);
            return;
        }
        System.out.println("Validando pila do(a): "+pila.getNomeCriador());
        BigInteger hash = PilaUtil.geraHash(pilaStr);
        if (hash.compareTo(PilaUtil.DIFFICULTY) < 0){
            System.out.println("Valido!");
            PilacoinValido valido = PilacoinValido.builder().assinatura(PilaUtil.geraAssinatura(pila))
                    .chavePublicaValidador(PilaUtil.PUBLIC_KEY.getEncoded()).nomeValidador(PilaUtil.USERNAME)
                    .pilaCoinJson(pila).build();
            this.rabbitTemplate.convertAndSend("pila-validado", objectMapper.writeValueAsString(valido));
        }
        this.rabbitTemplate.convertAndSend("pila-minerado", pilaStr);
    }

    @SneakyThrows
    public void validaBloco(String blocoStr){
        ObjectMapper objectMapper = new ObjectMapper();
        Bloco bloco = objectMapper.readValue(blocoStr, Bloco.class);
        if(bloco.getNomeUsuarioMinerador().equals(PilaUtil.USERNAME)){
            rabbitTemplate.convertAndSend("bloco-minerado", blocoStr);
            return;
        }
        System.out.println("Validando bloco mienrado pelo(a): "+bloco.getNomeUsuarioMinerador());
        BigInteger hash = PilaUtil.geraHash(blocoStr);
        if (hash.compareTo(PilaUtil.DIFFICULTY) < 0){
            System.out.println("Valido!");
            BlocoValido valido = BlocoValido.builder().assinatura(PilaUtil.geraAssinatura(bloco))
                    .bloco(bloco).chavePublicaValidador(PilaUtil.PUBLIC_KEY.getEncoded())
                    .nomeValidador(PilaUtil.USERNAME).build();
            this.rabbitTemplate.convertAndSend("bloco-validado", objectMapper.writeValueAsString(valido));
        }
        this.rabbitTemplate.convertAndSend("bloco-minerado", blocoStr);
    }
}
