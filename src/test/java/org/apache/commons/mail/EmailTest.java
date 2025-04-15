package org.apache.commons.mail;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import java.util.Date;
import java.util.Properties;

public class EmailTest {

    // Sample list of test email addresses used in multiple tests
    private static final String[] TEST_EMAILS = {
        "ab@bc.com", 
        "a.b@c.org", 
        "abcdefghijklmnopqrs@abcdefghijklmnopqrst.com.bd"
    };

    private EmailConcrete email;

    // Initialize the email object before each test
    @Before
    public void setUpEmailTest() throws Exception {
        email = new EmailConcrete();
    }

    // Clean up after each test
    @After
    public void tearDownEmailTest() throws Exception {
        email = null;
    }

    // Test: Adding multiple BCC recipients
    @Test
    public void testAddBcc() throws Exception {
        email.addBcc(TEST_EMAILS);
        assertEquals(3, email.getBccAddresses().size());
    }

    // Test: Adding a single CC recipient
    @Test
    public void testAddCc() throws Exception {
        email.addCc("cc@example.com");
        assertEquals(1, email.getCcAddresses().size());
    }

    // Test: Adding headers (valid and invalid cases)
    @Test
    public void testAddHeader() {
        email.addHeader("X-Test-Header", "HeaderValue");
        assertEquals("HeaderValue", email.headers.get("X-Test-Header"));
        
     
        try {
            email.addHeader("", "HeaderValue");
            fail("Expected IllegalArgumentException for empty header name");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        try {
            email.addHeader("X-Test-Header", "");
            fail("Expected IllegalArgumentException for empty header value");
        } catch (IllegalArgumentException e) {
            
        }
    }

    // Test: Adding a replyto address
    @Test
    public void testAddReplyTo() throws Exception {
        email.addReplyTo("reply@example.com", "Reply User");
        assertEquals(1, email.getReplyToAddresses().size());
    }

    //  test for various cases in buildMimeMessage()
    @Test
    public void testBuildMimeMessage_AllCases() throws Exception {
        // 1. Valid message with basic fields
        email.setFrom("from@example.com");
        email.addTo("to@example.com");
        email.setSubject("Test Subject");
        email.setHostName("Mime.example.com");
        email.setMsg("message");
        email.buildMimeMessage();

        assertNotNull("MimeMessage should not be null", email.getMimeMessage());
        assertEquals("Subject mismatch", "Test Subject", email.getMimeMessage().getSubject());
        assertEquals("From mismatch", "from@example.com", email.getMimeMessage().getFrom()[0].toString());
        assertEquals("To mismatch", "to@example.com", email.getMimeMessage().getRecipients(Message.RecipientType.TO)[0].toString());

        // 2. Valid message with CC and BCC recipients
        EmailConcrete email2 = new EmailConcrete();
        email2.setFrom("from@example.com");
        email2.addTo("to@example.com");
        email2.addCc("cc@example.com");
        email2.addBcc("bcc@example.com");
        email2.setSubject("Test Subject");
        email2.setHostName("Mime.example.com");
        email2.setMsg("message");
        email2.buildMimeMessage();

        assertNotNull(email2.getMimeMessage());
        assertEquals("cc@example.com", email2.getMimeMessage().getRecipients(Message.RecipientType.CC)[0].toString());
        assertEquals("bcc@example.com", email2.getMimeMessage().getRecipients(Message.RecipientType.BCC)[0].toString());

        // 3. Valid message with a custom header
        EmailConcrete email3 = new EmailConcrete();
        email3.setFrom("from@example.com");
        email3.addTo("to@example.com");
        email3.setSubject("Test Subject");
        email3.setHostName("Mime.example.com");
        email3.addHeader("X-Custom-Header", "HeaderValue");
        email3.setMsg("message");
        email3.buildMimeMessage();

        assertEquals("HeaderValue", email3.getMimeMessage().getHeader("X-Custom-Header")[0]);

        // 4. Invalid case: missing from address
        EmailConcrete email4 = new EmailConcrete();
        email4.addTo("to@example.com");
        email4.setSubject("Test Subject");
        email4.setHostName("Mime.example.com");
        try {
            email4.buildMimeMessage();
            fail("Expected EmailException for missing 'From' address");
        } catch (EmailException e) {
            // Expected
        }

        // 5. Invalid case: missing recipient
        EmailConcrete email5 = new EmailConcrete();
        email5.setFrom("from@example.com");
        email5.setSubject("Test Subject");
        email5.setHostName("Mime.example.com");
        try {
            email5.buildMimeMessage();
            fail("Expected EmailException for missing recipient");
        } catch (EmailException e) {
            // Expected
        }

        // 6. Valid HTML content
        EmailConcrete email8 = new EmailConcrete();
        email8.setFrom("from@example.com");
        email8.addTo("to@example.com");
        email8.setHostName("Mime.example.com");
        email8.setSubject("HTML Content");
        email8.setContent("<h1>Hello</h1>", "text/html");
        email8.buildMimeMessage();

        assertTrue(email8.getMimeMessage().getDataHandler().getContentType().contains("text/html"));

        // 7. Invalid: Attempting to build the message twice
        EmailConcrete email6 = new EmailConcrete();
        email6.setFrom("from@example.com");
        email6.addTo("to@example.com");
        email6.setSubject("Test Subject");
        email6.setHostName("Mime.example.com");
        email6.setMsg("message");
        email6.buildMimeMessage();

        assertNotNull(email6.getMimeMessage());

        try {
            email6.buildMimeMessage();
            fail("Expected IllegalStateException when building MimeMessage twice");
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    // Test: Getting and setting the hostname
    // @Test
    // public void testGetHostName() {
    //     assertNull(email.getHostName());
    //     email.setHostName("Host.example.com");
    //     assertEquals("smtp.example.com", email.getHostName()); // Assumes internal transformation
    // }

    // Test: Creating a valid mail session when host is set
    @Test
    public void testGetMailSession() throws Exception {
        email.setHostName("Host.example.com");
        Session session = email.getMailSession();
        assertNotNull(session);
    }

    // Test: Exception when trying to get a mail session with no hostname
    @Test
    public void testGetMailSession_MissingHostThrowsException() {
        
        System.clearProperty("mail.host");

        try {
            email.getMailSession();
            fail("Expected EmailException for missing mail host name");
        } catch (EmailException e) {
            assertEquals("Cannot find valid hostname for mail session", e.getMessage());
        }
    }

    // Test: Get and set sent date
    // @Test
    // public void testGetSentDate() {
    //     Date before = new Date();

    //     // Should auto set to current time
    //     Date sentDate = email.getSentDate();
    //     assertNotNull(sentDate);
    //     assertTrue(sentDate.equals(before) || sentDate.after(before));

    //     // Set custom date
    //     Date customDate = new Date(1000000000);
    //     email.setSentDate(customDate);
    //     assertEquals(customDate, email.getSentDate());

    //     // Reset to null, should  auto set
    //     email.setSentDate(null);
    //     assertNotNull(email.getSentDate());
    //     assertTrue(email.getSentDate().after(before));
    // }

    // Test: Default socket timeout
    @Test
    public void testGetSocketConnectionTimeout() {
        assertEquals(60000, email.getSocketConnectionTimeout());
    }

    // Test: Setting and retrieving "from" email address
    @Test
    public void testSetFrom() throws Exception {
        email.setFrom("from@example.com");
        assertEquals("from@example.com", email.getFromAddress().getAddress());
    }
}
