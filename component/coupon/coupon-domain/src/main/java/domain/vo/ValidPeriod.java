package domain.vo;

import java.time.LocalDate;

public class ValidPeriod {
    LocalDate frDt;
    LocalDate toDt;

    public ValidPeriod(LocalDate frDt, LocalDate toDt) throws Exception {
        if(LocalDate.now().isBefore(frDt)) throw new Exception("적용 시작일자 확인");
        else if(toDt.isBefore(frDt)) throw new Exception("적용 종료일자 확인");

        this.frDt = frDt;
        this.toDt = toDt;
    }

    public boolean checkNowInPeriod() {
        LocalDate now = LocalDate.now();
        return !now.isBefore(this.frDt) && !now.isAfter(this.toDt);
    }
}
