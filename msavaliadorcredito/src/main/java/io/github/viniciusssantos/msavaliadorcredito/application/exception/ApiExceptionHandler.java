package io.github.viniciusssantos.msavaliadorcredito.application.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler {

    @ExceptionHandler(DadosClienteNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(DadosClienteNotFoundException e) {
        return errorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ErrosComunicacaoMicroservicoException.class)
    public ResponseEntity<Map<String, Object>> handleComunicacao(ErrosComunicacaoMicroservicoException e) {
        log.error("Falha de comunicacao com microservico downstream", e);
        HttpStatus status = HttpStatus.resolve(e.getStatus());
        if (status == null) {
            status = HttpStatus.BAD_GATEWAY;
        }
        return errorResponse(status, "Falha ao comunicar com um servico dependente");
    }

    @ExceptionHandler(ErroSolicitacaoCartaoException.class)
    public ResponseEntity<Map<String, Object>> handleSolicitacaoCartao(ErroSolicitacaoCartaoException e) {
        log.error("Falha ao solicitar emissao de cartao", e);
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Nao foi possivel processar a solicitacao de emissao de cartao");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception e) {
        log.error("Erro nao tratado", e);
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado");
    }

    private ResponseEntity<Map<String, Object>> errorResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "message", message
        ));
    }
}
