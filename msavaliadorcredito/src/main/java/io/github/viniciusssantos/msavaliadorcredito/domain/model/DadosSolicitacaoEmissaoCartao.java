package io.github.viniciusssantos.msavaliadorcredito.domain.model;


import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class DadosSolicitacaoEmissaoCartao {
    @NotNull
    private Long idCartao;
    @NotBlank
    private String cpf;
    @NotBlank
    private String endereco;
    // Não validado como obrigatório: o valor é sempre recalculado no servidor
    // em solicitarEmissaoDeCartao, nunca aceito do chamador.
    private BigDecimal limiteLiberado;
}
