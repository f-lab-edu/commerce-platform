package domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PayMethod {
    CARD("카드결제"),
    PHONE("휴대폰결제"),
    EASY_PAY("간편결제");

    private final String value;
}
