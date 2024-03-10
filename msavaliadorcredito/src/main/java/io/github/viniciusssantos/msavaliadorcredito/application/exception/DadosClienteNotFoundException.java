package io.github.viniciusssantos.msavaliadorcredito.application.exception;

public class DadosClienteNotFoundException extends Exception {
    public DadosClienteNotFoundException(){
        super("Dados do cliente não encontrados para o CPF informado");
    }
}
