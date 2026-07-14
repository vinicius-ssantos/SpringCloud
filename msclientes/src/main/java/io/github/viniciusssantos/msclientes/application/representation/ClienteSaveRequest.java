package io.github.viniciusssantos.msclientes.application.representation;


import io.github.viniciusssantos.msclientes.domain.Cliente;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ClienteSaveRequest {
    @NotBlank
    private String cpf;
    @NotBlank
    private String nome;
    @NotNull
    private Integer idade;

    public Cliente toModel() {
        return new Cliente(cpf, nome, idade);
    }
}
