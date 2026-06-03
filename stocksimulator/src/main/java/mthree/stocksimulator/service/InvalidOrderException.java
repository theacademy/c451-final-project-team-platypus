/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mthree.stocksimulator.service;

/**
 *
 * @author gabri
 */
public class InvalidOrderException extends Exception {
    public InvalidOrderException(String message){
        super(message);
    }
    public InvalidOrderException(String message, Throwable cause){
        super(message, cause);
    }
}