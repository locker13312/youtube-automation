# YouTube UI automation (Selenium 4 + TestNG)

[![CI](https://github.com/locker13312/youtube-automation/actions/workflows/tests.yml/badge.svg)](https://github.com/locker13312/youtube-automation/actions/workflows/tests.yml)

UI test from a take-home assignment: search YouTube with a random numeric query, open the 2nd and the
4th video in the results, go to the author's channel and try to subscribe as a guest. The test expects
the sign-in prompt.

**Stack:** Java 17, Selenium 4, TestNG, Maven, Allure, GitHub Actions

## Run

Requires JDK 17+, Maven 3.8+ and Chrome. Selenium Manager picks the driver.

```bash
mvn clean test                          # full scenario
mvn test -DsuiteFile=testng-unit.xml    # test data checks only, no browser
mvn clean test -Dbrowser=firefox
mvn allure:serve                        # Allure report
```

In PowerShell, quote arguments that contain `=`: `mvn test "-DsuiteFile=testng-unit.xml"`.

Failure screenshots go to `screenshots/`, the TestNG report to `test-output/index.html`. The
ENVIRONMENT block in the Allure report is filled by `AllureEnvironment` from the live driver, so it
shows the browser and OS versions of the actual run.

## Structure

```
src/test/java/io/github/locker13312/qa/
├── core/     Config, DriverFactory, Browser, ScreenshotListener, AllureEnvironment
├── pages/    BasePage, YouTubeHomePage, SearchResultsPage, VideoPage, ChannelPage,
│             SignInPromptDialog, PromoBanner, MiniPlayer
├── tests/    BaseTest, YouTubeSearchAndSubscribeTest, RandomQueryTest
└── utils/    RandomQuery
```

There is no production code, so everything lives in `src/test` and all dependencies use `test` scope.

Page objects have no assertions: a page performs an action and returns state, the test checks it. A
method that leads to another screen returns that screen's object, and `WebElement`s never leave the
page class. Locators are constants at the top of each class. `SignInPromptDialog`, `PromoBanner` and
`MiniPlayer` are overlays that can appear over any screen, so they are page objects too.

Steps are marked with `@Step`, and steps 2 and 9 are wrapped in `Allure.step`, so all ten show up in
the report. On failure the report gets a screenshot, the current URL and the page DOM.

## Scenario

| #   | Step                                     | Implementation                                                  |
| --- | ---------------------------------------- | --------------------------------------------------------------- |
| 1   | Open youtube.com                         | `YouTubeHomePage.open()`                                        |
| 2   | Tab title is "YouTube"                   | `assertEquals(home.getPageTitle(), "YouTube")`                  |
| 3   | Search for 2-4 random digits             | `RandomQuery.digits()`                                          |
| 4   | Maximize the browser                     | `Browser.maximize()`                                            |
| 5   | Select the 2nd result                    | `SearchResultsPage.openVideo(2)` + `VideoPage.backToResults()` |
| 6   | Play the 4th video                       | `SearchResultsPage.openVideo(4)`                                |
| 7   | Click the author's avatar                | `VideoPage.clickOwnerAvatar()`                                  |
| 8   | Click "ПІДПИСАТИСЯ" (Subscribe)          | `ChannelPage.clickSubscribe()`                                  |
| 9   | The prompt shows exactly "Увійти" (Sign in) | `SignInPromptDialog.isTextVisible("Увійти")`                 |
| 10  | Close the tab                            | `Browser.closeCurrentTab()`                                     |

## Implementation notes

- Steps 5 and 6 work with the same results list. "Select" means clicking the card, because hovering
  changes nothing a test could check. Step 5 opens the 2nd video, `backToResults()` goes back, step 6
  plays the 4th one. Positions are read again after going back.
- Only video cards are counted. The results also contain ads, Shorts shelves, channels and playlists.
  Shorts use the same `ytd-video-renderer` tag but link to `/shorts/`, so the main locator filters by
  link: `ytd-video-renderer:has(a#video-title[href*='/watch'])`.
- The subscribe button text in the DOM is "Підписатися" and CSS renders it in upper case, so that
  check ignores case. Step 9 needs an exact match, so it uses `normalize-space(text())='Увійти'` and
  checks visibility rather than presence: YouTube keeps hidden copies of labels in the markup.
- The Premium promo banner (`yt-mealbar-promo-renderer`) and the miniplayer (`ytd-miniplayer`) cover
  clickable elements and cause `ElementClickInterceptedException`. Both are closed with a real click
  before the affected steps, and before the target element is looked up, since the layout shifts and
  an element found earlier goes stale. The miniplayer's close button has zero size until hovered, so
  the test hovers first. No JS clicks: they go through overlays and hide the problem.
- Fallback locators are a list of separate `By`s, not one comma-separated CSS selector. A comma
  selector returns the first match in document order. After SPA navigation the video page stays in
  the DOM before the channel page, and an unscoped subscribe locator used to pick up its hidden copy.
- The window starts at 900x600 on purpose: if it started maximized, step 4 would change nothing
  measurable. The UI language is `uk-UA` because the test checks Ukrainian labels.

## Stability and CI

Explicit waits only (`WebDriverWait`), no `Thread.sleep` and no implicit waits. `firstVisibleOf` and
`firstNonEmptyOf` try every locator in the list within one shared timeout. Headless mode is off by
default because YouTube shows a captcha more often in headless Chrome.

The workflow has two jobs. `unit` checks the test data on every push and pull request, needs no
network and blocks the pipeline. `ui` runs the scenario under Xvfb and is started manually: GitHub
runners reach YouTube from another country and get a different language version and captchas, so
running it automatically would only add noise. Allure results, screenshots and the TestNG report are
uploaded as artifacts.
