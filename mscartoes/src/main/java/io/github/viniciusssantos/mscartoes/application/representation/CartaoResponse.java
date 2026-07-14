package io.github.viniciusssantos.mscartoes.application.representation;

import io.github.viniciusssantos.mscartoes.domain.Cartao;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartaoResponse {

    private Long id;
    private String nome;
    private String bandeira;
    private BigDecimal renda;
    private BigDecimal limiteBasico;

    public static CartaoResponse fromModel(Cartao cartao) {
        return new CartaoResponse(
                cartao.getId(),
                cartao.getNome(),
                cartao.getBandeira().toString(),
                cartao.getRenda(),
                cartao.getLimiteBasico()
        );
    }
}
