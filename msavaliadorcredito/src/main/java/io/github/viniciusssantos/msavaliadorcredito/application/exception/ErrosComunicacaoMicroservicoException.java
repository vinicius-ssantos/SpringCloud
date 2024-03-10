package io.github.viniciusssantos.msavaliadorcredito.application.exception;

import lombok.Getter;

public class ErrosComunicacaoMicroservicoException extends Exception{
    @Getter
    private Integer status;

    public ErrosComunicacaoMicroservicoException(String msg,Integer status){
        super(msg);
        this.status = status;
    }
}
