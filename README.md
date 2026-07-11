# minoreader

A multiplatform RSS reader built with Kotlin Multiplatform + Compose Multiplatform, running on
**Android** and **Linux desktop** (JVM). Local-first: each device keeps its own SQLite database,
no server.

Package: `org.dev.minoreader`

## Features

- Add RSS/Atom feeds, each tagged with a category.
- One-line title feed with per-category color coding, filterable by category.
- Mark as read (leaves the feed) and favorite; **Read** (history) and **Favorites** tabs.
- Full-text search across all articles (title + excerpt).
- In-app reader: extracts and renders the full article as native Compose (headings, paragraphs,
  bold/italic, clickable links, lists, quotes, code). Images are shown as tappable links for now.
  "Open original" opens the page in the system browser.
- AI summaries are planned (a disabled button is already in place).

## Stack

- **UI:** Compose Multiplatform (Material 3), single codebase in `commonMain`.
- **Database:** SQLDelight (SQLite; Android/JDBC drivers via `SqlDriverFactory`).
- **RSS:** `com.prof18.rssparser` (KMP).
- **Article extraction:** jsoup (in the `jvmShared` source set) + a main-content heuristic →
  a neutral `ContentBlock` model → Compose renderer in `commonMain`.
- **DI:** Koin.
- **Navigation:** simple state (a sealed `Screen`).

## Architecture (`composeApp/`)

```
commonMain/  UI (ui/), data (data/), rss/, reader/, di/, util/, platform/  + sqldelight/ (.sq)
androidMain/ MainActivity, MinoApp (Application + Koin), Android driver/opener
desktopMain/ main.kt (Koin + Window), desktop driver/opener, resources/ (window icon)
jvmShared/   JsoupArticleExtractor (jsoup is JVM-only; shared by android + desktop)
desktopTest/ tests (DbTest, RssParseTest, SearchReadTest, ExtractorTest, DiGraphTest)
```

Platform seams use `expect/actual`: the SQLite driver, `currentTimeMillis`/`parseFeedDate`,
`formatDate`, and `UrlOpener`. Android and desktop are both JVM, so the `actual`s use `java.time`.

## Build & run

Requires a JDK 17+ and, for Android, the Android SDK (`local.properties` → `sdk.dir`).

```bash
# Tests (run on the desktop/JVM, no emulator)
./gradlew :composeApp:desktopTest

# Android (debug)
./gradlew :composeApp:assembleDebug        # -> composeApp/build/outputs/apk/debug/
./gradlew :composeApp:installDebug         # with a device/emulator connected

# Desktop: build + run the packaged app (bundled JRE, correct HiDPI scale)
./gradlew :composeApp:runDistributable
# ...or quick dev run (blocks the terminal while the window is open)
./gradlew :composeApp:run
```

The desktop database lives at `~/.local/share/minoreader/minoreader.db`.

### Install as an app on Ubuntu (dock icon)

Installs the packaged app under `~/.local` and creates a `.desktop` entry + icon (no root), so it
shows up in GNOME Activities and can be pinned to the dock:

```bash
./install.sh          # builds the app, installs under ~/.local, creates the entry + icon
./uninstall.sh        # removes it (keeps the database)
```

Packaging (jpackage) needs a **full JDK with `jlink`** — a JRE alone fails at
`:composeApp:checkRuntime`. `install.sh` auto-detects a suitable JDK; if none is found, install
one with `sudo apt install openjdk-21-jdk` (or run `JAVA_HOME=/path/to/jdk ./install.sh`).

> **HiDPI:** running the plain uber-jar with `java -jar` can render at 1x on HiDPI screens. Use
> the packaged app (above), or pass the scale: `GDK_SCALE=2 java -jar <jar>`.
