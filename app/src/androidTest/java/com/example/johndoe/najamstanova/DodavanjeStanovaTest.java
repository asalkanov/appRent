package com.example.johndoe.najamstanova;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.EditText;
import android.widget.TextView;

import junit.framework.Assert;

import org.junit.Test;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.containsString;


public class DodavanjeStanovaTest extends ActivityInstrumentationTestCase2<DodavanjeStanova> {

    private DodavanjeStanova mTestActivity;
    private EditText opis, cijena, povrsina;


    public DodavanjeStanovaTest() {
        super(DodavanjeStanova.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Starts the activity under test using
        // the default Intent with:
        // action = {@link Intent#ACTION_MAIN}
        // flags = {@link Intent#FLAG_ACTIVITY_NEW_TASK}
        // All other fields are null or empty.
        mTestActivity = getActivity();
        opis = mTestActivity.findViewById(R.id.opis);
        cijena = mTestActivity.findViewById(R.id.cijenaCL);
        povrsina = mTestActivity.findViewById(R.id.povrsina);

    }

    /**
     * Test if your test fixture has been set up correctly.
     * You should always implement a test that
     * checks the correct setup of your test fixture.
     * If this tests fails all other tests are
     * likely to fail as well.
     */
    public void testPreconditions() {
        assertNotNull("mTestActivity je null", mTestActivity);
        assertEquals("", opis.getText().toString());
        assertNotNull("Opis stana je null", opis);
        assertNotNull("Cijena stana je null", cijena);
        assertNotNull("Povrsina stana je null", povrsina);
    }
    public void testMainThread_operations() {
        Espresso.onView(ViewMatchers.withId(R.id.opis)).perform(ViewActions.typeText("Dvosobni stan"));
        Espresso.onView(ViewMatchers.withId(R.id.povrsina)).perform(ViewActions.typeText(String.valueOf(100)));
        Espresso.onView(ViewMatchers.withId(R.id.povrsina)).perform(ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.brojSoba)).perform(ViewActions.click());
        onData(anything()).atPosition(2).perform(click());
        Espresso.onView(ViewMatchers.withId(R.id.brojSoba)).check(matches(withSpinnerText
                (containsString("Dvosoban"))));
    }
    public void testCheckboxes() {
        Espresso.onView(ViewMatchers.withId(R.id.tvCB)).perform(click());
        Espresso.onView(ViewMatchers.withId(R.id.tvCB)).perform(click());
        Espresso.onView(ViewMatchers.withId(R.id.tvCB)).perform(click());
    }

}

// Try to add a message to add context to your assertions.
// These messages will be shown if
// a tests fails and make it easy to
// understand why a test failed