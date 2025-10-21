package domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PgProvider {
    // todo 여기서 pg사별 지원하는 결제유형을 관리해야되는지
    //  todo 카드-수기, 인증, sms 위한 값을 만들지 이 부분은 생략할지 CardAuthType
    TOSS("토스"),
    OLIVE_NETWORKS("올리브네트웍스"),
    NHN("NHN"),
    NICE_PAYMENTS("나이스페이먼츠");

    private final String value;

}
