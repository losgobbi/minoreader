#!/usr/bin/env bash
#
# Install minoreader as a desktop app on Linux (user-level, NO root).
# Builds the packaged app (bundled JRE via jpackage) and creates the launcher entry
# and icon so it shows up in GNOME Activities and can be pinned to the dock.
#
# Requirements: a JDK 17+ with jlink/jpackage on PATH (the same one used to build the
#               project) and a configured Android SDK (local.properties) — the module
#               applies the Android plugin, so every Gradle task needs it to configure.
#
# Optional variables:
#   SKIP_BUILD=1         -> don't build; reuse whatever is in build/compose/binaries
#   GRADLE_ARGS="..."    -> extra Gradle args (default: --no-daemon --max-workers=1)
#   XDG_DATA_HOME=...     -> install root (default: ~/.local/share)
#   MINOREADER_BIN=...    -> where to create the terminal symlink (default: ~/.local/bin)
#   MINOREADER_WMCLASS=.. -> force the .desktop StartupWMClass (skip WM_CLASS auto-detection)
#   SKIP_WMCLASS_DETECT=1 -> don't briefly launch the app to detect WM_CLASS; use "minoreader"
#
set -euo pipefail

APP_ID="minoreader"
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DATA_HOME="${XDG_DATA_HOME:-$HOME/.local/share}"
BIN_DIR="${MINOREADER_BIN:-$HOME/.local/bin}"
INSTALL_DIR="$DATA_HOME/$APP_ID/app"
DESKTOP_FILE="$DATA_HOME/applications/$APP_ID.desktop"
ICON_DIR="$DATA_HOME/icons/hicolor/scalable/apps"
SRC_APP="$PROJECT_DIR/composeApp/build/compose/binaries/main/app/$APP_ID"

