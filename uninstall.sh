#!/usr/bin/env bash
#
# Remove the minoreader installed by install.sh (keeps the database).
#
set -euo pipefail

APP_ID="minoreader"
DATA_HOME="${XDG_DATA_HOME:-$HOME/.local/share}"
BIN_DIR="${MINOREADER_BIN:-$HOME/.local/bin}"

rm -rf "$DATA_HOME/$APP_ID/app"
rm -f "$DATA_HOME/applications/$APP_ID.desktop"
rm -f "$DATA_HOME/icons/hicolor/scalable/apps/$APP_ID.svg"
rm -f "$BIN_DIR/$APP_ID"

update-desktop-database "$DATA_HOME/applications" 2>/dev/null || true
gtk-update-icon-cache -f -t "$DATA_HOME/icons/hicolor" 2>/dev/null || true

echo "✅ minoreader removed."
echo "   The database at $DATA_HOME/$APP_ID/minoreader.db was kept — delete it manually if you want."
