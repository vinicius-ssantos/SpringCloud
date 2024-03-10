package io.github.viniciusssantos.msclientes.application.representation;


import ch.qos.logback.core.net.server.Client;
import io.github.viniciusssantos.msclientes.domain.Cliente;
import lombok.Data;

@Data
public class ClienteSaveRequest {
    private String cpf;
    private String nome;
    private Integer idade;

    public Cliente toModel() {
        return new Cliente(cpf, nome, idade);
    }
}
