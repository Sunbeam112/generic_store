package ua.sunbeam.genericstore.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import ua.sunbeam.genericstore.api.security.JWTUtils;
import ua.sunbeam.genericstore.model.DAO.UserRepository;
import ua.sunbeam.genericstore.service.ProductService;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(
        controllers = ProductController.class

)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Mock the ProductService dependency (as before)
    @MockBean
    private ProductService productService;
    @MockBean
    private JWTUtils jwtUtils;

    @MockBean
    private UserRepository userRepository;


    @Test
    public void getAllProductsEndpointReturnsUnauthorizedWhenNoAuth() throws Exception {
        mockMvc.perform(get("/products/all"))
                .andExpect(status().isUnauthorized());
    }
}