package br.ufsm.csi.tpav.pilacoin.service;

import br.ufsm.csi.tpav.pilacoin.model.Bloco;
import br.ufsm.csi.tpav.pilacoin.model.BlocoValido;
import br.ufsm.csi.tpav.pilacoin.model.Pilacoin;
import br.ufsm.csi.tpav.pilacoin.model.PilacoinValido;
import br.ufsm.csi.tpav.pilacoin.util.PilaUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.Getter;
import java.util.ArrayList;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

@Service
public class ValidaService {

    private final RabbitTemplate rabbitTemplate;
    @Getter
    private static boolean validating = true;
    @Getter
    private static boolean validatingBloco = true;
    private final List<String> ignoreList = new ArrayList<>();

    public ValidaService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }


    public void validaPila(String pilaStr){
        System.out.println(pilaStr);
        if (ignoreList.contains(pilaStr)) {
            rabbitTemplate.convertAndSend("pila-minerado", pilaStr);
            System.out.println("[PILA] Ja tentei validar ou validei");
            return;
        }
        ignoreList.add(pilaStr);
        if (!validating){
            System.out.println("Ignorando validação de pilas");
            rabbitTemplate.convertAndSend("pila-minerado", pilaStr);
            return;
        }
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

    public static boolean changeValidatingState(){
        validating = !validating;
        return validating;
    }


    public void validaBloco(String blocoStr) {
        if (ignoreList.contains(blocoStr)) {
            rabbitTemplate.convertAndSend("bloco-minerado", blocoStr);
            System.out.println("[BLOCO] Ja tentei validar ou validei");
            return;
        }
        ignoreList.add(blocoStr);
        if (!validatingBloco){
            System.out.println("Ignorando validação de blocos");
            rabbitTemplate.convertAndSend("bloco-minerado", blocoStr);
            return;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        Bloco bloco;
        try {
            bloco = objectMapper.readValue(blocoStr, Bloco.class);
        } catch (JsonProcessingException e) {
            System.out.println(blocoStr);
            System.out.println("bloco formato ivnalido");
            return;
        }
        if (bloco.getNomeUsuarioMinerador() == null || bloco.getNomeUsuarioMinerador().equals(PilaUtil.USERNAME)) {
            System.out.println("Ignora meu bloco\n" + "XXXXXXXXXX".repeat(4));
            rabbitTemplate.convertAndSend("bloco-minerado", blocoStr);
            return;
        }
        System.out.println("Validando bloco mienrado pelo(a): " + bloco.getNomeUsuarioMinerador());
        BigInteger hash;
        try {
            hash = PilaUtil.geraHash(blocoStr);
        } catch (JsonProcessingException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        System.out.println(blocoStr);
        System.out.println(hash);
        System.out.println(PilaUtil.DIFFICULTY);
        System.out.println("Numero do bloco: " + bloco.getNumeroBloco());
        if (hash.compareTo(PilaUtil.DIFFICULTY) < 0) {
            System.out.println("Valido!");
            BlocoValido valido = BlocoValido.builder().assinaturaBloco(PilaUtil.geraAssinatura(bloco))
                    .bloco(bloco).chavePublicaValidador(PilaUtil.PUBLIC_KEY.getEncoded())
                    .nomeValidador(PilaUtil.USERNAME).build();
            try {
                ObjectWriter objectWriter = objectMapper.writer();
                System.out.println(objectWriter.writeValueAsString(valido));
                System.out.println(objectMapper.writeValueAsString(valido));
                rabbitTemplate.convertAndSend("bloco-validado", objectMapper.writeValueAsString(valido));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("Bloco invalido");
            rabbitTemplate.convertAndSend("bloco-minerado", blocoStr);
        }
    }

    public static boolean changeValidatingBlocoState(){
        validatingBloco = !validatingBloco;
        return validatingBloco;
    }
}
