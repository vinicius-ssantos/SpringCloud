package io.github.viniciusssantos.mscartoes.application;


import io.github.viniciusssantos.mscartoes.domain.BandeiraCartao;
import io.github.viniciusssantos.mscartoes.domain.Cartao;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class CartaoSaveRequest {
    @NotBlank
    private String nome;
    @NotNull
    private BandeiraCartao bandeira;
    @NotNull
    private BigDecimal renda;
    @NotNull
    private BigDecimal limite;

    Cartao toModel(){
        return new Cartao(nome,bandeira,renda,limite);

    }

}
