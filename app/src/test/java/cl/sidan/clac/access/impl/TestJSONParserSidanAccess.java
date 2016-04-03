package cl.sidan.clac.access.impl;

import junit.framework.Assert;

import org.junit.Test;

import java.util.List;

import cl.sidan.clac.access.impl.JSONParserSidanAccess;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class TestJSONParserSidanAccess {

    private static JSONParserSidanAccess sidanAccess = new JSONParserSidanAccess("user", "password");
    private static String MESSAGE_1 = "A message with #38, #68 and #62 as well as #3 and #6 as well as #8";
    private static int SIGNATURES_IN_MESSAGE_1 = 6;

    @Test
    public void testGetAllSignaturesInMessage() throws Exception {

        List<String> signaturesInMessage = sidanAccess.getAllSignaturesInMessage(MESSAGE_1);

        Assert.assertEquals(signaturesInMessage.size(), SIGNATURES_IN_MESSAGE_1);

    }
}