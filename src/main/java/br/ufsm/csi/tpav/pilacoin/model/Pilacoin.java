package br.ufsm.csi.tpav.pilacoin.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
@Entity
@Table(name = "pilacoin")
public class Pilacoin {
    @Id
    @Column(name = "nonce",unique = true)
    private String nonce;
    @Column(name = "nome_criador")
    private String nomeCriador;
    @Column(name = "chave_criador")
    private byte[] chaveCriador;
    @Column(name = "data_criacao")
    private Date dataCriacao;
}
