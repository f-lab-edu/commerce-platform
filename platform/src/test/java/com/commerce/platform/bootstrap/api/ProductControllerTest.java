package com.commerce.platform.bootstrap.api;

import com.commerce.platform.bootstrap.admin.ProductAdController;
import com.commerce.platform.bootstrap.dto.product.CreateProductRequest;
import com.commerce.platform.bootstrap.dto.product.UpdateStockRequest;
import com.commerce.platform.bootstrap.exception.GlobalExceptionHandler;
import com.commerce.platform.core.application.in.ProductUseCase;
import com.commerce.platform.core.domain.aggreate.Product;
import com.commerce.platform.core.domain.enums.StockOperation;
import com.commerce.platform.core.domain.vo.ProductId;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static com.commerce.platform.shared.exception.BusinessError.INVALID_REQUEST_VALUE;
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

    List<Product> productList;
    List<CreateProductRequest> requestList;

    @BeforeEach
    void setUp() {
        CreateProductRequest req = null;
        Product product = null;
        productList = new ArrayList<>();
        requestList = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            req = new CreateProductRequest("테스트상품_" + i,
                    "이 상품은 테스트용 입니다.",
                    1004L,
                    5000L);

            product = CreateProductRequest.to(req);
            requestList.add(req);
            productList.add(product);
        }
    }

    @DisplayName("상품 등록 성공")
    @Test
    void createProduct() throws Exception {
        ProductId expectedId = productList.get(0).getProductId();
        CreateProductRequest req = requestList.get(0);

        given(productUseCase.createProduct(any()))
                .willReturn(expectedId);

        mockMvc.perform(post("/admin/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(expectedId.getId())));

        verify(productUseCase).createProduct(any());
    }

    @DisplayName("재고 수정 성공")
    @Test
    void updateStock() throws Exception {
        Product givenProduct = productList.get(0);
        UpdateStockRequest stockRequest = new UpdateStockRequest(100L, StockOperation.SET);

        given(productUseCase.updateStock(any()))
                .willReturn(givenProduct);

        mockMvc.perform(patch("/admin/products/{productId}/stock", givenProduct.getProductId().getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(stockRequest)))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(content().string(containsString(String.valueOf(givenProduct.getStockQuantity().getValue()))));

        verify(productUseCase).updateStock(any());
    }

    @DisplayName("재고 수정 실패 - Validation 오류")
    @Test
    void failedUpdateStockO() throws Exception {
        Product givenProduct = productList.get(0);
        UpdateStockRequest stockRequest = new UpdateStockRequest(-1, null);

        mockMvc.perform(patch("/admin/products/{productId}/stock", givenProduct.getProductId().getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stockRequest)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.code").value(INVALID_REQUEST_VALUE.getCode()));
    }
}