package main.java.bgu.spl.mics.example;

import main.java.bgu.spl.mics.MicroService;

public interface ServiceCreator {
    MicroService create(String name, String[] args);
}
