package com.commerce.united.controller.web.order;

import com.commerce.united.port.in.OrderUseCase;
import com.commerce.united.port.in.dto.OrderRequest;
import com.commerce.united.port.in.dto.OrderResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderUseCase orderUseCase;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createOrder() throws Exception {
        // given
        String orderId = "ORDER_0001";
        String productId = "P_0001";
        OrderResponse orderResponse = new OrderResponse(orderId, "주문완료");

        // when
        when(orderUseCase.createOrder(any(OrderRequest.class)))
                .thenReturn(orderResponse);

        // then
        OrderRequest orderRequest = new OrderRequest(productId, 3);
        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.status").value("주문완료"));
        ;
    }

    @Test
    void getOrder() {
    }

    @Test
    void cancelOrder() {
    }

    @Test
    void refundOrder() {
    }
}