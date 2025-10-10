## ✨ Log Level Usage Guide (for Production-Grade Applications)   
####  Last Modified 25.04.28


### ⚠️ Log Level Policy

```text
FATAL - 사용하지 않습니다. FATAL은 개발 자체가 잘못된 경우를 의미합니다.
ERROR - 처리 가능한 예외 포함, 미처 처리하지 못한 예외를 출력합니다.
WARN  - 사용자의 잘못으로 인해 발생한 예외 상황입니다.
INFO  - 정상적인 흐름 중 중요한 비즈니스 이벤트를 출력합니다.
        비즈니스 메서드당 2~3개 이하로 제한합니다.
DEBUG - 프로덕션 환경에서는 출력하지 않습니다. 개발자가 흐름을 이해하기 위한 레벨입니다.
TRACE - 모든 내부 흐름을 추적하는 가장 상세한 로깅 레벨입니다.
```

---

### 🔢 Log Format Rule

> **비즈니스 로직 중심의 로그 추적**, **흐름 단위 상태 구분**, **핵심 정보 노출**, **일관된 분석**을 위한 로그 포맷입니다.

#### ▶️ Parameters

| 파라미터        | 설명                                                                                                                                                                             |
|-------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `action`    | 로그의 주된 행동을 나타냅니다. 하나의 비즈니스 레이어 단위 작업을 의미합니다.  <br> 예: 회원가입 메서드 호출 시 `"UserRegistration"`                                                                                       |
| `status`    | 로그 상태를 나타내며, 비즈니스 또는 프레젠테이션 레이어에서 프로세스의 시작/종료/예외 상태를 표현합니다. <br> 예: `"RequestReceived"`, `"RequestCompleted"`, 또는 예외 클래스 이름                                                    |
| `detail`    | 로그 상세 내용입니다. 핵심 정보(예: Email, ID 등)를 표시합니다. 단, Password, Token 등 민감한 정보는 제외합니다. <br/>에러 발생 시 에러 메세지를 출력합니다. (예: `Please enter a valid email address`) <br> 예: `Email`,`Profile` |
| `className` | 로그가 기록된 클래스명을 나타냅니다.                                                                                                                                                         
| `method`    | HTTP 메서드 종류를 나타냅니다. (ex. POST, GET)                                                                                                                                
| `uri`       | 요청된 URI 경로를 나타냅니다. (ex. /api/v1/register)                                                                                                                                                  
| `code`      | HTTP 응답 코드입니다.  (ex. 200, 400)                                                                                                                                                      
| `value`     | 로그에 포함할 주요 값입니다. (예: "ia****@gmail.com")                                                                                                                                           

#### ▶️ Return

- 지정된 형식으로 포맷된 로그 메시지를 반환합니다.

```java
 public String formatMessage() {
    return
            String.format("[%s] ", className) +
                    String.format("%s ", action) +
                    String.format("%s ", status) +
                    String.format("%s", detail) +
                    String.format("(%s)", value) +
                    " - \"" +
                    String.format("%-4s ", method) +
                    String.format("%s ", uri) +
                    String.format("%s", code) +
                    "\"";
}
```

---

### 🔍 Example
- `LogMessage`클래스는 로그 메세지에 필요한 필드를 선언하며, 최종 출력용 `formatMessage()` 메서드를 제공합니다.
- `LogStatus`Enum 은 `status` 파라미터에 사용될 값을 관리합니다. Error/WARN의 경우 별도로 관리되는 `ErrorCode`Enum 을 사용합니다.
- `LogUtil`클래스는 로그 메세지의 `value`에 대해 민감한 정보를 마스킹 하는 `maskValue()` 메서드를 제공합니다.
  - `formatMessage()` 메서드는 더 이상 사용되지 않으며, 현재는 `LogMessage` 클래스 내부에서 처리합니다.
  
- `LogFactory`클래스는 LogUtil에 의존하며 Bean으로 등록되어 있습니다. 동작 원리는 다음과 같습니다.
  - 1, `of()`메서드를 통해 `LogBuilder` 인스턴스를 생성합니다.
  - 2, `of()`메서드의 파라미터로 기본 로그 정보를 채웁니다.
  - 3, 이후 메서드 체이닝 방식으로 추가 정보를 등록하고, `build()` 호출 시 최종 로그 포맷을 완성합니다.

```java
log.info(logFactory.of(request, response, logContext, className).status(LogStatus.OK).value(userId).build());
```

Output:
```
2025-04-28 09:56:41.027 WARN  [Request-ID: a22f06b1-a872-499b-8e90-918898e747f0] [RegisterController] UserRegistration INVALID_PASSWORD_ERROR InvalidFormatException(1ma******) - "POST /api/v1/register 422"
2025-04-28 11:14:05.567 INFO  [Request-ID: 24a5053e-faa6-41e1-a84d-a046b53c4b4a] [RegisterController] UserRegistration SUCCESS UserId(a19*****@0aco.com) - "POST /api/v1/register 200"
```

---

### 🏆 운영 레벨 권장 설정

- **개발 환경**: TRACE, DEBUG, INFO, WARN, ERROR
- **운영 환경**: INFO, WARN, ERROR (DEBUG 이하는 비활성화)

> 슬랙 알림, APM 연동은 WARN/ERROR 수준으로 필터링

---

### 🚀 Tips

- 로그 남발은 분석을 어렵게 만듭니다. 목적을 갖고 작성합니다.
- 로그 메시지는 사람이 읽고 이해할 수 있게 작성합니다.
- 개인정보 포함 시 마스킹 필수!
- 비즈니스 단위별 ACTION 명세 문서화 추천!

