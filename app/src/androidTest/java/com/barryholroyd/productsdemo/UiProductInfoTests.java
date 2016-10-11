package com.barryholroyd.productsdemo;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Automated tests for the ProductInfo page.
 * <p>
 * These tests use Espresso.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class UiProductInfoTests
{
    @Rule
    public ActivityTestRule<ActivityProductInfo> activityRule =
            new ActivityTestRule<>(ActivityProductInfo.class);

    @Test
    public void when_PressAllProducts_Expect_ActivityProductList() {
        // TBD: or pressBack().
        /*
        onView(withId(R.id.my_view))      // withId(R.id.my_view) is a ViewMatcher
  .perform(click())               // click() is a ViewAction
  .check(matches(isDisplayed())); // matches(isDisplayed()) is a ViewAssertion
         */
        onView(withId(R.id.button_display_productlist)).perform(click());
    }
}
