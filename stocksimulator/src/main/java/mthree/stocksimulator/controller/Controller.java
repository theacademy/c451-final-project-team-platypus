/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mthree.stocksimulator.controller;

/**
 *
 * @author gabri
 */
public class Controller {
    
    private final SimController simController;
    private final UserController userController;
    
    public Controller(SimController sController, UserController uController){
        this.simController = sController;
        this.userController = uController;
    }
}
