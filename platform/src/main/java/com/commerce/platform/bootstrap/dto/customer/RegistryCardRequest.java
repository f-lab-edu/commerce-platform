package com.commerce.platform.bootstrap.dto.customer;

import com.commerce.platform.core.application.in.dto.RegistryCardCommand;
import com.commerce.platform.core.domain.enums.PayProvider;
import com.commerce.platform.core.domain.vo.CustomerId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistryCardRequest(
        String customerId,

        @NotBlank(message = "카드사를 선택해주세요.")
        String payProvider,

        @NotBlank(message = "카드번호를 입력해주세요")
        @Pattern(regexp = "^[0-9]+$", message = "숫자로만 구성되어야 합니다.")
        @Size(min = 16, max = 20)
        String cardNumber,

        @NotBlank(message = "비밀번호를 입력해주세요")
        @Pattern(regexp = "^[0-9]{2}$", message = "비밀번호 앞 2자리")
        String password,

        @Pattern(regexp = "^(0[1-9]|1[0-2])$", message = "01~12 사이의 숫자여야 합니다.")
        String expiryMonth,

        @Pattern(regexp = "^[0-9]{2}$", message = "년도 숫자 2자리")
        String expiryYear,

        @Pattern(
                regexp = "^(\\d{2})(0[1-9]|1[0-2])(0[1-9]|[12][0-9]|3[01])$",
                message = "생년월일을 확인하세요"
        )
        String birthDate,

        @NotBlank
        @Size(min = 1, max = 20)
        String cardNickName
) {
        public RegistryCardCommand to() {
                return new RegistryCardCommand(
                        CustomerId.of(this.customerId),
                        PayProvider.getPayProviderByValue(this.payProvider),
                        this.cardNumber,
                        this.password,
                        this.expiryMonth,
                        this.expiryYear,
                        this.birthDate,
                        this.cardNickName
                );
        }
}
