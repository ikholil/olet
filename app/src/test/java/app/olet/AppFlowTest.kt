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

    private fun quickAdd(kind: String) {
        rule.onNodeWithContentDescription("Add new", useUnmergedTree = true).performClick()
        waitText("What would you like to add?")
        rule.onAllNodesWithText(kind, useUnmergedTree = true).let { it[it.fetchSemanticsNodes().size - 1] }.performClick()
        rule.waitForIdle()
    }

    @Test
    fun fullFlow() {
        // Login
        waitText("Continue without account")
        shot("01_login")
        rule.onNodeWithText("Your name (optional)").performTextInput("Ayesha Rahman")
        click("Continue without account")

        // Home (empty)
        waitText("Total balance")
        waitText("Ayesha Rahman")

        // Add an expense via the center + button
        quickAdd("Expense")
        waitText("New transaction")
        rule.onAllNodes(hasSetTextAction()).onFirst().performTextInput("250")
        rule.onNodeWithText("What was it for?").performTextInput("Lunch at Kacchi Bhai")
        shot("03_add_expense")
        click("Save expense")
        waitText("Spending this month")

        // Autocomplete: typing part of a previous title suggests it and fills amount + category
        quickAdd("Expense")
        waitText("New transaction")
        rule.onNodeWithText("What was it for?").performTextInput("lunch")
        waitText("Suggestions")
        shot("03b_autocomplete")
        click("Lunch at Kacchi Bhai")
        rule.waitUntil(5_000) { rule.onAllNodesWithText("250").fetchSemanticsNodes().isNotEmpty() }
        click("Save expense")
        waitText("Total balance")

        // Add an income
        quickAdd("Income")
        waitText("Save income")
        rule.onAllNodes(hasSetTextAction()).onFirst().performTextInput("45000")
        rule.onNodeWithText("What was it for?").performTextInput("November salary")
        click("Salary")
        click("Save income")
        waitText("৳44,500")  // 45000 - 2*250
        shot("02_home")

        // Activity list + edit/delete one of the lunches
        goTab("Activity")
        waitText("November salary")
        shot("04_activity")
        rule.onAllNodesWithText("Lunch at Kacchi Bhai", useUnmergedTree = true).onFirst().performClick()
        waitText("Edit transaction")
        rule.onNodeWithContentDescription("Delete", useUnmergedTree = true).performClick()
        rule.onNode(hasText("Delete") and androidx.compose.ui.test.hasClickAction()).performClick()
        rule.waitUntil(10_000) { rule.onAllNodesWithText("Lunch at Kacchi Bhai", useUnmergedTree = true).fetchSemanticsNodes().size == 1 }

        // Search
        rule.onNodeWithText("Search title, category, note…", useUnmergedTree = true).performClick()
        rule.onAllNodes(hasSetTextAction()).onFirst().performTextInput("salary")
        rule.waitUntil(5_000) { rule.onAllNodesWithText("Lunch at Kacchi Bhai", useUnmergedTree = true).fetchSemanticsNodes().isEmpty() }
        waitText("November salary")
        rule.onNodeWithContentDescription("Clear search", useUnmergedTree = true).performClick()

        // Loans via quick add
        quickAdd("Loan")
        waitText("New loan")
        rule.onNodeWithText("Person name").performTextInput("Tanvir")
        rule.onNodeWithText("Phone (optional)").performTextInput("01711000000")
        rule.onNodeWithText("Amount").performTextInput("5000")
        click("Save", scroll = true)
        waitText("Tanvir")
        click("Received")
        waitText("Remaining: ৳5,000")
        click("Save")
        waitText("Settled")
        // Person autocomplete on a new loan
        rule.onNodeWithContentDescription("Add loan", useUnmergedTree = true).performClick()
        waitText("New loan")
        waitText("People")
        rule.onAllNodesWithText("Tanvir", useUnmergedTree = true).let { it[it.fetchSemanticsNodes().size - 1] }.performClick()
        waitText("01711000000")
        rule.onNodeWithText("Amount").performTextInput("700")
        click("Save", scroll = true)
        waitText("৳700")
        click("I owe")
        rule.onNodeWithContentDescription("Add loan", useUnmergedTree = true).performClick()
        waitText("New loan")
        click("I borrowed")
        rule.onNodeWithText("Person name").performTextInput("Shuvo")
        rule.onNodeWithText("Amount").performTextInput("1200")
        click("Save", scroll = true)
        waitText("Shuvo")
        waitText("৳1,200")
        rule.waitUntil(3_000) { rule.onAllNodesWithText("7,001,200", substring = true, useUnmergedTree = true).fetchSemanticsNodes().isEmpty() }
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

        // Settings -> Accounts
        goTab("Home")
        rule.onNodeWithContentDescription("Settings", useUnmergedTree = true).performClick()
        waitText("Currency")
        shot("08_settings")
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

        // Dark theme
        click("Theme")
        click("Dark")
        rule.onNodeWithContentDescription("Back", useUnmergedTree = true).performClick()
        waitText("Total balance")
        waitText("৳47,750") // 44,750 + 3,000 opening on bKash
        shot("11_home_dark")
        quickAdd("Expense")
        waitText("New transaction")
        shot("12_add_dark")
    }
}
