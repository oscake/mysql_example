/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hello;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ParticleRepository particleRepository;

	//Make sure that the repository is empty befor each test
	@Before
	public void deleteAllBeforeTests() throws Exception {
		particleRepository.deleteAll();
	}


	@Test
	public void shouldReturnRepositoryIndex() throws Exception {
		//Check that the repository exists
		mockMvc.perform(get("/")).andDo(print()).andExpect(status().isOk()).andExpect(
				jsonPath("$._links.particles").exists());
	}

	@Test
	public void shouldCreateEntity() throws Exception {
		//Add a particle via the GET method and check the return message
		mockMvc.perform(get("/demo/add?name=electron&mass=0.511&charge=-1")).andDo(print()).andExpect(
						status().isOk()).andExpect(content().
                string("Saved: electron with mass 0.511 MeV and charge -1.0"));

		//Check that we cannot add a second particle with the same name
		mockMvc.perform(get("/demo/add?name=electron&mass=0.511&charge=-1")).andDo(print()).andExpect(
				status().isOk()).andExpect(content().string("There is already a electron in the database"));
	}

    @Test
    public void shouldPostEntity() throws Exception {
        //Add a particle via the POST method and check the return message
        mockMvc.perform(post("/demo/post").contentType(MediaType.APPLICATION_JSON).
                content("{\"name\":\"electron\",\"mass\":0.511,\"charge\":-1.0}")).andExpect(
                status().isOk()).andExpect(content().
                string("Saved: electron with mass 0.511 MeV and charge -1.0"));

        //Check that we cannot add a second particle with the same name
        mockMvc.perform(post("/demo/post").contentType(MediaType.APPLICATION_JSON).content(
                "{\"name\":\"electron\",\"mass\":0.511,\"charge\":-1.0}")).andExpect(
                status().isOk()).andExpect(content().string("There is already a electron in the database"));
    }

	@Test
	public void shouldQueryEntity() throws Exception {
        //Add a particle
		mockMvc.perform(get("/demo/add?name=electron&mass=0.511&charge=-1"));

		//Search for it and check that it is returned
		mockMvc.perform(get("/demo/findByName?name={name}", "electron")).andExpect(
						status().isOk()).andExpect(content().string("[{\"name\":\"electron\",\"mass\":0.511,\"charge\":-1.0}]"));
	}


	@Test
	public void shouldDeleteEntity() throws Exception {
        //Add particle
		mockMvc.perform(get("/demo/add?name=electron&mass=0.511&charge=-1"));

        //Delete it and check the return message
		mockMvc.perform(delete("/demo/delete?name={name}", "electron")).andExpect(content().string("Deleted particles with name electron"));

		//Check that we cannot delete it again, i.e. it is already deleted
		mockMvc.perform(delete("/demo/delete?name={name}", "electron")).andExpect(
						status().isOk()).andExpect(content().string("No such entity to delete"));
	}
}
