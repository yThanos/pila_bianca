package br.ufsm.csi.tpav.pilacoin.service;

import br.ufsm.csi.tpav.pilacoin.model.Bloco;
import br.ufsm.csi.tpav.pilacoin.model.Pilacoin;
import br.ufsm.csi.tpav.pilacoin.repository.PilacoinRepository;
import br.ufsm.csi.tpav.pilacoin.util.PilaUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Service
public class MineraService {
    private final PilacoinRepository pilacoinRepository;
    private final RabbitTemplate rabbitTemplate;

    public MineraService(RabbitTemplate rabbitTemplate, PilacoinRepository pilacoinRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.pilacoinRepository = pilacoinRepository;
    }

    @PostConstruct
    public void main(){
        new Thread(()->{
            Pilacoin pilacoin = Pilacoin.builder().chaveCriador(PilaUtil.PUBLIC_KEY.getEncoded()).
                    nomeCriador(PilaUtil.USERNAME).dataCriacao(new Date()).build();
            ObjectMapper om = new ObjectMapper();
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            int tentativa = 0;
            while (true){
                tentativa++;
                pilacoin.setNonce(PilaUtil.geraNonce());
                BigInteger hash;
                try {
                    hash = new BigInteger(md.digest(om.writeValueAsString(pilacoin).getBytes(StandardCharsets.UTF_8))).abs();
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                if (hash.compareTo(PilaUtil.DIFFICULTY) < 0){
                    System.out.println("===========".repeat(4)+"\nPila Minerado em: "+tentativa+" tentativas\n"+"===========".repeat(4));
                    try {
                        rabbitTemplate.convertAndSend("pila-minerado", om.writeValueAsString(pilacoin));
                        pilacoinRepository.save(pilacoin);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();
    }

    @SneakyThrows
    public void mineraBloco(String blocoJson) {
        ObjectMapper objectMapper = new ObjectMapper();
        Bloco bloco = objectMapper.readValue(blocoJson, Bloco.class);
        bloco.setChaveUsuarioMinerador(PilaUtil.PUBLIC_KEY.getEncoded());
        bloco.setNomeUsuarioMinerador(PilaUtil.USERNAME);
        BigInteger hash;
        while (true){
            bloco.setNonce(PilaUtil.geraNonce());
            hash = PilaUtil.geraHash(bloco);
            if(hash.compareTo(PilaUtil.DIFFICULTY) < 0){
                System.out.println("Minerou Bloco\n"+hash+"\n"+PilaUtil.DIFFICULTY+"\n"+bloco.getNumeroBloco());
                rabbitTemplate.convertAndSend("bloco-minerado", objectMapper.writeValueAsString(bloco));
                return;
            }
        }
    }
}
