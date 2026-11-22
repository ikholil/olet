# Olet — personal expense manager

A minimal, offline-first Android expense manager built with Kotlin and Jetpack Compose (Material 3).

## Features
- **Optional sign in** — Google Sign-In (Credential Manager) or continue without an account
- **Expenses & income** — title, amount, category, account, date and note; edit, swipe-to-delete with undo
- **Categories** — defaults (Food, Transport, Utilities, Entertainment, Shopping, Health, Other / Salary, Business, Gift, Other), plus your own with icon and color
- **Accounts** — Cash, Bank, Mobile Banking (bKash/Nagad), Card, Other with live balances
- **Loans** — who owes you and whom you owe, partial payments, due dates, overdue highlight
- **Investments** — date, amount, company/person and contact info, active/closed
- **Smart entry** — title autocomplete from your history (fills category, account and amount), remembered amounts, quick +50/+100/+500/+1000, categories ordered by usage, Today/Yesterday date chips, person/company suggestions for loans and investments
- **Floating navigation** with a center + quick-add sheet, spending donut chart, hide-balance toggle, search across all transactions
- Monthly summaries, currency picker (৳ default), light/dark/system theme, Plus Jakarta Sans typeface (OFL, see `licenses/`)

All data is stored locally on the device (Room + DataStore).

## Build
Requires JDK 17 and the Android SDK (API 35).

```bash
./gradlew assembleDebug        # app/build/outputs/apk/debug/app-debug.apk
./gradlew testDebugUnitTest    # DB + end-to-end UI tests (Robolectric); screenshots in app/build/screenshots
./gradlew assembleRelease
```

## Releases (GitHub Actions)
Every push to `main` runs the tests, builds a release APK and publishes it as a GitHub Release (`v1.0.<run number>`).

Optional repository secrets:

| Secret | Purpose |
| --- | --- |
| `GOOGLE_WEB_CLIENT_ID` | Enables Google Sign-In (OAuth *Web application* client ID from Google Cloud). Without it the app works fully; the Google button just explains it isn't configured. |
| `RELEASE_KEYSTORE_BASE64`, `RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD` | Sign with your own private key (needed for Play Store). Without them, APKs are signed with the committed `app/debug.keystore`, so updates still install over each other. |

For Google Sign-In you also need an **Android** OAuth client in the same Google Cloud project with package `app.olet` and the SHA-1 of the signing key.
