package com.commerce.inventory.bootstrap;

import com.commerce.inventory.bootstrap.dto.UpdateStockRequest;
import com.commerce.inventory.bootstrap.exception.GlobalExceptionHandler;
import com.commerce.inventory.core.application.port.in.InventoryUseCase;
import com.commerce.inventory.core.domain.aggregate.Inventory;
import com.commerce.inventory.core.domain.enums.StockOperation;
import com.commerce.shared.vo.ProductId;
import com.commerce.shared.vo.Quantity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static com.commerce.shared.exception.BusinessError.INVALID_QUANTITY;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = InventoryController.class,
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class
        )
)
class InventoryControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    InventoryUseCase inventoryUseCase;

    @DisplayName("재고 조회 성공")
    @Test
    void getStock() throws Exception {
        ProductId productId = ProductId.create();
        Inventory inventory = Inventory.builder()
                .productId(productId)
                .quantity(Quantity.create(100))
                .updatedAt(LocalDateTime.now())
                .build();

        given(inventoryUseCase.getStock(any()))
                .willReturn(inventory);

        mockMvc.perform(get("/inventory/{productId}", productId.id()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("100")));

        verify(inventoryUseCase).getStock(any());
    }

    @DisplayName("재고 수정 성공 - SET")
    @Test
    void updateStock() throws Exception {
        ProductId productId = ProductId.create();
        UpdateStockRequest stockRequest = new UpdateStockRequest(Quantity.create(10), StockOperation.SET);

        Inventory inventory = Inventory.builder()
                .productId(productId)
                .quantity(Quantity.create(10))
                .updatedAt(LocalDateTime.now())
                .build();

        given(inventoryUseCase.getStock(any()))
                .willReturn(inventory);

        mockMvc.perform(patch("/inventory/{productId}/stock", productId.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stockRequest)))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().string(containsString("10")));

        verify(inventoryUseCase).setStock(any(), any());
    }

    @DisplayName("재고 수정 실패 - operation null")
    @Test
    void failedUpdateStock() throws Exception {
        ProductId productId = ProductId.create();
        String invalidJson = """
                {"quantity": {"value": -1}, "operation": "increase"}
                """;

        mockMvc.perform(patch("/inventory/{productId}/stock", productId.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.code").value(INVALID_QUANTITY.getCode()));
    }
}