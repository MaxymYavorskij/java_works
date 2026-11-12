# Багатопотокова система обробки замовлень

Система демонструє обробку замовлень на Java 25. Вона має п'ять модулів JPMS. Система генерує замовлення, фільтрує їх, обробляє в пулах потоків і зберігає результат у пам'яті.


## Бібліотеки

| Бібліотека | Версія | Роль | Хто використовує |
|---|---:|---|---|
| Lombok | 1.18.48 | Генерує геттери, білдери, `equals`, `hashCode` і `toString` під час компіляції. Має scope `provided`, тому не потрапляє в runtime. | `order-model` |
| JavaFaker | 1.0.2 | Генерує випадкові товари, імена клієнтів, бренди, кольори та ціни. | `order-processing`, клас `FakeOrderGenerator` |
| SnakeYAML | 1.23 | Читає YAML-словники JavaFaker. Прямих імпортів у коді проєкту немає. | JavaFaker у runtime |
| Commons Lang3 | 3.5 | Транзитивна залежність JavaFaker. Містить допоміжні методи для роботи з рядками та об'єктами. | JavaFaker |
| Generex | 1.0.2 | Транзитивна залежність JavaFaker. Генерує рядки за регулярними виразами. Приклад: `Model-####`. | JavaFaker |
| Automaton | 1.11-8 | Транзитивна залежність Generex. Працює з регулярними автоматами. | Generex |



### Дерево залежностей

```text
order-processing
├── order-model
├── order-storage
├── javafaker 1.0.2
│   ├── commons-lang3 3.5
│   ├── generex 1.0.2
│   │   └── automaton 1.11-8
│   └── snakeyaml 1.23 (android) — виключено
└── snakeyaml 1.23 (звичайний JVM JAR) — додано явно
```

## Структура проєкту

| Модуль JPMS | Каталог | Відповідальність | Експортує | Основні залежності |
|---|---|---|---|---|
| `order.model` | `order-model` | Модель предметної області: товари, замовлення, статуси, валідація | `com.example.order.model` | `lombok` (static) |
| `order.storage` | `order-storage` | Потокобезпечне зберігання замовлень у пам'яті | `com.example.order.storage` | `order.model` |
| `order.processing` | `order-processing` | Генерація, фільтрація та обробка замовлень | `com.example.order.processing` | `order.model`, `order.storage`, `javafaker` |
| `order.concurrency` | `order-concurrency` | Паралельна обробка в пулах потоків | `com.example.order.concurrency` | `order.model`, `order.processing` |
| `order.app` | `order-app` | Точка входу та демонстрація роботи | — | усі модулі |

Напрямок залежностей:

```text
order.app
  ├── order.concurrency ──> order.processing ──> order.storage ──> order.model
  ├── order.processing
  ├── order.storage
  └── order.model
```

Пакети `com.example.order.storage.internal` і `com.example.order.processing.internal` не експортуються. Інші модулі не можуть імпортувати з них класи.

## Як працює система

1. `Main` створює два репозиторії: для електроніки та одягу.
2. `Main` створює два сервіси обробки. Кожен сервіс працює з одним типом товару.
3. `FakeOrderGenerator` генерує 8 замовлень на електроніку та 6 замовлень на одяг.
4. `OrderFilters` вибирає електроніку із сумою від 1000 грн. Це демонстрація лямбда-предикатів.
5. `OrderDispatcher` передає замовлення у два пули потоків. Перший пул має 4 потоки, другий — 3 потоки.
6. `OrderProcessingService` перевіряє тип товару, застосовує знижку та зберігає замовлення зі статусом `COMPLETED`.
7. Якщо обробка падає, сервіс зберігає замовлення зі статусом `FAILED` і кидає `OrderProcessingException`. Диспетчер збирає такі помилки в `BatchResult`.
8. `Main` друкує результати, вміст репозиторіїв і демонструє дві помилки: некоректну знижку та порожнє ім'я клієнта.

## Головні класи

### Модуль `order.model`

- `Product` — абстрактний базовий клас. Містить `id`, `name`, `price`. Перевіряє дані в конструкторі. Методи `equals` і `hashCode` використовують `id`.
- `Electronics` — товар з брендом, моделлю та гарантією. Гарантія не може бути від'ємною.
- `Clothing` — товар з брендом, розміром, кольором і матеріалом.
- `Order<T extends Product>` — незмінне замовлення. Містить клієнта, товар, кількість, час створення та статус. Метод `totalPrice()` множить ціну на кількість. Метод `withStatus()` повертає нове замовлення.
- `ProductCategory` — категорія товару з українською назвою.
- `OrderStatus` — статус замовлення: `NEW`, `COMPLETED`, `FAILED`.
- `ModelValidation` — package-private перевірки для моделі.
- `OrderValidationException` — помилка некоректних даних моделі.

### Модуль `order.storage`

- `OrderRepository<T extends Product>` — потокобезпечний репозиторій на `ConcurrentHashMap`. Зберігає замовлення за `UUID`. Метод `findAll()` сортує замовлення за часом створення.
- `StorageTypeGuard` — внутрішня перевірка типу товару під час збереження.
- `OrderStorageException` — помилка рівня зберігання.

