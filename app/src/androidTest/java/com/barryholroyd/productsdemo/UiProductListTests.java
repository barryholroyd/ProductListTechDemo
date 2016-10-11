package com.barryholroyd.productsdemo;

import android.app.Activity;
import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import static android.support.test.espresso.intent.Intents.intended;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.intent.matcher.ComponentNameMatchers.hasShortClassName;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.instanceOf;

/**
 * TBD:
 */

@RunWith(AndroidJUnit4.class)
@MediumTest
public class UiProductListTests {
    @Rule
    public IntentsTestRule<ActivityProductList> intentsTestRule =
            new IntentsTestRule<>(ActivityProductList.class);

    /**
     * Click on row 0 and verify that it starts the ActivityProductInfo
     * activity. TBD: verify.
     * <p>
     * Espresso's onData() does not work with RecyclerView, so we have to
     * use onView() and RecyclerViewActions.
     *
     * @see <a href="https://developer.android.com/reference/android/support/test/espresso/contrib/RecyclerViewActions.html">
     *     RecyclerViewActions</a>
     */
    @Test
    public void When_ClickOnRow_Expect_DisplayProductInfoPage() {

        ViewInteraction vi = onView(withId(R.id.list));
        vi.perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));

// DEL:
//        Matcher m = withId(R.id.list);
//        Activity a = intentsTestRule.getActivity();
//        View v = a.findViewById(R.id.list);
//        boolean b = m.matches(v);
//        Support.logd(String.format("MATCH RETURNED: %b", b));

//        intended(allOf(
//                toPackage("com.barryholroyd.productsdemo"),
//                hasComponent(hasShortClassName(".ActivityProductInfo"))
//        ));
    }
}
