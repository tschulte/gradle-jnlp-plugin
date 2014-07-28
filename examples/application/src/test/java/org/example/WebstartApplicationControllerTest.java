package org.example;

import griffon.core.test.GriffonUnitRule;
import griffon.core.test.TestFor;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.fail;

@TestFor(WebstartApplicationController.class)
public class WebstartApplicationControllerTest {
    private WebstartApplicationController controller;

    @Rule
    public final GriffonUnitRule griffon = new GriffonUnitRule();

    @Test
    public void testClickAction() {
        // fail("Not yet implemented!");
    }
}
