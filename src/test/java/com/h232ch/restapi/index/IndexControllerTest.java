package com.h232ch.restapi.index;

import com.h232ch.restapi.common.BaseControllerTest;
import com.h232ch.restapi.common.RestDocsConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*; // 스태틱으로 넣어줘야 함;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//@RunWith(SpringRunner.class)
//@SpringBootTest
//@AutoConfigureMockMvc
//@AutoConfigureRestDocs
//@Import(RestDocsConfiguration.class)
// BaseControllerTest
public class IndexControllerTest extends BaseControllerTest {

//    @Autowired
//    MockMvc mockMvc;

    @Test
    public void index() throws Exception {
        this.mockMvc.perform(get("/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("_links.events").exists());
    }
}
