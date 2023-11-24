package br.ufsm.csi.tpav.pilacoin.service;

import br.ufsm.csi.tpav.pilacoin.util.PilaUtil;
import org.springframework.stereotype.Service;
import java.io.*;
import java.security.KeyPair;

@Service
public class LoadKeys {
    public LoadKeys() {
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
