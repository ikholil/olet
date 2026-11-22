package app.olet

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import app.olet.data.LoanEntity
import app.olet.data.LoanType
import app.olet.ui.screens.PaymentDialog
import app.olet.ui.theme.OletTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DialogTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun paymentDialogShows() {
        rule.setContent {
            OletTheme {
                PaymentDialog(LoanEntity(person = "A", type = LoanType.LENT, amount = 5000.0, date = 0), "৳", {}, {})
            }
        }
        rule.onNodeWithText("Payment received from A", useUnmergedTree = true).assertExists()
    }

    @Test
    fun progressIdle() {
        rule.setContent {
            OletTheme {
                androidx.compose.material3.LinearProgressIndicator(progress = { 0f }, drawStopIndicator = {})
            }
        }
        rule.waitForIdle()
    }

    @Test
    fun sheetOpenClose() {
        var open by androidx.compose.runtime.mutableStateOf(true)
        rule.setContent {
            OletTheme {
                if (open) app.olet.ui.screens.FormSheet("T", onDismiss = { open = false }, onSave = { open = false }) {
                    androidx.compose.material3.Text("body")
                }
            }
        }
        rule.waitForIdle()
        rule.onNodeWithText("Save", useUnmergedTree = true).performClick()
        rule.waitForIdle()
    }
}
