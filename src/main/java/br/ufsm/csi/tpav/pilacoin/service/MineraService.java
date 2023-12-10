package br.ufsm.csi.tpav.pilacoin.service;

import br.ufsm.csi.tpav.pilacoin.model.Bloco;
import br.ufsm.csi.tpav.pilacoin.model.Pilacoin;
import br.ufsm.csi.tpav.pilacoin.repository.PilacoinRepository;
import br.ufsm.csi.tpav.pilacoin.util.PilaUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
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
    @Getter
    private static volatile boolean mining = true;
    @Getter
    private static boolean miningBloco = true;

    public MineraService(RabbitTemplate rabbitTemplate, PilacoinRepository pilacoinRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.pilacoinRepository = pilacoinRepository;
    }

    @PostConstruct
    public void mineraPila(){
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
                while (!mining) {
                    Thread.onSpinWait();
                }
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
                        Pilacoin pila = pilacoin;
                        pila.setStatus("AG_VALIDACAO");
                        pilacoinRepository.save(pila);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();
    }

    public static boolean changeMineState(){
        mining = !mining;
        return mining;
    }

    @SneakyThrows
    public void mineraBloco(String blocoJson) {
        if (!miningBloco){
            System.out.println("Ignorando blocos!");
            rabbitTemplate.convertAndSend("descobre-bloco", blocoJson);
            return;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        Bloco bloco = objectMapper.readValue(blocoJson, Bloco.class);
        bloco.setChaveUsuarioMinerador(PilaUtil.PUBLIC_KEY.getEncoded());
        bloco.setNomeUsuarioMinerador(PilaUtil.USERNAME);
        BigInteger hash;
        int tentativas = 0;
        while (true){
            tentativas++;
            bloco.setNonce(PilaUtil.geraNonce());
            hash = PilaUtil.geraHash(bloco);
            if(hash.compareTo(PilaUtil.DIFFICULTY) < 0){
                System.out.println("Tentativas: "+tentativas);
                System.out.println("Minerou Bloco\n"+hash+"\n"+PilaUtil.DIFFICULTY+"\n"+bloco.getNumeroBloco());
                rabbitTemplate.convertAndSend("bloco-minerado", objectMapper.writeValueAsString(bloco));
                return;
            }
        }
    }

    public static boolean changeMiningBlocoState(){
        miningBloco = !miningBloco;
        return miningBloco;
    }
}
