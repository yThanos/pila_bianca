package br.ufsm.csi.tpav.pilacoin.model;

import lombok.Builder;

@Builder
public class MineState {
    private boolean mineraPila;
    private boolean mineraBloco;
    private boolean validaPila;
    private boolean validaBloco;
}
