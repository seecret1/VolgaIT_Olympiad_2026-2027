# VolgaIT — UI-автотесты полуфинала

Проект автоматизирует проверки учебного сайта [Practice Automation](https://practice-automation.com/) по полуфинальному заданию дисциплины «Автоматизация тестирования (Java, Python)».

Покрыты страницы:

- [Calendars](https://practice-automation.com/calendars/) — 10 позитивных и 4 негативных сценария;
- [Modals](https://practice-automation.com/modals/) — 9 позитивных и 4 негативных сценария;
- [Ads](https://practice-automation.com/ads/) — 8 позитивных и 3 негативных сценария;
- [Form Fields](https://practice-automation.com/form-fields/) — отдельный обязательный сценарий переноса списка Automation Tools в Message средствами Selenium.
- общая навигация Calendars, Modals и Ads — переходы по ссылкам Blog, Home и YouTube (9 параметризованных проверок).

Всего: **48 UI-автотестов** и **7 проверок фабрики браузеров**. Каждый параметр параметризованного теста отображается в JUnit и Allure как отдельный запуск.

## Стек

- Java 17;
- Selenium 4;
- JUnit 5;
- Maven;
- Allure;
- Chrome, Firefox, Edge и Safari через Selenium Manager / Selenium Grid.

## Архитектура

В проекте осознанно сочетаются несколько паттернов:

- **Page Object** — `CalendarPage`, `ModalsPage`, `AdsPage`, `FormFieldsPage` скрывают локаторы и действия страниц; элементы объявлены через Selenium `@FindBy` и инициализируются `PageFactory`;
- **Component Object** — `PopupComponent`, `ContactFormComponent` и `NavigationComponent` моделируют переиспользуемые части интерфейса;
- **Factory** — `DriverFactory` централизованно создаёт Chrome, Firefox, Edge и Safari с едиными настройками;
- **Builder** — `ContactData.Builder` создаёт читаемые тестовые данные формы;
- **JUnit Extension** — `ScreenshotExtension` прикладывает скриншот, HTML страницы и причину к Allure до закрытия браузера.
- **External Configuration** — URL, параметры запуска, вводимые данные и ожидаемые значения хранятся в `application.conf` и читаются через `ConfigProvider`.

Не используются неявные ожидания и `Thread.sleep`. Все синхронизации основаны на явных ожиданиях и наблюдаемом состоянии DOM.

Локаторы объявлены через `@FindBy(className = ...)` и `@FindBy(xpath = ...)`. XPath проверяет полные имена CSS-классов и ограничивает поиск нужным модальным окном. Общие строки разделены на `DomAttributes`, `RuntimeKeys` и `TestMetadata`; поля контактной формы представлены enum `ContactField`. URL и тестовые данные остаются в `application.conf`.

```text
src/test/java/io/github/seecret1/volgait
├── components   # Component Objects
├── config       # параметры запуска
├── driver       # BrowserType и DriverFactory
├── extensions   # артефакты при падении
├── model        # тестовые данные и Builder
├── pages        # Page Objects
└── tests        # JUnit 5 + Allure сценарии
```

## Быстрый запуск

Нужны Java 17+ и установленный выбранный браузер. Драйвер вручную скачивать не требуется — его подберёт Selenium Manager.

Windows:

```powershell
.\mvnw.cmd clean test
```

Linux/macOS:

```bash
./mvnw clean test
```

По умолчанию параметры берутся из `src/test/resources/application.conf`. Системные параметры позволяют переопределить настройки запуска:

```powershell
.\mvnw.cmd clean test "-Dbrowser=firefox"
.\mvnw.cmd clean test "-Dheadless=false"
.\mvnw.cmd clean test "-Dtimeout=20"
.\mvnw.cmd clean test `
  "-Djunit.jupiter.execution.parallel.config.fixed.parallelism=6" `
  "-Djunit.jupiter.execution.parallel.config.fixed.max-pool-size=6"
```

| Параметр | По умолчанию | Назначение |
|---|---:|---|
| `browser` | `chrome` | `chrome`, `firefox`, `edge` или `safari` |
| `remoteUrl` | пусто | URL Selenium Grid; пустое значение означает локальный запуск |
| `browserBinary` | пусто | путь к браузеру Chrome/Firefox/Edge (на узле Grid при удалённом запуске) |
| `headless` | `true` | запуск без окна браузера |
| `timeout` | `15` | ожидание элементов, секунды |
| `pageLoadTimeout` | `40` | ожидание загрузки страницы, секунды |

JUnit 5 запускает тестовые классы и отдельные сценарии параллельно в фиксированном пуле из 6 потоков. Каждый сценарий получает собственный экземпляр WebDriver, поэтому браузерные сессии изолированы друг от друга. Размер пула можно переопределить системным параметром `junit.jupiter.execution.parallel.config.fixed.parallelism`; при изменении также задайте такое же значение для `junit.jupiter.execution.parallel.config.fixed.max-pool-size`.

## Allure

Для проверки в нескольких браузерах выполните отдельный прогон для каждого:

```powershell
foreach ($browser in 'chrome', 'firefox', 'edge') {
    .\mvnw.cmd test "-Dbrowser=$browser"
    if ($LASTEXITCODE -ne 0) { throw "Tests failed: $browser" }
}
```

Каждый запуск выполняет весь набор в выбранном браузере. Allure сохраняет результаты всех запусков до `clean`.
Safari запускается на macOS с включённым Remote Automation (`safaridriver --enable`), с `-Dbrowser=safari -Dheadless=false`.
Для удалённых браузеров добавьте `-DremoteUrl=http://localhost:4444`; браузер должен быть доступен на соответствующем узле Grid.
Параметр `browserBinary` позволяет использовать совместимый Chromium-браузер через режим `chrome`, но совместимость его версии с ChromeDriver нужно проверять отдельно.
Dockerfile собирает образ с Chrome или Firefox; Edge и Safari доступны на хосте либо через Grid.

## Отчёт

Результаты создаются в `target/allure-results`. Для локального просмотра:

```powershell
.\mvnw.cmd allure:serve
```

Для статического отчёта:

```powershell
.\mvnw.cmd allure:report
```

В отчёте есть Epic/Feature/Story, severity, позитивные/негативные теги, понятные названия сценариев и шаги Page/Component Objects. При любой ошибке автоматически прикладываются screenshot, page source и текст исключения.

## Docker

Образ включает Java 17, Maven и выбранный браузер с драйвером. Зависимости Maven кэшируются на отдельном слое сборки, а контейнер по умолчанию запускает полный набор тестов в headless Chrome.

```bash
docker build -t volgait-ui-tests .
docker run --rm --shm-size=2g -v "${PWD}/target:/workspace/target" volgait-ui-tests
```

Для Firefox образ собирается тем же Dockerfile:

```bash
docker build --build-arg BROWSER=firefox -t volgait-ui-tests:firefox .
docker run --rm --shm-size=2g volgait-ui-tests:firefox
```

Для запуска конкретного набора или передачи дополнительных параметров замените команду контейнера:

```bash
docker run --rm --shm-size=2g volgait-ui-tests \
  mvn --batch-mode test -Dgroups=positive \
  -Djunit.jupiter.execution.parallel.config.fixed.parallelism=6 \
  -Djunit.jupiter.execution.parallel.config.fixed.max-pool-size=6
```

После запуска Allure results и Surefire reports доступны в локальном каталоге `target`, если он подключён как volume.

## Тестовые сценарии

### Общая навигация Calendars, Modals и Ads

Каждая из трёх проверок параметризована страницами Calendars, Modals и Ads — всего 9 отдельных запусков:

| ID | Проверка | Ожидаемый результат |
|---|---|---|
| NAV01 | Нажать кнопку `Blog` в шапке | Проверен исходный `href`, открывается сайт `automatenow.io` |
| NAV02 | Нажать ссылку `Home` рядом с названием страницы | Проверен исходный `href`, открывается главная страница Practice Automation |
| NAV03 | Нажать ссылку на YouTube-ролик в описании | Проверены `href` и `target=_blank`, ролик открывается в новой вкладке |

Если ссылка YouTube неверна или новая вкладка не открылась, тест падает. Если ссылка корректна, но внешний сервис не смог загрузить сам ролик, тест получает статус `Skipped` с причиной в Allure — временная недоступность YouTube не маскирует дефект навигации сайта.

### Calendars — позитивные

| ID | Проверка | Ожидаемый результат |
|---|---|---|
| P01 | Открыть страницу | Заголовок `Calendars` отображается |
| P02 | Проверить подсказку формата | Отображается `YYYY-MM-DD` |
| P03 | Нажать на поле даты | Открывается date picker |
| P04 | Проверить дни текущего месяца | Доступно 28–31 дней, включая 1 и 28 |
| P05 | Выбрать первый день | В поле записана дата с днём `01` |
| P06 | Перейти к следующему месяцу | Месяц/год в календаре изменились |
| P07 | Вернуться к предыдущему месяцу | Восстановлены исходные месяц и год |
| P08 | Ввести високосную дату `2024-02-29` | Дата корректно читается как ISO date |
| P09 | Ввести граничную дату начала года `2026-01-01` | Значение сохранено без искажения |
| P10 | Ввести будущую дату `2099-12-31` | Значение сохранено без искажения |

### Calendars — негативные

| ID | Проверка | Ожидаемый результат |
|---|---|---|
| N01 | Ввести дату в неверном формате `31/12/2026` | Значение не интерпретируется как ISO date |
| N02 | Ввести несуществующий день `2025-02-30` | Значение не интерпретируется как валидная дата |
| N03 | Ввести текст `not-a-date` | Значение не интерпретируется как дата |
| N04 | Ввести несуществующий месяц `2026-13-01` | Значение не интерпретируется как валидная дата |

### Modals — позитивные

| ID | Проверка | Ожидаемый результат |
|---|---|---|
| P01 | Открыть страницу | Заголовок `Modals` отображается |
| P02 | Нажать `Simple Modal` | Простое модальное окно открыто |
| P03 | Проверить заголовок простого окна | Отображается `Simple Modal` |
| P04 | Проверить содержимое простого окна | Отображается текст о simple modal |
| P05 | Проверить кнопку закрытия | Кнопка видима и доступна |
| P06 | Закрыть простое окно кнопкой | Окно скрыто |
| P07 | Нажать `Form Modal` | Открыто окно `Modal Containing A Form` |
| P08 | Проверить обязательность Name | Есть `required` и `aria-required=true` |
| P09 | Заполнить Name, Email и Message | Все значения отображаются без искажений |

### Modals — негативные

| ID | Проверка | Ожидаемый результат |
|---|---|---|
| N01 | Нажать Escape в Simple Modal | Окно не закрывается согласно конфигурации |
| N02 | Проверить состояние после закрытия | Закрытое окно не остаётся видимым |
| N03 | Отправить форму без обязательного Name | Появляется ошибка валидации Name |
| N04 | Отправить форму с некорректным Email | Появляется ошибка валидации Email |

### Ads — позитивные

| ID | Проверка | Ожидаемый результат |
|---|---|---|
| P01 | Открыть страницу | Заголовок `Ads` отображается |
| P02 | Проверить текст обратного отсчёта | В тексте есть последовательность 5…4…3…2…1 |
| P03 | Дождаться таймера | Реклама появляется автоматически |
| P04 | Проверить заголовок рекламы | Отображается `Hi` |
| P05 | Проверить текст рекламы | Отображается `I am an ad.` |
| P06 | Проверить семантику окна | Корневой элемент имеет `role=dialog` |
| P07 | Проверить кнопку закрытия | Кнопка видима |
| P08 | Закрыть рекламу кнопкой | Реклама скрыта |

### Ads — негативные

| ID | Проверка | Ожидаемый результат |
|---|---|---|
| N01 | Проверить состояние сразу после загрузки | Реклама ещё не показана до истечения таймера |
| N02 | Нажать Escape | Реклама не закрывается согласно конфигурации |
| N03 | Попытаться обработать рекламу как browser alert | Selenium подтверждает, что это DOM modal, а не alert |

### Обязательный сценарий Form Fields

1. Selenium находит все `li` из раздела **Automation tools**.
2. Тексты элементов собираются в список: `Selenium`, `Playwright`, `Cypress`, `Appium`, `Katalon Studio`.
3. Список соединяется через запятую и пробел.
4. Поле Message заполняется строкой `Selenium, Playwright, Cypress, Appium, Katalon Studio`.
5. Тест отдельно проверяет исходный список, сформированную строку и фактическое значение поля.

## CI/CD

GitHub Actions и GitLab CI запускают полный набор в headless Chrome и Firefox для каждого push и pull request. Jenkins позволяет выбрать Chrome или Firefox параметром `BROWSER`. Даже при падении CI сохраняет `allure-results`, статический Allure report и Surefire-отчёты как артефакты.

Также в репозитории есть:

- `.gitlab-ci.yml` — параллельные UI-тесты, JUnit-отчёт, артефакты и публикация статического Allure-отчёта через GitLab Pages из основной ветки;
- `Jenkinsfile` — Declarative Pipeline в Docker-контейнере: компиляция, тесты, Allure и архивирование результатов с fingerprint.

Для Jenkins нужны плагины Pipeline, Docker Pipeline и JUnit, а на агенте — Docker. GitLab Runner должен иметь доступ в интернет для Maven Central и тестового стенда.
