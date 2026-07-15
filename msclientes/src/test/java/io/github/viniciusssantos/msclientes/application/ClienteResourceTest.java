package io.github.viniciusssantos.msclientes.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.viniciusssantos.msclientes.domain.Cliente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteResource.class)
class ClienteResourceTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClienteService service;

    @Test
    void save_retorna201ComLocationApontandoParaOCpfCadastrado() throws Exception {
        var request = "{\"cpf\":\"12345678900\",\"nome\":\"Fulano\",\"idade\":30}";

        mvc.perform(post("/clientes").contentType("application/json").content(request))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("cpf=12345678900")));
    }

    @Test
    void save_retorna400QuandoCamposObrigatoriosFaltam() throws Exception {
        var request = "{\"cpf\":\"\",\"nome\":\"\",\"idade\":null}";

        mvc.perform(post("/clientes").contentType("application/json").content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void save_retorna409QuandoCpfJaCadastrado() throws Exception {
        when(service.save(any(Cliente.class))).thenThrow(new DataIntegrityViolationException("cpf duplicado"));
        var request = "{\"cpf\":\"12345678900\",\"nome\":\"Fulano\",\"idade\":30}";

        mvc.perform(post("/clientes").contentType("application/json").content(request))
                .andExpect(status().isConflict())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Já existe um cliente")));
    }

    @Test
    void dadosCliente_retorna200ComClienteQuandoCpfExiste() throws Exception {
        var cliente = new Cliente("12345678900", "Fulano", 30);
        when(service.getByCpf(eq("12345678900"))).thenReturn(Optional.of(cliente));

        mvc.perform(get("/clientes").param("cpf", "12345678900"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Fulano")));
    }

    @Test
    void dadosCliente_retorna404QuandoCpfNaoExiste() throws Exception {
        when(service.getByCpf(eq("00000000000"))).thenReturn(Optional.empty());

        mvc.perform(get("/clientes").param("cpf", "00000000000"))
                .andExpect(status().isNotFound());
    }
}
