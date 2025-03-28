package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MyURLTest {

    @Autowired
    private MyURL myURL;

    @Test
    void testSetUrl() {
        assertDoesNotThrow(() -> myURL.setUrl(DocType.Catalog_Номенклатура, FieldType.Ref_Key, "Number"));
        String url = myURL.setUrl(DocType.Catalog_Номенклатура, FieldType.Ref_Key, "Number");
        assertNotNull(url, "URL не должен быть null");
        assertFalse(url.isEmpty(), "URL не должен быть пустым");
    }
}