### Модуль `order.processing`

- `FakeOrderGenerator` — створює товари та замовлення через JavaFaker і Lombok-білдери.
- `OrderProcessingService<T extends Product>` — реалізує `OrderProcessor<T>`. Перевіряє тип товару, рахує знижку, зберігає результат і повертає `ProcessedOrder`.
- `OrderProcessor<T extends Product>` — функціональний інтерфейс для обробки одного замовлення.
- `DiscountPolicy` — функціональний інтерфейс знижки. Має готові варіанти `none()`, `quantityBased()` і `categoryBased()`.
- `OrderFilters` — набір предикатів для статусу, категорії, типу товару, мінімальної суми та імені клієнта.
- `ProcessedOrder<T extends Product>` — незмінний результат обробки: замовлення, знижка, фінальна ціна, ім'я потоку та час.
- `PriceCalculator` — внутрішній розрахунок ціни. Перевіряє знижку в межах від 0 до 1. Округлює результат до двох знаків.
- `OrderProcessingException` — помилка обробки замовлення.
- `UnsupportedOrderTypeException` — помилка невідповідності типу товару.

### Модуль `order.concurrency`

- `OrderDispatcher<T extends Product>` — керує пулом потоків. Метод `submit()` запускає одне замовлення. Метод `processAll()` запускає пакет через `invokeAll()` і збирає успішні та невдалі результати.
- `BatchResult<T extends Product>` — результат пакета: успішні замовлення, помилки та тривалість.
- `FailedOrder<T extends Product>` — замовлення з повідомленням про помилку.
- `OrderDispatchException` — помилка пулу потоків.

Клас `OrderDispatcher` реалізує `AutoCloseable`. Конструкція `try-with-resources` у `Main` гарантує завершення пулів.

### Модуль `order.app`

- `Main` — створює репозиторії, сервіси, генератор і диспетчери. Запускає демонстрацію та друкує результати.

### Ключові інваріанти

- Параметр `T extends Product` обмежує типи на етапі компіляції.
- Java стирає generic-типи в runtime. Тому `OrderProcessingService` і `OrderRepository` додатково перевіряють реальний клас товару.
- Замовлення незмінне. Зміна статусу створює новий об'єкт.
- `OrderRepository` використовує `ConcurrentHashMap`, тому кілька потоків можуть писати та читати без зовнішнього блокування.
- Помилка одного замовлення не зупиняє обробку інших замовлень у пакеті.

## Вимоги

| Інструмент | Версія | Перевірка |
|---|---:|---|
| JDK | 25 | `java -version` |
| Maven | 3.9 або новіша | `mvn -v` |

Якщо команда `mvn` недоступна, додайте Maven до `PATH`. Maven також входить до IntelliJ IDEA:

```text
C:\Program Files\JetBrains\IntelliJ IDEA <version>\plugins\maven-plugin\lib\maven3\bin
```

## Запуск


### IntelliJ IDEA

1. Відкрийте `File → Open`.
2. Виберіть кореневий `pom.xml`.
3. Відкрийте проєкт як Maven-проєкт.
4. Виберіть JDK 25 у `File → Project Structure → Project SDK`.
5. Відкрийте `order-app/src/main/java/com/example/order/app/Main.java`.
6. Натисніть `Run`.

IntelliJ додає модулі та залежності до module path автоматично.


## Результат

1. Система генерує 8 замовлень на електроніку та 6 замовлень на одяг.
2. Система друкує кількість електроніки із сумою від 1000 грн.
3. Обидва пули обробляють замовлення паралельно.
4. Система друкує успішні замовлення, кількість помилок і час обробки.
5. Система друкує завершені замовлення з репозиторіїв.
6. Система друкує рядок `У сховищі: електроніка=8, одяг=6`.
7. Система перехоплює `OrderProcessingException` для знижки `1.5`.
8. Система перехоплює `OrderValidationException` для порожнього імені клієнта.

JavaFaker генерує випадкові дані. Тому назви товарів, імена клієнтів, ціни, кількість після фільтра та час обробки можуть відрізнятися.

## Якщо запуск не працює

- `mvn` не розпізнається. Додайте Maven до `PATH` або запустіть проєкт з IntelliJ IDEA.
- `Module order.model not found`. Виконайте `mvn clean install` у корені проєкту. Команда `mvn -pl order-app` без `install` не бачить інші модулі.
- `invalid target release: 25`. `JAVA_HOME` вказує на старіший JDK. Виберіть JDK 25.
- Кирилиця в консолі відображається як `????`. Додайте `-Dstdout.encoding=UTF-8` і `-Dstderr.encoding=UTF-8` або виконайте `chcp 65001`.
- `NoClassDefFoundError: org/yaml/snakeyaml/Yaml`. Перевірте залежність SnakeYAML у `order-processing/pom.xml`.

## Обмеження

- Дані зберігаються тільки в пам'яті. Після завершення програми дані зникають.
- Проєкт демонструє generics, лямбди, JPMS і багатопотоковість. 
- Система не має бази даних, REST API та постійного сховища.
