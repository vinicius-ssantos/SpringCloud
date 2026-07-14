package io.github.viniciusssantos.msavaliadorcredito.domain.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class DadosAvaliacao {
    @NotNull
    private Long renda;
    @NotBlank
    private String cpf;
}