# Find a FULL JDK (with jlink). jpackage/createDistributable require jlink, which a JRE
# lacks. Order: JAVA_HOME → PATH → common locations → ~/toolcache.
find_jdk_with_jlink() {
  if [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/jlink" ]; then
    printf '%s\n' "$JAVA_HOME"; return 0
  fi
  if command -v jlink >/dev/null 2>&1; then
    dirname "$(dirname "$(command -v jlink)")"; return 0
  fi
  local c
  for c in /usr/lib/jvm/*/bin/jlink "$HOME"/toolcache/jdk*/bin/jlink "$HOME"/.sdkman/candidates/java/*/bin/jlink; do
    [ -x "$c" ] && { dirname "$(dirname "$c")"; return 0; }
  done
  return 1
}

# Detect the running window's WM_CLASS so GNOME matches the open window to this .desktop
# entry (otherwise a second, generic "gear" icon appears in the dock on launch). AWT derives
# WM_CLASS from the Java main class, so we can't reliably guess it — we briefly launch the app
# and read it with xprop. Best-effort: needs an X session + xprop; falls back to "minoreader".
detect_wmclass() {
  [ -n "${DISPLAY:-}" ] || return 1
  command -v xprop >/dev/null 2>&1 || return 1

  "$INSTALL_DIR/bin/$APP_ID" >/dev/null 2>&1 &
  local pid=$!
  local raw="" res_class="" i
  for i in $(seq 1 40); do
    sleep 0.5
    kill -0 "$pid" 2>/dev/null || break   # app died early
    raw="$(xprop -name "$APP_ID" WM_CLASS 2>/dev/null | sed -n 's/.*WM_CLASS(STRING) = //p')"
    [ -n "$raw" ] && break
  done

  kill "$pid" 2>/dev/null || true
  pkill -f "$INSTALL_DIR/bin/$APP_ID" 2>/dev/null || true
  wait "$pid" 2>/dev/null || true

  # raw looks like: "minoreader", "org-dev-minoreader-MainKt"  -> take res_class (2nd token),
  # else the single token. GNOME matches StartupWMClass against res_name or res_class.
  res_class="$(printf '%s' "$raw" | sed -n 's/.*,[[:space:]]*"\([^"]*\)".*/\1/p')"
  [ -n "$res_class" ] || res_class="$(printf '%s' "$raw" | sed -n 's/^[[:space:]]*"\([^"]*\)".*/\1/p')"
  [ -n "$res_class" ] && { printf '%s\n' "$res_class"; return 0; }
  return 1
}

if [ "${SKIP_BUILD:-0}" != "1" ]; then
  if ! JDK_HOME="$(find_jdk_with_jlink)"; then
    cat >&2 <<'MSG'
ERROR: no full JDK with 'jlink' found (needed to package the app with a bundled JRE).
The current Java looks like a JRE only (no jlink/jpackage).

Fixes:
  • Install a full JDK:                sudo apt install openjdk-21-jdk
  • Or point to one that has jlink:    JAVA_HOME=/path/to/jdk ./install.sh
MSG
    exit 1
  fi
  export JAVA_HOME="$JDK_HOME"
  echo "==> Using JDK (with jlink): $JAVA_HOME"
  echo "==> Building the packaged app (createDistributable) — may take a while the first time..."
  ( cd "$PROJECT_DIR" && ./gradlew :composeApp:createDistributable ${GRADLE_ARGS:---no-daemon --max-workers=1} )
fi

if [ ! -x "$SRC_APP/bin/$APP_ID" ]; then
  echo "ERROR: launcher not found at $SRC_APP/bin/$APP_ID" >&2
  echo "       Run without SKIP_BUILD, or make sure createDistributable ran." >&2
  exit 1
fi

echo "==> Installing app to $INSTALL_DIR ..."
rm -rf "$INSTALL_DIR"
mkdir -p "$(dirname "$INSTALL_DIR")"
cp -r "$SRC_APP" "$INSTALL_DIR"

echo "==> Installing icon ..."
mkdir -p "$ICON_DIR"
cp "$PROJECT_DIR/composeApp/src/desktopMain/resources/$APP_ID.svg" "$ICON_DIR/$APP_ID.svg"

echo "==> Resolving WM_CLASS (to avoid a second 'gear' icon in the dock on launch) ..."
if [ -n "${MINOREADER_WMCLASS:-}" ]; then
  WMCLASS="$MINOREADER_WMCLASS"
  echo "    using MINOREADER_WMCLASS=$WMCLASS"
elif [ "${SKIP_WMCLASS_DETECT:-0}" = "1" ]; then
  WMCLASS="$APP_ID"
  echo "    detection skipped; using '$WMCLASS'"
elif WMCLASS="$(detect_wmclass)"; then
  echo "    detected WM_CLASS: $WMCLASS"
else
  WMCLASS="$APP_ID"
  echo "    could not detect (no X/xprop?); using '$WMCLASS' — override with MINOREADER_WMCLASS=... if the gear icon persists"
fi

echo "==> Creating .desktop entry ..."
mkdir -p "$(dirname "$DESKTOP_FILE")"
cat > "$DESKTOP_FILE" <<EOF
[Desktop Entry]
Type=Application
Version=1.0
Name=minoreader
GenericName=RSS Reader
Comment=RSS reader (AI summaries coming soon)
Exec=$INSTALL_DIR/bin/$APP_ID
Icon=$ICON_DIR/$APP_ID.svg
Terminal=false
Categories=Network;News;
StartupNotify=true
StartupWMClass=$WMCLASS
EOF
chmod +x "$DESKTOP_FILE"

echo "==> Terminal symlink at $BIN_DIR/$APP_ID ..."
mkdir -p "$BIN_DIR"
ln -sf "$INSTALL_DIR/bin/$APP_ID" "$BIN_DIR/$APP_ID"

update-desktop-database "$DATA_HOME/applications" 2>/dev/null || true
gtk-update-icon-cache -f -t "$DATA_HOME/icons/hicolor" 2>/dev/null || true

echo
echo "✅ Installed!"
echo "   • Look for 'minoreader' in Activities (indexing may take a few seconds)."
echo "   • Pin to dock: open the app → right-click its dock icon → 'Add to Favorites'."
echo "   • Terminal: $APP_ID   (if $BIN_DIR is on your PATH)"
echo "   • Uninstall: ./uninstall.sh"
