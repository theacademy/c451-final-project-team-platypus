/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mthree.stocksimulator.controller;

import mthree.stocksimulator.service.SimService;
import mthree.stocksimulator.view.SimulatorView;

/**
 *
 * @author gabri
 */
public class SimController {
    
    private final SimService simService;
    private final SimulatorView io;
    
    public SimController(SimService sService, SimulatorView io){
        this.simService = sService;
        this.io = io;
    }
}
