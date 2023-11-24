package br.ufsm.csi.tpav.pilacoin.service;

import br.ufsm.csi.tpav.pilacoin.util.PilaUtil;
import org.springframework.stereotype.Service;
import java.io.*;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

@Service
public class LoadKeys {
    public LoadKeys() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair keyPair = kpg.generateKeyPair();
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("keypair.ser"))) {
            out.writeObject(keyPair);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        KeyPair kp;
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("keypair.ser"))) {
            kp = (KeyPair) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Erro carregando as chaves");
            return;
        }
        PilaUtil.PRIVATE_KEY = kp.getPrivate();
        PilaUtil.PUBLIC_KEY = kp.getPublic();
    }
}
