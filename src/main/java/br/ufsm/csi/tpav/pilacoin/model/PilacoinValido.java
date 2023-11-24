package br.ufsm.csi.tpav.pilacoin.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class PilacoinValido {
    private String nomeValidador;
    private byte[] chavePublicaValidador;
    private byte[] assinatura;
    private Pilacoin pilaCoinJson;
}
