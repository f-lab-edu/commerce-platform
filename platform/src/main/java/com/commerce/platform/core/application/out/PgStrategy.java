package com.commerce.platform.core.application.out;

import com.commerce.platform.core.application.in.dto.PayOrderCommand;
import com.commerce.platform.core.domain.enums.PgProvider;

import java.util.Map;

public abstract class PgStrategy {

    public abstract PgProvider getPgProvider();

    /**
     * 라우팅된 pg사에서 해당 결제유형으로 승인 요청
     * @param command
     * @return pg 응답
     */
    public final Map<String, String> processApproval(PayOrderCommand command) {

        return switch (command.payMethod()) {
            case CARD -> getCardPay().approveCard(command);
            case EASY_PAY -> getEasyPay().approveEasyPay(command);
            case PHONE -> getPhonePay().approvePhone(command);
        };
    }

    /**
     * 라우팅된 pg사에서 해당 결제유형으로 취소 요청
     * @param command
     * @return pg 응답
     */
    public final Map<String, String> processCancel(PayOrderCommand command) {
        return switch (command.payMethod()) {
            case CARD -> getCardPay().cancelCard(command);
            case EASY_PAY -> getEasyPay().cancelEasyPay(command);
            case PHONE -> getPhonePay().cancelPhone(command);
        };
    }

    private CardPay getCardPay() {
        if (this instanceof CardPay cardPay) {
            return cardPay;
        }
        throw new UnsupportedOperationException("카드결제 미지원 pg사");
    }

    private PhonePay getPhonePay() {
        if (this instanceof PhonePay phonePay) {
            return phonePay;
        }
        throw new UnsupportedOperationException("휴대폰결제 미지원 pg사");
    }

    private EasyPay getEasyPay() {
        if (this instanceof EasyPay easyPay) {
            return easyPay;
        }
        throw new UnsupportedOperationException("간편결제 미지원 pg사");
    }

}
