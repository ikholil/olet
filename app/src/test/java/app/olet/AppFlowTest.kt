package app.olet

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.github.takahirom.roborazzi.captureScreenRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Drives the real app end to end on the JVM (Robolectric) and saves screenshots to
 * app/build/screenshots so the UI can be reviewed without a device.
 */
@OptIn(ExperimentalTestApi::class, com.github.takahirom.roborazzi.ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w400dp-h860dp-xhdpi")
class AppFlowTest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    private fun shot(name: String) {
        rule.waitForIdle()
        captureScreenRoboImage("build/screenshots/$name.png")
    }

    private fun waitText(text: String, substring: Boolean = false) =
        rule.waitUntil(10_000) { rule.onAllNodesWithText(text, substring = substring, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }

    private fun click(text: String, scroll: Boolean = false) {
        val node = rule.onAllNodesWithText(text, useUnmergedTree = true).onFirst()
        if (scroll) node.performScrollTo()
        node.performClick()
        rule.waitForIdle()
    }

    private fun goTab(label: String) {
        rule.onNodeWithContentDescription(label, useUnmergedTree = true).performClick()
        rule.waitForIdle()
    }

    @Test
    fun fullFlow() {
        // Login
        waitText("Continue without account")
        shot("01_login")
        rule.onNodeWithText("Your name (optional)").performTextInput("Ayesha Rahman")
        click("Continue without account")

        // Home
        waitText("Total balance")
        waitText("Ayesha Rahman")

        // Add an expense
        click("Add")
        waitText("New transaction")
        rule.onAllNodes(hasSetTextAction()).onFirst().performTextInput("250")
        rule.onNodeWithText("Title").performTextInput("Lunch at Kacchi Bhai")
        shot("03_add_expense")
        click("Save expense")
        waitText("Lunch at Kacchi Bhai")

        // Add an income
        rule.onAllNodesWithText("Income", useUnmergedTree = true).let { it[it.fetchSemanticsNodes().size - 1] }.performClick() // quick action
        waitText("Save income")
        rule.onAllNodes(hasSetTextAction()).onFirst().performTextInput("45000")
        rule.onNodeWithText("Title").performTextInput("November salary")
        click("Save income")
        waitText("November salary")
        waitText("৳44,750")  // 45000 - 250 total balance
        shot("02_home")

        // Activity list + edit/delete
        goTab("Activity")
        waitText("Lunch at Kacchi Bhai")
        shot("04_activity")
        click("Lunch at Kacchi Bhai")
        waitText("Edit transaction")
        rule.onNodeWithContentDescription("Delete", useUnmergedTree = true).performClick()
        rule.onNode(hasText("Delete") and androidx.compose.ui.test.hasClickAction()).performClick()
        rule.waitUntil(10_000) { rule.onAllNodesWithText("Lunch at Kacchi Bhai").fetchSemanticsNodes().isEmpty() }

        // Loans
        goTab("Loans")
        waitText("I'll receive")
        rule.onNodeWithContentDescription("Add loan", useUnmergedTree = true).performClick()
        waitText("New loan")
        rule.onNodeWithText("Person name").performTextInput("Tanvir")
        rule.onNodeWithText("Amount").performTextInput("5000")
        click("Save", scroll = true)
        waitText("Tanvir")
        click("Received")
        waitText("Remaining: ৳5,000")
        click("Save")
        waitText("Settled")
        click("I owe")
        rule.onNodeWithContentDescription("Add loan", useUnmergedTree = true).performClick()
        waitText("New loan")
        rule.onNodeWithText("Person name").performTextInput("Shuvo")
        rule.onNodeWithText("Amount").performTextInput("1200")
        click("Save", scroll = true)
        waitText("Shuvo")
        shot("05_loans")

        // Investments
        goTab("Invest")
        waitText("No investments yet")
        rule.onNodeWithContentDescription("Add investment", useUnmergedTree = true).performClick()
        waitText("New investment")
        rule.onNodeWithText("Company or person").performTextInput("Grameenphone shares")
        rule.onNodeWithText("Contact info (phone, email, address)").performTextInput("broker@example.com")
        rule.onNodeWithText("Amount invested").performTextInput("20000")
        shot("06_investment_form")
        click("Save", scroll = true)
        waitText("Grameenphone shares")
        shot("07_investments")

        // Accounts
        goTab("More")
        waitText("Accounts")
        shot("08_more")
        click("Accounts")
        waitText("Total across accounts")
        rule.onNodeWithContentDescription("Add account", useUnmergedTree = true).performClick()
        waitText("New account")
        rule.onNodeWithText("Account name (e.g. DBBL, bKash)").performTextInput("bKash")
        click("Mobile Banking")
        rule.onNodeWithText("Opening balance").performTextInput("3000")
        click("Save", scroll = true)
        waitText("bKash")
        shot("09_accounts")
        rule.onNodeWithContentDescription("Back", useUnmergedTree = true).performClick()

        // Categories: add one
        waitText("Categories")
        click("Categories")
        waitText("Food")
        rule.onNodeWithContentDescription("Add category", useUnmergedTree = true).performClick()
        waitText("New category")
        rule.onNodeWithText("Name").performTextInput("Rent")
        rule.onNodeWithContentDescription("home", useUnmergedTree = true).performClick()
        click("Save", scroll = true)
        waitText("Rent")
        shot("10_categories")
        rule.onNodeWithContentDescription("Back", useUnmergedTree = true).performClick()

        // Back home, then dark theme
        goTab("Home")
        waitText("Total balance")
        waitText("৳48,000") // 45,000 salary (lunch deleted) + 3,000 opening on bKash
        goTab("More")
        click("Theme")
        click("Dark")
        goTab("Home")
        waitText("Total balance")
        shot("11_home_dark")
    }
}
