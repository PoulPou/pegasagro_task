package com.meshalkin.pegasagro_task.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coordinates {

//    широта градусы
    private byte width;

//    широта минуты
    private double widthMinute;

//    долгота градусы
    private byte longitude;

//    долгота минуты
    private double longitudeMinute;

//    скорость км/ч
    private double speed;

}
