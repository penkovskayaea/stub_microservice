# Stub Server

Легковесный HTTP-сервер-заглушка на Spring Boot. Позволяет быстро поднимать моки для внешних API, эмулировать задержки, отдавать случайные данные или проксировать реальные запросы. Идеально подходит для интеграционного тестирования и локальной разработки.

## Технологический стек
*   Java 11
*   Spring Boot 2.7.0
*   Maven
*   Docker
*   Kubernetes / Helm

## Быстрый старт (Локально)

Для сборки и запуска вам понадобятся Maven и JDK 11.

```bash
# Сборка проекта
mvn clean package

# Запуск
java -jar target/stub-1.0.0.jar
```

По умолчанию сервер запустится на порту `8080`, а Actuator (метрики и health-check) — на порту `8081`.

## Конфигурация заглушки

Вся логика работы описывается в YAML-файле. По умолчанию приложение ищет файл `config.yml` в корне рабочей директории. Путь можно переопределить через переменную окружения `STUB_CONFIG_FILE` или в `application.yml` (параметр `stub.config-file`).

### Структура `config.yml`

Файл состоит из списка правил `rules`. Каждое правило имеет следующую структуру:

*   **`path`** (обязательно) — URI путь запроса. Поддерживаются Ant-паттерны (например, `/api/**`).
*   **`method`** (обязательно) — HTTP-метод (`GET`, `POST`, `PUT`, `DELETE` и т.д.).
*   **`delay`** — задержка ответа в миллисекундах. По умолчанию `0`.
*   **`response`** — блок настройки статического ответа.
    *   `status` — HTTP-код ответа (например, `200`, `404`, `500`).
    *   `body` — строковое тело ответа.
    *   `randomSize` — размер генерируемого случайного тела ответа в байтах (например, `1048576` для 1 МБ).
*   **`proxy`** — блок настройки проксирования запроса.
    *   `url` — адрес реального сервера, куда будет перенаправлен запрос.

*Особенность:* Блоки `response` и `proxy` можно комбинировать. Если указать `proxy`, запрос уйдет на реальный сервер, но если также указан `response.status`, то клиент получит этот статус, а тело ответа будет взято от реального сервера.

### Примеры использования

**1. Статический ответ**
```yaml
rules:
  - path: /ping
    method: GET
    response:
      status: 200
      body: "I am alive"
    delay: 0
```

**2. Ответ с задержкой и случайными данными**
```yaml
rules:
  - path: /random
    method: GET
    response:
      status: 200
      randomSize: 1048576 # 1 MB случайных байт
    delay: 200
```

**3. Проксирование запроса**
```yaml
rules:
  - path: /proxy-test
    method: POST
    proxy:
      url: http://httpbin.org/post
    delay: 500
```
*(При проксировании заглушка пробрасывает все query-параметры, заголовки и тело запроса, кроме `Host` и `Content-Length`).*

**4. Ошибка (Custom Status)**
```yaml
rules:
  - path: /custom-status
    method: GET
    response:
      status: 410
      body: "Gone"
```

---

## Запуск через Docker

В проекте есть готовый `Dockerfile` на базе `eclipse-temurin:11-jre`.

```bash
# Сборка образа
docker build -t my-stub-image:latest .

# Запуск контейнера с монтированием конфига
docker run -d \
  -p 8080:8080 \
  -p 8081:8081 \
  -v $(pwd)/config.yml:/app/config/config.yml \
  my-stub-image:latest
```

---

## Развертывание в Kubernetes

В корне проекта лежат манифесты `stub-manifest.yaml`, `stub-2-deployment.yaml` и `nodeport.yaml`.

### 1. Через манифесты
Вы можете создать ConfigMap с настройками и Deployment.
```bash
kubectl apply -f stub-manifest.yaml
kubectl apply -f nodeport.yaml
```

### 2. Через Helm (рекомендуется)
В папке `helm/stub` находится полноценный Helm-чарт.

Параметры, доступные для настройки в `values.yaml`:

*   `replicaCount` — количество реплик (по умолчанию `1`).
*   `image.repository` — репозиторий с вашим Docker-образом.
*   `image.tag` — тег образа.
*   `image.pullPolicy` — политика загрузки образов.
*   `updateStrategy.type` — стратегия обновления (`RollingUpdate` или `Recreate`).
*   `service.type` — тип сервиса (`ClusterIP`, `NodePort`, `LoadBalancer`).
*   `service.port` — порт сервиса (по умолчанию `8080`).
*   `service.actuatorPort` — опциональный порт для Actuator (`8081`).
*   `ingress.enabled` — включение Ingress (`true/false`).
*   `ingress.className` — класс Ingress-контроллера (например, `nginx`).
*   `ingress.hosts` — настройка доменов и путей.
*   `ingress.tls` — настройка TLS-сертификатов.
*   `resources.limits` — лимиты CPU и памяти.
*   `resources.requests` — запрашиваемые ресурсы.
*   `probes.livenessProbe` / `readinessProbe` — настройки health-check'ов.
*   `env` — дополнительные переменные окружения (например, путь до конфига).
*   `config.rules` — непосредственно сам YAML со списком правил, который будет помещен в ConfigMap.

Установка чарта:
```bash
helm install my-stub ./helm/stub
```

---

## Мониторинг и Метрики

Приложение интегрировано с Spring Actuator и Micrometer.
*   **Порт:** `8081`
*   **Health Check:** `/actuator/health` (используется в Kubernetes probes).
*   **Кастомные метрики:**
    *   `stub.requests.total` — общее количество запросов.
    *   `stub.requests.failed` — количество неуспешных запросов (статус >= 400).
*   **Стандартные метрики:** доступны через `/actuator/prometheus` (если подключен соответствующий экспортер) или `/actuator/metrics`.

## Структура проекта

```text
├── src/main/java/com/example/stub
│   ├── config       # Конфигурационные классы (Rule, StubConfig, ProxyConfig, ResponseConfig)
│   ├── controller   # REST-контроллер, перехватывающий все запросы
│   ├── metrics      # Кастомные метрики для Actuator
│   ├── service      # Логика мокирования (StubService) и проксирования (ProxyService)
│   ├── util         # Генератор случайных данных
│   └── StubApplication.java
├── src/main/resources
│   ├── application.yml
│   └── config.yml   # Пример файла с правилами
├── helm/stub        # Helm-чарт для деплоя
├── Dockerfile       # Инструкция сборки Docker-образа
└── pom.xml
```