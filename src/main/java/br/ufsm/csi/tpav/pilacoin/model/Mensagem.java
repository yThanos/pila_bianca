package br.ufsm.csi.tpav.pilacoin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mensagem {
    private String msg;
    private String erro;
    private String nomeUsuario;
    private String nonce;
    private String queue;
}
