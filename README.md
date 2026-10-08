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

### Запуск тестов
#### Предварительные требования
* Установленный JDK 17.
* Установленный Maven.
* Запущенный тестируемый сервис (JAR) на порту 8080. JAR находится в `resources/jars/`.

#### Команды запуска
* Сборка проекта и запуск тестов: mvn clean test
* Можно указать свойство -Dparallel=true/false. Если его не будет, 
то берется значение из pom.xml -> properties.parallel(true)
* Если запускается из IDEA, то для параллельного запуска тесты
смотрят на значение из junit-platform.properties
* Генерация и открытие Allure отчета: allure serve api-tests/target/allure-results

### Запуск в Docker
* Тестируемое приложение в Docker: `docker build -t aqa-app . && docker run -p 8080:8080 aqa-app`
* Полный прогон тестов в Docker (приложение + WireMock + mvn test):
  `docker build -f Dockerfile.test -t aqa-tests .` и
  `docker run --rm -v "$PWD/allure-results:/app/api-tests/target/allure-results" aqa-tests`
* Запуск тестов через docker-compose (локально): `docker compose -f docker-compose.yml up`

### Запуск в GitHub
CI-прогон настроен в `.github/workflows/api-tests.yml`: на каждый push/PR
собирается Docker-образ `Dockerfile.test` и запускаются все тесты.
Результаты Allure прикрепляются как артефакт сборки.

### Конфигурация окружений
`ConfigReader` читает настройки в порядке: системное свойство `-Dkey=...`,
переменная окружения `KEY` (dots -> underscore), затем `config.properties`.
Поэтому URL сервиса, ключ API, порт мока и регламентированные значения можно
переопределить без перекомпиляции.