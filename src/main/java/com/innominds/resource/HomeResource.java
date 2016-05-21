package com.innominds.resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeResource {

    @RequestMapping("/")
    String index(Model model) {

        return "index";
    }

}
