package com.meshalkin.pegasagro_task.controller;

import com.meshalkin.pegasagro_task.exception.IllegalFileException;
import com.meshalkin.pegasagro_task.model.PathTraveled;
import com.meshalkin.pegasagro_task.service.CoordinateService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/coordinates")
public class CoordinatesController {

    private final CoordinateService coordinateService;

    public CoordinatesController(CoordinateService coordinateService) {
        this.coordinateService = coordinateService;
    }

    @GetMapping
    public String home(Model model){
        List<PathTraveled> list = coordinateService.findAll();
        model.addAttribute("home", list);
        return "home.html";
    }


    @PostMapping
    public String getDistanceFromFile (String URL, Model model) throws IllegalFileException, IOException {
        coordinateService.getDistanceFromFile(URL);
        List<PathTraveled> list = coordinateService.findAll();
        model.addAttribute("home", list);
        return "home.html";
    }
}
