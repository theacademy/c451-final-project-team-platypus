import React, { useState } from 'react'
import DashboardNav from '../../components/DashboardNav'
import Footer from '../../components/Footer'
import { Link } from 'react-router-dom'
import {
  ResponsiveContainer,
  CartesianGrid,
  Legend,
  Line,
  LineChart,
  XAxis,
  YAxis,
} from 'recharts'

const BuyStocks = () => {
  const [stocks, setStocks] = useState([
    {
      category: 'stock',
      name: 'Apple',
      abbr: 'AAPL',
      price: '312.06',
      change: '2.5',
    },
    {
      category: 'stock',
      name: 'Netflix',
      abbr: 'NFLX',
      price: '200',
      change: '1.5',
    },
    {
      category: 'stock',
      name: 'Tesla',
      abbr: 'tsla',
      price: '300',
      change: '1.2',
    },
  ])
  const [forex, setForex] = useState([
    {
      category: 'forex',
      name: 'United States Dollar',
      abbr: 'USD',
      price: '25',
      change: '1.6',
    },
    {
      category: 'forex',
      name: 'British Pound',
      abbr: 'GBPD',
      price: '30',
      change: '3.4',
    },
    {
      category: 'forex',
      name: 'Japanese Yen',
      abbr: 'JPY',
      price: '5',
      change: '2.8',
    },
  ])
  const [crypto, setCrypto] = useState([
    {
      category: 'crypto',
      name: 'BitCoin',
      abbr: 'BTCN',
      price: '75303',
      change: '24',
    },
    {
      category: 'crypto',
      name: 'etherium',
      abbr: 'ETRM',
      price: '20405',
      change: '5.4',
    },
  ])
  const data = [
    {
      name: 'Page A',
      uv: 400,
      pv: 2400,
      amt: 2400,
    },
    {
      name: 'Page B',
      uv: 300,
      pv: 4567,
      amt: 2400,
    },
    {
      name: 'Page C',
      uv: 320,
      pv: 1398,
      amt: 2400,
    },
    {
      name: 'Page D',
      uv: 200,
      pv: 9800,
      amt: 2400,
    },
    {
      name: 'Page E',
      uv: 278,
      pv: 3908,
      amt: 2400,
    },
    {
      name: 'Page F',
      uv: 189,
      pv: 4800,
      amt: 2400,
    },
  ]

  return (
    <div className="flex flex-col min-h-svh">
      <DashboardNav />
      <div className="container mx-auto flex-grow">
        <div className="border p-2 flex flex-col flex-grow">
          {/* watchlist */}
          <div className="flex">
            <div className="border p-2">
              <div className="grid grid-cols-3 gap-10 p-2 pl-20">
                <div>Symbol</div>
                <div>Price</div>
                <div>Change</div>
              </div>
              <div>Stocks</div>
              <div>
                {stocks.map((stock) => {
                  return (
                    <div className="grid grid-cols-3 gap-10 p-2 pl-20">
                      <div> {stock.abbr} </div>
                      <div> {stock.price} </div>
                      <div> {stock.change} </div>
                    </div>
                  )
                })}
              </div>
              <div>
                <div>Forex</div>
                {forex.map((forex) => {
                  return (
                    <div className="grid grid-cols-3 gap-10 p-2 pl-20">
                      <div> {forex.abbr} </div>
                      <div> {forex.price} </div>
                      <div> {forex.change} </div>
                    </div>
                  )
                })}
              </div>
              <div>
                <div>Crypto</div>
                {crypto.map((crypto) => {
                  return (
                    <div className="grid grid-cols-3 gap-10 p-2 pl-20">
                      <div> {crypto.abbr} </div>
                      <div> {crypto.price} </div>
                      <div> {crypto.change} </div>
                    </div>
                  )
                })}
              </div>
            </div>
          </div>
          {/* trajectory graph and purchase ui */}
          <div className="flex flex-col flex-grow min-w-0">
            {/* trajectory graph */}
            <div style={{ width: '100%', maxWidth: 600, aspectRatio: 1.618 }}>
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={data}>
                  <CartesianGrid />
                  <Line dataKey="uv" />
                  <XAxis dataKey="name" />
                  <YAxis />
                  <Legend />
                </LineChart>
              </ResponsiveContainer>
            </div>
            {/* purchase ui (with news maybe)*/}
            <div>
              <div>Current Balance</div>
              <div>Stock Amt:</div>
              <div>Buy Stock</div>
              <div>Sell Stock</div>
            </div>
          </div>
        </div>
      </div>

      <Footer />
    </div>
  )
}

export default BuyStocks
