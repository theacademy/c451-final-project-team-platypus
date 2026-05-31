/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mthree.stocksimulator.controller;

import mthree.stocksimulator.service.UserService;
import mthree.stocksimulator.view.SimulatorView;

/**
 *
 * @author gabri
 */
public class UserController {
    
    private final UserService userService;
    private final SimulatorView io;

    public UserController(UserService userService, SimulatorView io) {
        this.userService = userService;
        this.io = io;
    }
    
    
}
