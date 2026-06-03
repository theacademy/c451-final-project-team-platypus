/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/SQLTemplate.sql to edit this template
 */
/**
 * Author:  gabri
 * Created: 1 Jun 2026
 */

USE stocksimulator;
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE User;
INSERT INTO User (userName, accountBal) VALUES ('admin', 100000.00);

TRUNCATE Stock;
INSERT INTO Stock (stockName, stockCode) VALUES ('Apple', 'AAPL');
INSERT INTO Stock (stockName, stockCode) VALUES ('Microsoft', 'MSFT');
INSERT INTO Stock (stockName, stockCode) VALUES ('IBM', 'IBM');
INSERT INTO Stock (stockName, stockCode) VALUES ('Nvidia', 'NVDA');
/*INSERT INTO Stock (stockName, stockCode) VALUES ('Dell', 'DELL');*/
INSERT INTO Stock (stockName, stockCode) VALUES ('Bank of America', 'BAC');
INSERT INTO Stock (stockName, stockCode) VALUES ('Goldman Sachs', 'GS');
INSERT INTO Stock (stockName, stockCode) VALUES ('Wells Fargo', 'WFC');
INSERT INTO Stock (stockName, stockCode) VALUES ('Citigroup', 'C');
INSERT INTO Stock (stockName, stockCode) VALUES ('Ford', 'F');
INSERT INTO Stock (stockName, stockCode) VALUES ('Toyota', 'TM');
INSERT INTO Stock (stockName, stockCode) VALUES ('Honda', 'HMC');
INSERT INTO Stock (stockName, stockCode) VALUES ('Harley-Davidson', 'HOG');
INSERT INTO Stock (stockName, stockCode) VALUES ('Southwest Airlines', 'LUV');
INSERT INTO Stock (stockName, stockCode) VALUES ('Alaska Air Group', 'ALK');
INSERT INTO Stock (stockName, stockCode) VALUES ('SkyWest', 'SKYW');
INSERT INTO Stock (stockName, stockCode) VALUES ('Ryanair', 'RYAAY');
INSERT INTO Stock (stockName, stockCode) VALUES ('Amazon', 'AMZN');

SET FOREIGN_KEY_CHECKS = 1;