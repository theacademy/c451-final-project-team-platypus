/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mthree.stocksimulator.controller;

import mthree.stocksimulator.view.SimulatorView;

/**
 *
 * @author gabri
 */
public class Controller {
    
    private final SimController simController;
    private final UserController userController;
    private final SimulatorView view;
    
    public Controller(SimController sController, UserController uController, SimulatorView sView){
        this.simController = sController;
        this.userController = uController;
        this.view = sView;
    }
}
