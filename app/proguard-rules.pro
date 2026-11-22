# Credential Manager / Google ID
-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }

# Enum names are persisted in Room/DataStore as text; keep them stable.
-keep enum app.olet.data.** { *; }
