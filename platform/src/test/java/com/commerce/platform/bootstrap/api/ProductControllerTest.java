package com.commerce.platform.bootstrap.api;

import com.commerce.platform.bootstrap.admin.ProductAdController;
import com.commerce.platform.bootstrap.dto.product.CreateProductRequest;
import com.commerce.platform.bootstrap.dto.product.UpdateStockRequest;
import com.commerce.platform.bootstrap.exception.GlobalExceptionHandler;
import com.commerce.platform.core.application.port.in.ProductUseCase;
import com.commerce.platform.core.domain.aggreate.Product;
import com.commerce.platform.core.domain.enums.StockOperation;
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

import static com.commerce.shared.exception.BusinessError.INVALID_REQUEST_VALUE;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = {
//                ProductController.class,
                ProductAdController.class
        },
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalExceptionHandler.class
        )
)
class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    ProductUseCase productUseCase;

    @DisplayName("상품 등록 성공")
    @Test
    void createProduct() throws Exception {
        ProductId expectedId = ProductId.create();
        CreateProductRequest req = new CreateProductRequest("테스트상품_",
                "이 상품은 테스트용 입니다.",
                1004L,
                5000L);

        given(productUseCase.createProduct(any()))
                .willReturn(expectedId);

        mockMvc.perform(post("/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(expectedId.id())));

        verify(productUseCase).createProduct(any());
    }

    @DisplayName("재고 수정 성공")
    @Test
    void updateStock() throws Exception {
        Product givenProduct = Product.builder()
                .productId(ProductId.create())
                .stockQuantity(Quantity.create(100))
                .build();

        UpdateStockRequest stockRequest = new UpdateStockRequest(10, StockOperation.SET);

        given(productUseCase.updateStock(any()))
                .willReturn(Quantity.create(10));

        mockMvc.perform(patch("/admin/products/{productId}/stock", givenProduct.getProductId().id())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(stockRequest)))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(content().string(containsString(String.valueOf(stockRequest.quantity()))));

        verify(productUseCase).updateStock(any());
    }

    @DisplayName("재고 수정 실패 - Validation 오류")
    @Test
    void failedUpdateStockO() throws Exception {
        Product givenProduct = Product.builder()
                .productId(ProductId.create())
                .stockQuantity(Quantity.create(100))
                .build();

        UpdateStockRequest stockRequest = new UpdateStockRequest(-1, null);

        mockMvc.perform(patch("/admin/products/{productId}/stock", givenProduct.getProductId().id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stockRequest)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.code").value(INVALID_REQUEST_VALUE.getCode()));
    }
}