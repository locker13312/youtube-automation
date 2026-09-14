# YouTube UI automation (Selenium + TestNG)

[![UI tests](https://github.com/locker13312/youtube-automation/actions/workflows/tests.yml/badge.svg)](https://github.com/locker13312/youtube-automation/actions/workflows/tests.yml)

Сценарій із тестового завдання: пошук за випадковим числовим запитом, відкриття другого і четвертого відео у видачі, перехід на канал автора і спроба підписатися від імені неавторизованого користувача.

### Запуск

Потрібно JDK 17+, Maven 3.8+ і Chrome. Драйвер підбирає Selenium Manager.

```bash
mvn clean test                          # весь сценарій
mvn test -DsuiteFile=testng-unit.xml    # тільки тестові дані, без браузера
mvn clean test -Dbrowser=firefox
mvn allure:serve                        # звіт
```

У PowerShell аргумент із `=` береться в лапки: `mvn test "-DsuiteFile=testng-unit.xml"`.

Скріншоти падінь — у `screenshots/`, звіт TestNG — у `test-output/index.html`. Блок ENVIRONMENT у звіті Allure заповнює `AllureEnvironment` з живого драйвера, щоб там стояли фактичні версії браузера й ОС прогону.

### Структура

```
src/test/java/ua/privatbank/qa/
├── core/     Config, DriverFactory, Browser, ScreenshotListener, AllureEnvironment
├── pages/    BasePage, YouTubeHomePage, SearchResultsPage, VideoPage, ChannelPage,
│             SignInPromptDialog, PromoBanner, MiniPlayer
├── tests/    BaseTest, YouTubeSearchAndSubscribeTest, RandomQueryTest
└── utils/    RandomQuery
```

Продакшн-коду немає, тому все лежить у `src/test`, а залежності мають `test` scope.

У Page Object немає ассертів: сторінка виконує дію і повертає стан, перевіряє тест. Метод, що веде на інший екран, повертає його об'єкт. `WebElement` назовні не віддається. Локатори — константи на початку класу. `SignInPromptDialog`, `PromoBanner` і `MiniPlayer` — це компоненти, що з'являються поверх будь-якого екрана; вони в тому ж пакеті, бо влаштовані так само.

Кроки позначені `@Step`, пункти 2 і 9 обгорнуті в `Allure.step`, тому у звіті видно всі десять. При падінні до звіту додаються скріншот, URL і DOM.

### Кроки ТЗ

| # | Крок | Реалізація |
|---|------|------------|
| 1 | перехід на youtube.com | `YouTubeHomePage.open()` |
| 2 | вкладка "YouTube" | `assertEquals(home.getPageTitle(), "YouTube")` |
| 3 | запит із 2-4 випадкових цифр | `RandomQuery.digits()` |
| 4 | браузер на весь екран | `Browser.maximize()` |
| 5 | вибрати другу позицію | `SearchResultsPage.openVideo(2)` + `VideoPage.backToResults()` |
| 6 | запустити четверте відео | `SearchResultsPage.openVideo(4)` |
| 7 | клік на аватар автора | `VideoPage.clickOwnerAvatar()` |
| 8 | клік на "ПІДПИСАТИСЯ" | `ChannelPage.clickSubscribe()` |
| 9 | текст "Увійти", точний збіг | `SignInPromptDialog.isTextVisible("Увійти")` |
| 10 | закрити вкладку | `Browser.closeCurrentTab()` |

### Рішення щодо реалізації

Кроки 5 і 6 стосуються одного списку. Вибором вважається клік по картці: наведення курсора не змінює стан сторінки, і перевіряти в тесті було б нічого. Крок 5 відкриває друге відео, `backToResults()` повертає до видачі, крок 6 запускає четверте. Позиції зчитуються заново після повернення.

Позиції рахуються тільки серед карток відео: у видачі є ще реклама, полиці Shorts, канали і плейлисти. Shorts приходять тим самим тегом `ytd-video-renderer`, але ведуть на `/shorts/`, тому перший локатор фільтрує їх за посиланням — `ytd-video-renderer:has(a#video-title[href*='/watch'])`.

Текст кнопки підписки в DOM записаний як «Підписатися», верхній регістр домальовує CSS, тому порівняння через `equalsIgnoreCase`. Крок 9 навпаки вимагає точного збігу, там XPath `normalize-space(text())='Увійти'`. Перевіряється видимість, а не наявність у DOM: YouTube тримає в розмітці приховані копії написів.

Промо-банер Premium (`yt-mealbar-promo-renderer`) і мініплеєр (`ytd-miniplayer`) перекривають клікабельні елементи, і клік по них падає з `ElementClickInterceptedException`. Обидва закриваються справжнім кліком перед відповідними кроками, саме до пошуку елемента: після їх зникнення верстка зсувається і знайдений наперед `WebElement` стає stale. У мініплеєра перед кліком потрібне наведення курсора, бо до нього кнопка закриття має нульовий розмір. JS-клік як обхід не використовується — він проходить крізь перекриті елементи і ховає проблему замість того, щоб її показати.

Запасні локатори — це список окремих `By`, а не один CSS через кому: кома повертає перший елемент у порядку документа. Після SPA-переходів це критично, бо сторінка відео лишається в DOM раніше за сторінку каналу, і неприв'язаний локатор кнопки підписки чіпляв її схований елемент.

Стартове вікно 900×600 задане навмисно меншим за екран, інакше `maximize()` нічого не збільшить і крок 4 не буде вимірюваною дією. Мова інтерфейсу `uk-UA`, бо тест перевіряє українські написи.

### Стабільність і CI

Очікування тільки явні (`WebDriverWait`), без `Thread.sleep` та implicit wait. `firstVisibleOf` і `firstNonEmptyOf` перебирають увесь список локаторів у межах одного спільного таймауту. Headless вимкнений за замовчуванням: у ньому YouTube частіше показує капчу.

У CI два job'и. `unit` виконує перевірку тестових даних на кожен push, не залежить від мережі й блокує пайплайн. `ui` виконує сценарій під Xvfb вручну або нічним cron'ом: раннер GitHub звертається до YouTube з іншої країни й отримує іншу мовну версію та капчу, тож на пушах це давало б шум. Allure-результати, скріншоти і звіт TestNG зберігаються як артефакти.
