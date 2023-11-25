package br.ufsm.csi.tpav.pilacoin.service;

import br.ufsm.csi.tpav.pilacoin.model.Bloco;
import br.ufsm.csi.tpav.pilacoin.model.BlocoValido;
import br.ufsm.csi.tpav.pilacoin.model.Pilacoin;
import br.ufsm.csi.tpav.pilacoin.model.PilacoinValido;
import br.ufsm.csi.tpav.pilacoin.util.PilaUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

@Service
public class ValidaService {

    private final RabbitTemplate rabbitTemplate;

    public ValidaService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }


    public void validaPila(String pilaStr){
        ObjectMapper objectMapper = new ObjectMapper();
        Pilacoin pila;
        try {
            pila = objectMapper.readValue(pilaStr, Pilacoin.class);
        } catch (JsonProcessingException e) {
            System.out.println("Pila formato invalido");
            return;
        }
        if (pila.getNomeCriador().equals(PilaUtil.USERNAME)){
            rabbitTemplate.convertAndSend("pila-minerado", pilaStr);
            return;
        }
        System.out.println("Validando pila do(a): "+pila.getNomeCriador());
        BigInteger hash;
        try {
            hash = PilaUtil.geraHash(pilaStr);
        } catch (JsonProcessingException | NoSuchAlgorithmException e) {
            return;
        }
        if (hash.compareTo(PilaUtil.DIFFICULTY) < 0){
            System.out.println("Valido!");
            PilacoinValido valido = PilacoinValido.builder().assinaturaPilaCoin(PilaUtil.geraAssinatura(pila))
                    .chavePublicaValidador(PilaUtil.PUBLIC_KEY.getEncoded()).nomeValidador(PilaUtil.USERNAME)
                    .pilaCoinJson(pila).build();
            try {
                this.rabbitTemplate.convertAndSend("pila-validado", objectMapper.writeValueAsString(valido));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        this.rabbitTemplate.convertAndSend("pila-minerado", pilaStr);
    }


    public void validaBloco(String blocoStr){
        ObjectMapper objectMapper = new ObjectMapper();
        Bloco bloco;
        try {
            bloco = objectMapper.readValue(blocoStr, Bloco.class);
        } catch (JsonProcessingException e) {
            System.out.println("bloco formato ivnalido");
            return;
        }
        if(bloco.getNomeUsuarioMinerador().equals(PilaUtil.USERNAME)){
            rabbitTemplate.convertAndSend("bloco-minerado", blocoStr);
            return;
        }
        System.out.println("Validando bloco mienrado pelo(a): "+bloco.getNomeUsuarioMinerador());
        BigInteger hash;
        try {
            hash = PilaUtil.geraHash(blocoStr);
        } catch (JsonProcessingException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        if (hash.compareTo(PilaUtil.DIFFICULTY) < 0){
            System.out.println("Valido!");
            BlocoValido valido = BlocoValido.builder().assinaturaBloco(PilaUtil.geraAssinatura(bloco))
                    .bloco(bloco).chavePublicaValidador(PilaUtil.PUBLIC_KEY.getEncoded())
                    .nomeValidador(PilaUtil.USERNAME).build();
            try {
                this.rabbitTemplate.convertAndSend("bloco-validado", objectMapper.writeValueAsString(valido));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        this.rabbitTemplate.convertAndSend("bloco-minerado", blocoStr);
    }
}
