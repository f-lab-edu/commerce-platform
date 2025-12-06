package com.commerce.platform.core.application.out;

import com.commerce.platform.core.domain.enums.PgProvider;

public abstract class PgStrategy {

    /**
     * pg사별 요청에 따라 [Card | Easy | Phone]PayService 구현체 실행한다.
     * @param request todo dto
     * @return todo 결재응답dto
     */
    public abstract String process(String request);

    public abstract PgProvider getPgProvider();

}
