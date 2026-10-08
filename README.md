## API Automation Framework

#### Фреймворк построен на базе:
* Java 17, JUnit 5, RestAssured и WireMock с использованием многомодульной архитектуры.

### Архитектура проекта
Проект разделен на три логических модуля для обеспечения переиспользуемости и чистоты кода:

#### api-core — Ядро проекта. Содержит:
* HTTP клиенты для взаимодействия с тестируемым сервисом.
* Модели данных и константы.
* Утилиты (генерация токенов, конфигурация).
* Настройку отчетности.
* Конфигурацию запуска.
#### api-mock — Модуль эмуляции внешнего окружения. Содержит:
* Настройку WireMock сервера.
* Логику заглушек.
#### api-tests — Модуль с тестами. Содержит:
* Тестовые сценарии.
* JUnit 5 Extension для управления тестами через аннотации.
* Конфигурацию запуска.

### Технологический стек
Java 17, Maven, JUnit 5, RestAssured, WireMock, Allure, Lombok

### Запуск тестов (локально)
#### Предварительные требования
* Установленный JDK 17.
* Установленный Maven.
* Запущенный тестируемый сервис (JAR) на порту 8080.

#### Запуск тестируемого приложения
JAR-файл приложения находится в папке `resources/jars/` проекта
(вне Telegram и других внешних путей). Запуск:
```
java -Dsecret=qazWSXedc -Dmock=http://localhost:8888/ -jar resources/jars/internal-0.0.1-SNAPSHOT.jar
```

#### Команды запуска
* Сборка проекта и запуск тестов: `mvn clean test`
* Параллельный запуск: `-Dparallel=true|false` (по умолчанию true).
* Количество потоков: `-Dparallel.threads=<N>` (по умолчанию 4).
* Повтор упавших тестов: `-Dretry.count=<N>` (N повторных попыток, по умолчанию 1; 0 = без ретраев).
  Либо переменная окружения `TEST_RETRY_COUNT=<N>`.
* При запуске из IDEA параллельность и повторы читаются из `api-tests/src/test/resources/junit-platform.properties`.
* Генерация и открытие Allure отчёта: `allure serve api-tests/target/allure-results`

### Запуск в Docker
* Тестируемое приложение: `docker build -t aqa-app . && docker run -p 8080:8080 aqa-app`
* Полный прогон тестов (приложение + WireMock + `mvn test`) в одном контейнере:
  `docker build -f Dockerfile.test -t aqa-tests .`
  `docker run --rm -v "$PWD/allure-results:/app/api-tests/target/allure-results" aqa-tests`
* Запуск приложения через docker-compose: `docker compose -f docker-compose.yml up`

### Запуск в GitHub (CI)
Файл `.github/workflows/api-tests.yml`:
* На push/PR собирается `Dockerfile.test` и запускаются все тесты.
* Результаты Allure публикуются артефактом и далее генерируется Allure-отчёт
  в ветке `gh-pages` (см. `allure-report` job).
* История запусков сохраняется в `allure-results/history` (хранить в gh-pages),
  поэтому тренд учитывает предыдущие прогоны.
* Ссылка на Allure-страницу выводится в summary прогона Actions.

### Конфигурация окружений (переменные окружения)
`ConfigReader` читает настройки в порядке: системное свойство `-Dkey=...`,
переменная окружения `KEY` (dots → `_`), затем `config.properties`.

| Ключ properties | Env var | Значение по умолчанию |
|---|---|---|
| service.base.url | SERVICE_BASE_URL | http://localhost:8080 |
| service.api.key | SERVICE_API_KEY | qazWSXedc |
| mock.service.port | MOCK_SERVICE_PORT | 8888 |
| token.length | TOKEN_LENGTH | 32 |
| token.regex | TOKEN_REGEX | ^[0-9A-F]{32}$ |

Дополнительные переменные окружения:
* `TEST_RETRY_COUNT` — количество повторных попыток упавших тестов.

Таким образом, в CI достаточно передать эти env-переменные контейнеру,
и перезапускать/перекомпилять проект не потребуется.

### Ретраи тестов и логи в Allure
Добавлен extension `extensions.RetryingExtension` (`@ExtendWith` на `BaseTest`).
При падении тест повторяется `TEST_RETRY_COUNT` раз; для каждой попытки создаётся
Allure-шаг и прикладывается stack-trace в виде attachment `Retry N failure`.

### Allure в CI
В шаге `allure-report` используется `simple-elf/allure-report-action` для
построения отчёта и сохранения истории, далее `peaceiris/actions-gh-pages`
публикует его в ветку `gh-pages`, которая автоматически доступна по адресу:
`https://<owner>.github.io/<repo>/`.
