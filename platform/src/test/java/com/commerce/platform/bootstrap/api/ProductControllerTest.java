package com.commerce.platform.bootstrap.api;

import com.commerce.platform.bootstrap.admin.ProductAdController;
import com.commerce.platform.bootstrap.dto.product.CreateProductRequest;
import com.commerce.platform.bootstrap.exception.GlobalExceptionHandler;
import com.commerce.platform.core.application.port.in.ProductUseCase;
import com.commerce.shared.vo.ProductId;
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

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = {
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
}