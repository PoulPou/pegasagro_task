package com.meshalkin.pegasagro_task.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.File;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class PathTraveled {

    @Id
    @GeneratedValue
    private long id;

//    путь
    private double way;

//    файл с данными
    private String file;

}
