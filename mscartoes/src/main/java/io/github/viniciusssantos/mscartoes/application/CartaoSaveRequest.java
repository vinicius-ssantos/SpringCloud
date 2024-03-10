package io.github.viniciusssantos.mscartoes.application;


import io.github.viniciusssantos.mscartoes.domain.BandeiraCartao;
import io.github.viniciusssantos.mscartoes.domain.Cartao;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartaoSaveRequest {
    private String nome;
    private BandeiraCartao bandeira;
    private BigDecimal renda;
    private BigDecimal limite;

    Cartao toModel(){
        return new Cartao(nome,bandeira,renda,limite);

    }

}
