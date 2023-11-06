package com.zimbra.doc.soap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DocExcludedServicesTest {

    @Test
    void test_removeRequest_returns_empty_when_serviceName_is_request() {
        Assertions.assertEquals("", DocExcludedServices.removeRequest("Request"));
    }

    @Test
    void test_removeRequest_returns_input_when_serviceName_length_is_equal_to_request() {
        Assertions.assertEquals("request", DocExcludedServices.removeRequest("request"));
    }

    @Test
    void test_removeRequest_returns_input_when_className_length_is_equal_to_request() {
        Assertions.assertEquals("DocExcludedServices", DocExcludedServices.removeRequest(DocExcludedServices.class));
    }

    @Test
    void test_removeRequest_returns_input_when_serviceName_length_is_less_then_request() {
        Assertions.assertEquals("equest", DocExcludedServices.removeRequest("equest"));
    }

    @Test
    void test_removeRequest_returns_input_when_serviceName_without_Request_length_is_more_than_request() {
        Assertions.assertEquals("Xyz", DocExcludedServices.removeRequest("XyzRequest"));
    }

    @Test
    void test_removeRequest_returns_input_when_ClassName_without_Request_length_is_more_than_request() {
        Assertions.assertEquals("CreateArchive", DocExcludedServices.removeRequest("CreateArchiveRequest"));
    }

    @Test
    void test_isExcludeRequest_returns_false_when_serviceName_is_not_exists() {
        Assertions.assertFalse(DocExcludedServices.isExcludeRequest("AbcRequest"));
    }

    @Test
    void test_isExcludeRequest_returns_true_when_serviceName_is_exists() {
        Assertions.assertTrue(DocExcludedServices.isExcludeRequest("GetSMIMEPublicCertsRequest"));
    }

    @Test
    void test_isExclude_returns_false_when_serviceName_is_not_exists() {
        Assertions.assertFalse(DocExcludedServices.isExclude("Abc"));
    }

    @Test
    void test_isExclude_returns_true_when_serviceName_is_exists() {
        Assertions.assertTrue(DocExcludedServices.isExclude("GetSMIMEPublicCerts"));
    }
}
