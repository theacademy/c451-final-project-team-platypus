/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/SQLTemplate.sql to edit this template
 */
/**
 * Author:  gabri
 * Created: 1 Jun 2026
 */

USE stocksimulator;

TRUNCATE User;
INSERT INTO User (userName, accountBal) VALUES ('admin', 100000.00);

TRUNCATE Stock;
INSERT INTO Stock (stockName, stockCode) VALUES ('Apple', 'AAPL');
INSERT INTO Stock (stockName, stockCode) VALUES ('Microsoft', 'MSFT');
INSERT INTO Stock (stockName, stockCode) VALUES ('IBM', 'IBM');
INSERT INTO Stock (stockName, stockCode) VALUES ('Nvidia', 'NVDA');
INSERT INTO Stock (stockName, stockCode) VALUES ('Dell', 'DELL');