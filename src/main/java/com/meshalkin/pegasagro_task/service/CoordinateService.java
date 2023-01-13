package com.meshalkin.pegasagro_task.service;

import com.meshalkin.pegasagro_task.exception.IllegalFileException;
import com.meshalkin.pegasagro_task.model.Coordinates;
import com.meshalkin.pegasagro_task.model.PathTraveled;
import com.meshalkin.pegasagro_task.repository.PathTraveledRepository;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class CoordinateService {

    private final PathTraveledRepository pathTraveledRepository;

    public CoordinateService(PathTraveledRepository pathTraveledRepository) {
        this.pathTraveledRepository = pathTraveledRepository;
    }

    public PathTraveled getDistanceFromFile(String fileURL) throws IllegalFileException, IOException {
        File file = new File(fileURL);
        if (FilenameUtils.getExtension(file.getName()).equals("txt") || FilenameUtils.getExtension(file.getName()).equals("log")) {
            PathTraveled pathTraveled = fileProcessing(file);
            pathTraveled.setFile(file.getName());
            pathTraveledRepository.save(pathTraveled);
            return pathTraveled;
        } else {
            throw new IllegalFileException();
        }
    }

    //    Обработка всех строк и сбор их в массив для дальнейшего расчета пройденого пути
    public PathTraveled fileProcessing(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        ArrayList<Coordinates> coordinateList = new ArrayList<>();
        String str = reader.readLine();
        Coordinates coordinates = new Coordinates();
        while (str != null) {
            StringBuilder line = new StringBuilder(str);
            if (line.indexOf(",,") != -1 || (!line.substring(0, 6).equals("$GPGGA") && !line.substring(0, 6).equals("$GNVTG")
                    && !line.substring(0, 6).equals("$BDVTG") && !line.substring(0, 6).equals("$GNZDA") && !line.substring(0, 6).equals("$GPZDA"))) {
                str = reader.readLine();
            } else if (line.substring(0, 6).equals("$GPGGA") && line.length() > 10) {
                coordinates.setWidth(Byte.parseByte(line.substring(17, 19)));
                coordinates.setWidthMinute(Double.parseDouble(line.substring(19, 30)));
                coordinates.setLongitude(Byte.parseByte(line.substring(34, 36)));
                coordinates.setLongitudeMinute(Double.parseDouble(line.substring(36, 47)));
                str = reader.readLine();
            } else if ((line.substring(0, 6).equals("$GNVTG") || line.substring(0, 6).equals("$BDVTG")) && line.length() > 10) {
                int t = line.indexOf(",N,");
                if (t + 6 < str.length() && line.indexOf("0.0$") == -1) {
                    coordinates.setSpeed(Double.parseDouble(line.substring(t + 3, t + 10)));
                    coordinateList.add(coordinates);
                    coordinates = new Coordinates();
                    str = reader.readLine();
                } else if (t + 6 == str.length()) {
                    coordinates.setSpeed(0);
                    coordinateList.add(coordinates);
                    coordinates = new Coordinates();
                    str = reader.readLine();
                } else if (line.indexOf("0.0$") > 0){
                    coordinates.setSpeed(0);
                    coordinateList.add(coordinates);
                    coordinates = new Coordinates();
                    str = line.delete(0, line.indexOf("0.0$")+3).toString();
                }
            } else if (line.substring(0, 6).equals("$GNZDA") || line.substring(0, 6).equals("$GPZDA")) {
                coordinateList.add(coordinates);
                coordinates = new Coordinates();
                str = reader.readLine();
            } else {
                str = reader.readLine();
            }
        }
        return getCompletedPath(coordinateList, file);
    }

//    расчет пройденого растояния
    private PathTraveled getCompletedPath(ArrayList<Coordinates> arrayList, File file) {
        ArrayList<Coordinates> sortedCoordinates = new ArrayList<>();
        for (Coordinates c : arrayList){
            if (c.getSpeed()>2 && c.getLongitude()!=0 && c.getWidth()!=0) sortedCoordinates.add(c);
        }
        double way = 0;
        for (int i = 0; i < sortedCoordinates.size() - 1; i++) {

            double widthPoint1 = Math.toRadians(degreesAndMinutesToDegreesConverter(sortedCoordinates.get(i).getWidth(), sortedCoordinates.get(i).getWidthMinute()));
            double longitudePoint1 = Math.toRadians(degreesAndMinutesToDegreesConverter(sortedCoordinates.get(i).getLongitude(), sortedCoordinates.get(i).getLongitudeMinute()));
            double widthPoint2 = Math.toRadians(degreesAndMinutesToDegreesConverter(sortedCoordinates.get(i+1).getWidth(), sortedCoordinates.get(i+1).getWidthMinute()));
            double longitudePoint2 = Math.toRadians(degreesAndMinutesToDegreesConverter(sortedCoordinates.get(i+1).getLongitude(), sortedCoordinates.get(i+1).getLongitudeMinute()));


            double d1 = Math.sin(Math.abs(widthPoint1-widthPoint2)/2);
            double d2 = Math.sin(Math.abs(longitudePoint1-longitudePoint2)/2);

            double thisWay = 2*6371*Math.asin(Math.sqrt(d1*d1+Math.cos(widthPoint1)*Math.cos(widthPoint2)*d2*d2));

            if(thisWay<1000){
                way = way+thisWay;
            }
        }
        PathTraveled pathTraveled = new PathTraveled();
        pathTraveled.setWay(way);
        return pathTraveled;
    }

//    перевод градусов и минут в градусы
    private double degreesAndMinutesToDegreesConverter(byte width, double widthMinute) {
        String str = String.valueOf(width)+".";
        double d = widthMinute / 60;
        StringBuilder str2 = new StringBuilder(String.valueOf(d));
        str2.delete(0,2);
        str2.insert(0, str);
        return Double.parseDouble(String.valueOf(str2));
    }

    public List<PathTraveled> findAll(){
        return pathTraveledRepository.findAll();
    }

}