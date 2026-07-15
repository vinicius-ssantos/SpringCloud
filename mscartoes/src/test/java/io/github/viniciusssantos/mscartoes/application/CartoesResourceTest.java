package io.github.viniciusssantos.mscartoes.application;

import io.github.viniciusssantos.mscartoes.domain.BandeiraCartao;
import io.github.viniciusssantos.mscartoes.domain.Cartao;
import io.github.viniciusssantos.mscartoes.domain.ClienteCartao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartoesResource.class)
class CartoesResourceTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CartaoService cartaoService;

    @MockBean
    private ClienteCartaoService clienteCartaoService;

    @Test
    void cadastra_retorna201ParaCartaoValido() throws Exception {
        var request = "{\"nome\":\"Gold\",\"bandeira\":\"VISA\",\"renda\":1000,\"limite\":500}";

        mvc.perform(post("/cartoes").contentType("application/json").content(request))
                .andExpect(status().isCreated());
    }

    @Test
    void cadastra_retorna400QuandoCamposObrigatoriosFaltam() throws Exception {
        var request = "{\"nome\":\"\",\"bandeira\":null,\"renda\":null,\"limite\":null}";

        mvc.perform(post("/cartoes").contentType("application/json").content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCartoesRendaAteh_retornaCartoesDentroDaRendaInformada() throws Exception {
        var cartao = new Cartao("Basico", BandeiraCartao.VISA, BigDecimal.valueOf(1000), BigDecimal.valueOf(500));
        when(cartaoService.getCartoesRendaMenorIgual(1000L)).thenReturn(List.of(cartao));

        mvc.perform(get("/cartoes").param("renda", "1000"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Basico")));
    }

    @Test
    void getPorId_retorna200QuandoCartaoExiste() throws Exception {
        var cartao = new Cartao("Gold", BandeiraCartao.MASTERCARD, BigDecimal.valueOf(5000), BigDecimal.valueOf(2000));
        when(cartaoService.getPorId(eq(1L))).thenReturn(Optional.of(cartao));

        mvc.perform(get("/cartoes/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Gold")));
    }

    @Test
    void getPorId_retorna404QuandoCartaoNaoExiste() throws Exception {
        when(cartaoService.getPorId(eq(99L))).thenReturn(Optional.empty());

        mvc.perform(get("/cartoes/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCartoesByCliente_retornaCartoesEmitidosParaOCpf() throws Exception {
        var cartao = new Cartao("Gold", BandeiraCartao.MASTERCARD, BigDecimal.valueOf(5000), BigDecimal.valueOf(2000));
        var clienteCartao = new ClienteCartao();
        clienteCartao.setCpf("12345678900");
        clienteCartao.setCartao(cartao);
        clienteCartao.setLimite(BigDecimal.valueOf(1500));
        when(clienteCartaoService.listCartoesByCpf(eq("12345678900"))).thenReturn(List.of(clienteCartao));

        mvc.perform(get("/cartoes").param("cpf", "12345678900"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("1500")));
    }
}
