package app.olet.auth

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import app.olet.BuildConfig
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

sealed interface GoogleResult {
    data class Success(val name: String, val email: String) : GoogleResult
    data class Error(val message: String) : GoogleResult
    data object Cancelled : GoogleResult
}

object GoogleAuth {
    val isConfigured: Boolean get() = BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank()

    /** [activityContext] must be an Activity context so the account picker can be shown. */
    suspend fun signIn(activityContext: Context): GoogleResult {
        if (!isConfigured) {
            return GoogleResult.Error("Google Sign-In isn't configured in this build. Continue without an account — all features work offline.")
        }
        return try {
            val option = GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID).build()
            val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
            val result = CredentialManager.create(activityContext).getCredential(activityContext, request)
            val cred = result.credential
            if (cred is CustomCredential && cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val google = GoogleIdTokenCredential.createFrom(cred.data)
                GoogleResult.Success(
                    name = google.displayName ?: google.id.substringBefore('@'),
                    email = google.id,
                )
            } else {
                GoogleResult.Error("Unexpected credential type")
            }
        } catch (e: GetCredentialCancellationException) {
            GoogleResult.Cancelled
        } catch (e: NoCredentialException) {
            GoogleResult.Error("No Google account found on this device")
        } catch (e: Exception) {
            GoogleResult.Error(e.message ?: "Google Sign-In failed")
        }
    }

    suspend fun signOut(context: Context) {
        runCatching { CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest()) }
    }
}
