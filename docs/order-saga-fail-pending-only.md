# saga 실패 이벤트가 결제 완료 주문을 취소하던 문제 (N2)

## 배경
- `feat/outbox-debezium` E2E 재검증(I1)에서 확정된 주문의 `order.created`를 다시 보냄.
- Redis는 멱등 마커로 재차감하지 않았지만 `inventory.reserved`가 재발행돼 하류 saga가 다시 돎.
- 쿠폰이 이미 사용 중이라 `coupon.apply-failed` 발생 → order가 이를 받아 **CONFIRMED 주문을 CANCELED로 전이**.
- 결과: 결제는 APPROVED로 남고 주문만 취소됨(환불 없음).

## 원인
- `Order.cancel()`은 CANCELED만 막고 CONFIRMED → CANCELED를 허용함.
- 이 메서드를 사용자 취소(REST)와 saga 실패 처리(`OrderEventConsumer.handleOrderFailed`)가 함께 씀.
- 그래서 중복이 아니어도 **늦게 도착한 실패 이벤트 하나**로 결제 완료 주문이 취소될 수 있음. 컨슈머 멱등성과 별개인 도메인 규칙 결함.

## 해결
- saga 실패 전용 전이 `Order.cancelBySagaFailure()` 추가: **PENDING만 CANCELED**, 그 외는 상태 유지 후 false 반환.
- `OrderUseCase.failOrder()` 추가, `handleOrderFailed`는 `cancelOrder` 대신 `failOrder` 호출.
  - CONFIRMED면 취소하지 않고 error 로그 → 수동 확인 대상(하류 보상이 이미 돌았을 수 있음).
  - CANCELED면 warn 로그만 남김(중복 실패 이벤트).
- 사용자 취소(`cancelOrder`/`Order.cancel()`) 동작은 변경 없음.

## 범위 밖
- 중복 메시지로 하류 체인이 재실행되는 문제(N1, Redis 팬텀 복원 포함): 컨슈머 멱등성(`processed_event`)에서 일괄 해결 예정.
- CONFIRMED 주문의 사용자 취소·saga 실패 시 환불 saga: 별도 과제.

## 검증
- `OrderTest`: PENDING → CANCELED, CONFIRMED 유지, CANCELED 유지.
- `OrderUseCaseImplTest`: `failOrder`가 CONFIRMED는 유지, PENDING은 취소.
- `OrderEventConsumerTest`: 실패 이벤트 수신 시 `failOrder` 호출.
- order 모듈 테스트 28개 통과.
